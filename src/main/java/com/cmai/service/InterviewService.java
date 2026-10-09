package com.cmai.service;

import com.cmai.ai.ResumeContext;
import com.cmai.ai.interview.FeedbackAgent;
import com.cmai.ai.interview.GeneratedQuestion;
import com.cmai.ai.interview.InterviewReport;
import com.cmai.ai.interview.QuestionAgent;
import com.cmai.ai.interview.SummaryAgent;
import com.cmai.config.AppProperties;
import com.cmai.dto.FeedbackDto;
import com.cmai.dto.InterviewDto;
import com.cmai.dto.QuestionDto;
import com.cmai.dto.StartInterviewRequest;
import com.cmai.dto.StartInterviewResponse;
import com.cmai.dto.SubmitAnswerRequest;
import com.cmai.dto.SubmitAnswerResponse;
import com.cmai.entity.Interview;
import com.cmai.entity.InterviewQuestion;
import com.cmai.entity.Resume;
import com.cmai.exception.ApiException;
import com.cmai.repository.InterviewRepository;
import com.cmai.repository.ResumeRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Interview workflow (the Spring/LangChain4j version of the old LangGraph graph):
 * start -> generate questions; answer -> feedback; last answer -> feedback + final report.
 *
 * <p>LLM calls are slow, so database transactions are kept short and never span a model call.
 */
