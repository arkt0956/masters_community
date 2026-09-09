package kr.co.csp.admin.dto;

import jakarta.validation.constraints.NotBlank;

/** 관리자 로그인 요청 (SCR-A05 · POST /admin/api/login). */
public record AdminLoginRequest(
        @NotBlank(message = "아이디를 입력해 주세요.") String loginId,
        @NotBlank(message = "비밀번호를 입력해 주세요.") String password
) {
}
