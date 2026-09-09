package kr.co.csp.participation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 신고 등록 요청 (SCR-005 · POST /api/reports).
 *
 * created_ip는 받지 않는다. 클라이언트가 보낸 IP를 믿으면 제한을 우회할 수 있으므로
 * 서버가 요청에서 직접 얻는다 (DR-S01).
 */
public record ReportCreateRequest(
        @NotNull(message = "대상 문항이 필요합니다.")
        Long questionId,

        @NotBlank(message = "신고 유형을 선택해 주세요.")
        String typeCode,

        // R-29 — 필수, 길이 제한 없음. 미입력만 제어한다.
        @NotBlank(message = "어느 부분이 잘못됐는지 적어 주세요.")
        String contents
) {
}