@Service
public class InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewService.class);
    private static final Set<String> TYPES = Set.of("technical", "hr");

    private final InterviewRepository interviews;
    private final ResumeRepository resumes;
    private final CoinService coins;
    private final LockService locks;
    private final QuestionAgent questionAgent;
    private final FeedbackAgent feedbackAgent;
    private final SummaryAgent summaryAgent;
    private final TransactionTemplate tx;
    private final AppProperties props;

    public InterviewService(
            InterviewRepository interviews,
            ResumeRepository resumes,
            CoinService coins,
            LockService locks,
            QuestionAgent questionAgent,
            FeedbackAgent feedbackAgent,
            SummaryAgent summaryAgent,
            TransactionTemplate tx,
            AppProperties props) {
        this.interviews = interviews;
        this.resumes = resumes;
        this.coins = coins;
        this.locks = locks;
        this.questionAgent = questionAgent;
        this.feedbackAgent = feedbackAgent;
        this.summaryAgent = summaryAgent;
        this.tx = tx;
        this.props = props;
    }

    // ------------------------------------------------------------------ start

    public StartInterviewResponse start(Long userId, StartInterviewRequest req) {
        String type = req.type().trim().toLowerCase(Locale.ROOT);
        if (!TYPES.contains(type)) {
            throw new ApiException(400, "Invalid interview type. Must be technical or hr");
        }
        String role = req.role().trim();
        boolean useResume = Boolean.TRUE.equals(req.useResume());

        String resumeBlock = null;
        if (useResume) {
            Resume resume = resumes.findByUserId(userId)
                    .orElseThrow(() -> new ApiException(
                            400, "Upload your resume first to personalise the interview"));
            resumeBlock = ResumeContext.interviewBlock(resume);
        }

        // Charge first (atomic, server side); refund if we fail to produce an interview.
        int cost = props.coins().interviewCost();
        int balance = cost > 0 ? coins.deduct(userId, cost) : coins.balance(userId);

        try {
            List<GeneratedQuestion> generated = questionAgent.generate(type, role, resumeBlock);

            Long interviewId = Objects.requireNonNull(tx.execute(status -> {
                Interview interview = new Interview();
                interview.setUserId(userId);
                interview.setType(type);
                interview.setTargetRole(role);
                interview.setUseResume(useResume);
                interview.setCurrentQuestion(0);
                interview.setTotalQuestions(generated.size());
                interview.setStatus(Interview.STATUS_IN_PROGRESS);
                for (GeneratedQuestion g : generated) {
                    InterviewQuestion q = new InterviewQuestion();
                    q.setQuestion(g.question());
                    q.setDifficulty(g.difficulty());
                    q.setTimer(g.timer());
                    interview.addQuestion(q);
                }
                return interviews.save(interview).getId();
            }));

            GeneratedQuestion first = generated.get(0);
            QuestionDto firstQuestion =
                    new QuestionDto(first.question(), "", first.difficulty(), first.timer(), FeedbackDto.empty());
            return new StartInterviewResponse(true, interviewId, 0, generated.size(), firstQuestion, balance);
        } catch (RuntimeException e) {
            if (cost > 0) {
                try {
                    coins.credit(userId, cost);
                } catch (RuntimeException refundError) {
                    log.error("Could not refund {} coins to user {}", cost, userId, refundError);
                }
            }
            throw e;
        }
    }

    // ----------------------------------------------------------------- answer

    private record Pending(int index, int total, String question, String difficulty, String role, String type) {}

    private record Saved(List<QuestionDto> all, QuestionDto next) {}

    public SubmitAnswerResponse submitAnswer(Long userId, SubmitAnswerRequest req) {
        String lockKey = "lock:interview:" + req.interviewId();
        if (!locks.tryLock(lockKey, Duration.ofSeconds(180))) {
            throw new ApiException(409, "Your previous answer is still being evaluated. Please wait a moment.");
        }
        try {
            return process(userId, req);
        } finally {
            locks.unlock(lockKey);
        }
    }

    private SubmitAnswerResponse process(Long userId, SubmitAnswerRequest req) {
        Long interviewId = req.interviewId();
        String answer = req.answer().trim();

        // 1) Read the current question.
        Pending pending = Objects.requireNonNull(tx.execute(status -> {
            Interview interview = findOwned(interviewId, userId);
            if (Interview.STATUS_COMPLETED.equals(interview.getStatus())) {
                throw new ApiException(400, "Interview has already been completed");
            }
            int index = interview.getCurrentQuestion();
            if (index < 0 || index >= interview.getQuestions().size()) {
                throw new ApiException(400, "No current question found");
            }
            InterviewQuestion q = interview.getQuestions().get(index);
            return new Pending(
                    index,
                    interview.getQuestions().size(),
                    q.getQuestion(),
                    q.getDifficulty(),
                    interview.getTargetRole(),
                    interview.getType());
        }));

        // 2) Ask the model for feedback (no transaction open).
        FeedbackDto feedback = feedbackAgent.evaluate(pending.question(), answer, pending.difficulty());
        boolean completed = pending.index() + 1 >= pending.total();

        // 3) Save answer + feedback and advance.
        Saved saved = Objects.requireNonNull(tx.execute(status -> {
            Interview interview = findOwned(interviewId, userId);
            InterviewQuestion q = interview.getQuestions().get(pending.index());
            q.setUserAnswer(answer);
            feedback.applyTo(q);
            if (!completed) {
                interview.setCurrentQuestion(pending.index() + 1);
            }
            List<QuestionDto> all = interview.getQuestions().stream().map(QuestionDto::from).toList();
            return new Saved(all, completed ? null : all.get(pending.index() + 1));
        }));

        // 4) After the last answer, build the final report from ALL answers and feedback.
        if (completed) {
            InterviewReport report = summaryAgent.summarize(pending.type(), pending.role(), saved.all());
            tx.executeWithoutResult(status -> {
                Interview interview = findOwned(interviewId, userId);
                interview.setStatus(Interview.STATUS_COMPLETED);
                interview.setOverallScore(report.overallScore());
                interview.setSummary(report.summary());
                interview.setStrengths(new ArrayList<>(report.strengths()));
                interview.setWeaknesses(new ArrayList<>(report.weaknesses()));
                interview.setRecommendations(new ArrayList<>(report.recommendations()));
            });
        }

        int currentQuestion = completed ? pending.index() : pending.index() + 1;
        return new SubmitAnswerResponse(true, completed, currentQuestion, pending.total(), saved.next(), feedback);
    }

    // ------------------------------------------------------------------ reads

    @Transactional(readOnly = true)
    public InterviewDto get(Long userId, Long interviewId) {
        return InterviewDto.from(findOwned(interviewId, userId));
    }

    @Transactional(readOnly = true)
    public List<InterviewDto> list(Long userId) {
        return interviews.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(InterviewDto::from)
                .toList();
    }

    private Interview findOwned(Long interviewId, Long userId) {
        return interviews.findByIdAndUserId(interviewId, userId)
                .orElseThrow(() -> new ApiException(404, "Interview not found"));
    }
}
