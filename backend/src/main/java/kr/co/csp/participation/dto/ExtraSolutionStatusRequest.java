package kr.co.csp.participation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 추가풀이 현황 조회 요청 (SCR-007 · POST /api/extra-solutions/status).
 *
 * 왜 POST인가: ID/비밀번호를 URL 쿼리에 노출하지 않기 위해서다. 쿼리 문자열은
 * 브라우저 기록·프록시 로그·Referer에 남는다.
 */
public record ExtraSolutionStatusRequest(
        @NotBlank(message = "ID와 비밀번호를 모두 입력해 주세요.")
        String userName,

        @NotBlank(message = "ID와 비밀번호를 모두 입력해 주세요.")
        String password
) {
}
