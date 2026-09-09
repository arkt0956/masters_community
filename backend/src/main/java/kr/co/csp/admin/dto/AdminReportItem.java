package kr.co.csp.admin.dto;

import java.time.OffsetDateTime;

/**
 * 신고 목록·상세 (SCR-A02 ②③).
 *
 * created_ip는 담지 않는다. 화면이 쓰지 않고, IP는 제한 집계와 사후 추적용이다.
 * 상태·유형은 코드값과 이름을 함께 내려준다 (DR-P07).
 */
public record AdminReportItem(
        Long reportId,
        Long questionId,
        String questionTitle,
        String sourceLabel,
        String typeCode,
        String typeName,
        String contents,
        String statusCode,
        String statusName,
        OffsetDateTime createdAt,
        OffsetDateTime processedAt
) {
}
