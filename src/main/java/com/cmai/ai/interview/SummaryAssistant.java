package com.cmai.ai.interview;

import dev.langchain4j.service.UserMessage;

/** LangChain4j AI Service: writes the final interview report. */
public interface SummaryAssistant {

    String summarize(@UserMessage String prompt);
}
