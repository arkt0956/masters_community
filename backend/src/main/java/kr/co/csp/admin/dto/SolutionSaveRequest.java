package kr.co.csp.admin.dto;

import jakarta.validation.constraints.NotBlank;

/** 해설 저장 요청 (SCR-A04 ③). 문항당 1건이므로 upsert다. */
public record SolutionSaveRequest(
        @NotBlank(message = "해설 본문을 입력해 주세요.") String contents
) {
}
