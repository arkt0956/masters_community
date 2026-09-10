package kr.co.csp.admin.dto;

import java.time.OffsetDateTime;
import java.util.List;
import kr.co.csp.content.dto.DrawingResponse;

/**
 * 추가풀이 검토 목록·상세 (SCR-A03 ②③).
 *
 * 비밀번호는 조회 키일 뿐이므로 관리자 화면에 노출하지 않는다 (SCR-A03 비고).
 * 해시도 담지 않는다.
 *
 * drawings는 사용자가 첨부한 이미지다 (DR-F03). 검토 화면에서 보이지 않으면
 * 관리자가 내용을 확인하지 않은 채 게시하게 된다.
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
        OffsetDateTime processedAt,
        List<DrawingResponse> drawings
) {
}
