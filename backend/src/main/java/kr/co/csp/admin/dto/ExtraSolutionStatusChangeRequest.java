package kr.co.csp.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 추가풀이 상태 전이 요청 (ES_STATUS).
 *
 * rejectReason은 반려(EXS004)에만 쓰며 필수가 아니다. 입력하면 작성자가
 * 현황 조회에서 볼 수 있다 (R-39).
 */
public record ExtraSolutionStatusChangeRequest(
        @NotBlank(message = "변경할 상태를 지정해 주세요.") String statusCode,
        String rejectReason
) {
}
