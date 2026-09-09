package kr.co.csp.common.exception;

/**
 * IP 일일 제한 초과 (R-46 · R-48). 429로 변환된다.
 *
 * 왜 별도 예외인가: 화면이 "잘못 입력했다"와 "오늘은 더 못 쓴다"를 다르게 안내해야 한다
 * (SCR-005 · SCR-006 유효성·예외 표).
 */
public class RateLimitException extends DomainException {

    public RateLimitException(String message) {
        super(message);
    }
}
