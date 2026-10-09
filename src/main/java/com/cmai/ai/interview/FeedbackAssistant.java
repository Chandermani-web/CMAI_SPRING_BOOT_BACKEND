package com.cmai.ai.interview;

import dev.langchain4j.service.UserMessage;

/** LangChain4j AI Service: evaluates a single answer. */
public interface FeedbackAssistant {

    String evaluate(@UserMessage String prompt);
}
