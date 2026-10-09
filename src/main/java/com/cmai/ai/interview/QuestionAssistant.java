package com.cmai.ai.interview;

import dev.langchain4j.service.UserMessage;

/** LangChain4j AI Service: generates the interview question set. The prompt is built in QuestionAgent. */
public interface QuestionAssistant {

    String generate(@UserMessage String prompt);
}
