package com.cmai.ai;

import com.cmai.exception.ApiException;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Runs a model call and converts any provider failure into a clean 502 for the client. */
public final class AiCalls {

    private static final Logger log = LoggerFactory.getLogger(AiCalls.class);

    private AiCalls() {}

    public static String guarded(Supplier<String> call) {
        try {
            return call.get();
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("LLM call failed: {}", e.toString());
            throw new ApiException(502, "The AI service could not complete the request. Please try again.");
        }
    }
}
