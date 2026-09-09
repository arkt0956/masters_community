package kr.co.csp.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 문항 작성·수정 요청 (SCR-A04 ③).
 *
 * status_code는 받지 않는다. 상태 전이는 별도 API(게시·작성중 전환)로만 한다.
 * 저장 요청이 상태까지 바꿀 수 있으면 실수로 게시되는 경로가 생긴다.
 */
public record QuestionSaveRequest(
        @NotNull(message = "회차를 선택해 주세요.") Long examId,
        @Min(value = 1, message = "교시는 1 이상이어야 합니다.") int sessionNo,
        @Min(value = 1, message = "문항 번호는 1 이상이어야 합니다.") int questionNo,
        @NotBlank(message = "과목을 선택해 주세요.") String categoryCode,
        @NotBlank(message = "제목을 입력해 주세요.") String title,
        @NotBlank(message = "문제 본문을 입력해 주세요.") String contents
) {
}
