package kr.co.csp.common.exception;

/** 대상이 없음 (DR-P05). 404로 변환된다. */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }
}
