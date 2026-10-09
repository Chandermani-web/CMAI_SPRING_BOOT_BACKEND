package com.cmai.ai;

import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper over the Spring AI ChatClient.
 *
 * <p>Messages are passed as objects on purpose: ChatClient's {@code .system(String)} / {@code .user(String)}
 * treat the text as a template and would choke on the literal JSON braces in our prompts
 * and on any braces inside a resume.
 */
@Component
public class SpringAiChat {

    private final ChatClient chatClient;

    public SpringAiChat(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String complete(String system, String user) {
        return AiCalls.guarded(() -> chatClient
                .prompt(new Prompt(List.of(new SystemMessage(system), new UserMessage(user))))
                .call()
                .content());
    }
}
