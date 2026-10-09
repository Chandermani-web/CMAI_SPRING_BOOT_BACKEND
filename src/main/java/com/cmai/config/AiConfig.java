package com.cmai.config;

import com.cmai.ai.interview.FeedbackAssistant;
import com.cmai.ai.interview.QuestionAssistant;
import com.cmai.ai.interview.SummaryAssistant;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import java.time.Duration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Two AI stacks, both pointed at Groq's OpenAI-compatible endpoint:
 * <ul>
 *   <li><b>Spring AI</b> ({@link ChatClient}) powers the resume analyser and roadmap generator.</li>
 *   <li><b>LangChain4j</b> (AI Services) powers the mock-interview agents.</li>
 * </ul>
 */
@Configuration
public class AiConfig {

    // ---- Spring AI ----

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    // ---- LangChain4j ----

    @Bean
    public ChatModel langchain4jChatModel(AppProperties props) {
        AppProperties.Ai ai = props.ai();
        return OpenAiChatModel.builder()
                .baseUrl(ai.baseUrl())
                .apiKey(ai.apiKey())
                .modelName(ai.model())
                .temperature(ai.temperature())
                .maxTokens(ai.maxTokens())
                .maxRetries(ai.maxRetries())
                .timeout(Duration.ofSeconds(ai.timeoutSeconds()))
                .build();
    }

    @Bean
    public QuestionAssistant questionAssistant(ChatModel model) {
        return AiServices.create(QuestionAssistant.class, model);
    }

    @Bean
    public FeedbackAssistant feedbackAssistant(ChatModel model) {
        return AiServices.create(FeedbackAssistant.class, model);
    }

    @Bean
    public SummaryAssistant summaryAssistant(ChatModel model) {
        return AiServices.create(SummaryAssistant.class, model);
    }
}
