package kr.co.csp.participation.dto;

import java.time.OffsetDateTime;

/**
 * 현황 조회 결과 한 건 (SCR-007 ④).
 *
 * 비밀번호 해시는 담지 않는다. 반려 사유는 있으면 표시한다 (R-39).
 * 상태 라벨은 codeName을 함께 내려준다. 프론트가 코드값을 하드코딩하지 않게 하기 위해서다 (DR-P07).
 */
public record ExtraSolutionStatusItem(
        Long extraSolutionId,
        Long questionId,
        String questionTitle,
        String sourceLabel,
        OffsetDateTime createdAt,
        String statusCode,
        String statusName,
        String rejectReason
) {
}
