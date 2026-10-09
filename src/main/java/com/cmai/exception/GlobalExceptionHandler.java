package com.cmai.exception;

import com.cmai.config.AppProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Turns every failure into the same {"success": false, "message": "..."} JSON shape. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final AppProperties props;

    public GlobalExceptionHandler(AppProperties props) {
        this.props = props;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
        Map<String, Object> body = body(ex.getMessage());
        body.putAll(ex.getExtra());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        FieldError first = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = first == null
                ? "Invalid request"
                : first.getField() + ": " + first.getDefaultMessage();
        return ResponseEntity.badRequest().body(body(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(body("Malformed request body"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(413).body(body("File is too large. The limit is 15 MB."));
    }

    @ExceptionHandler({
        NoResourceFoundException.class,
        HttpRequestMethodNotSupportedException.class,
        HttpMediaTypeNotSupportedException.class,
        MissingServletRequestPartException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String, Object>> handleStandardMvc(Exception ex) {
        int status = 400;
        String message = "Bad request";
        if (ex instanceof ErrorResponse errorResponse) {
            status = errorResponse.getStatusCode().value();
        }
        if (ex instanceof NoResourceFoundException) {
            message = "Resource not found";
        } else if (ex instanceof HttpRequestMethodNotSupportedException) {
            message = "Method not allowed";
        } else if (ex instanceof HttpMediaTypeNotSupportedException) {
            message = "Unsupported content type";
        } else if (ex instanceof MissingServletRequestPartException) {
            message = "Missing file part in the request";
        } else if (ex instanceof MissingServletRequestParameterException) {
            message = "Missing request parameter";
        }
        return ResponseEntity.status(status).body(body(message));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleData(DataAccessException ex) {
        log.error("Database error", ex);
        return ResponseEntity.status(503).body(body("Database is temporarily unavailable"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
        log.error("Unhandled error", ex);
        Map<String, Object> body = body("Internal server error");
        if (!props.isProduction()) {
            body.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return ResponseEntity.status(500).body(body);
    }

    private static Map<String, Object> body(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", message);
        return body;
    }
}
