package kr.co.csp.common.exception;

/**
 * 예상 가능한 업무 실패 (DR-P05). 400으로 변환된다.
 *
 * 메시지는 사용자에게 그대로 노출되므로 테이블명·쿼리·스택을 담지 않는다.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
