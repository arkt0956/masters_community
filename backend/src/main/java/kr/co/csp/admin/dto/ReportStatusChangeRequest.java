package kr.co.csp.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 신고 상태 전이 요청 (RPT_STATUS).
 *
 * 왜 추가풀이와 DTO를 나누는가:
 * 신고에는 반려 사유가 없다. tb_csp_usr01에 reject_reason 컬럼 자체가 없고
 * 처리 결과는 개별 통지하지 않는다. 두 리소스가 하나의 DTO를 공유하면
 * 신고에 사유를 담아 보내도 200이 돌아오고 값은 조용히 사라진다.
 * 받지 않을 값은 타입에 두지 않는다.
 */
public record ReportStatusChangeRequest(
        @NotBlank(message = "변경할 상태를 지정해 주세요.") String statusCode
) {
}
