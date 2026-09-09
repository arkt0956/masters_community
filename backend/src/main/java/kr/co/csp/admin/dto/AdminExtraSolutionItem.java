package kr.co.csp.admin.dto;

import java.time.OffsetDateTime;

/**
 * 추가풀이 검토 목록·상세 (SCR-A03 ②③).
 *
 * 비밀번호는 조회 키일 뿐이므로 관리자 화면에 노출하지 않는다 (SCR-A03 비고).
 * 해시도 담지 않는다.
 */
public record AdminExtraSolutionItem(
        Long extraSolutionId,
        Long questionId,
        String questionTitle,
        String sourceLabel,
        String userName,
        String contents,
        String statusCode,
        String statusName,
        String rejectReason,
        OffsetDateTime createdAt,
        OffsetDateTime processedAt
) {
}
