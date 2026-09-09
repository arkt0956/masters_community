package kr.co.csp.common.web;

import kr.co.csp.common.exception.DomainException;
import kr.co.csp.common.exception.NotFoundException;
import kr.co.csp.common.exception.RateLimitException;
import kr.co.csp.common.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 예외를 한곳에서 응답으로 바꾼다 (DR-P05).
 *
 * 왜 한곳에 모으는가: Controller마다 try-catch를 두면 어느 하나가 스택을 그대로
 * 내보내는 순간 내부 구조가 노출된다. 여기서 형태를 고정한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(RateLimitException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ApiErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomain(DomainException e) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse(e.getMessage()));
    }

    /** Bean Validation 실패 (DR-P04). 첫 번째 위반 메시지만 내보낸다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getDefaultMessage())
                .orElse("입력값을 확인해 주세요.");
        return ResponseEntity.badRequest().body(new ApiErrorResponse(message));
    }

    /**
     * 예상하지 못한 실패.
     * 왜 메시지를 고정하는가: 예외 메시지에 쿼리나 파일 경로가 섞여 나갈 수 있다.
     * 원인은 로그로만 남긴다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception e) {
        log.error("처리하지 못한 오류", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요."));
    }
}
