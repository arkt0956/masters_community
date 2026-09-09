package kr.co.csp.common.exception;

/**
 * 관리자 인증 실패·미인증 (R-20 · R-49). 401로 변환된다.
 *
 * 메시지에 실패 사유를 구분해 담지 않는다. "ID 없음"과 "비밀번호 틀림"을 나누면
 * 어떤 계정이 존재하는지 알려주는 셈이 된다 (SCR-A05).
 */
public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
