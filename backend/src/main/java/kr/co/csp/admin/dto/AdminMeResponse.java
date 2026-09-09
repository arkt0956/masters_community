package kr.co.csp.admin.dto;

import java.time.OffsetDateTime;

/**
 * 로그인 상태 확인 응답.
 *
 * 프론트가 새로고침 후에도 세션이 살아 있는지 확인하는 데 쓴다.
 * 비밀번호 해시는 담지 않는다.
 */
public record AdminMeResponse(String loginId, OffsetDateTime lastLoginAt) {
}
