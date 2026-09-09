package kr.co.csp.participation.dto;

import java.time.OffsetDateTime;
import java.util.List;
import kr.co.csp.content.dto.DrawingResponse;

/**
 * 문항에 붙어 나가는 게시된 추가풀이 (SCR-004).
 *
 * 게시(EXS003) 상태만 나간다. 검토대기·검토중·반려 건은 작성자 본인도
 * 현황 조회(SCR-007)에서만 볼 수 있다.
 */
public record ExtraSolutionResponse(
        Long extraSolutionId,
        String userName,
        String contents,
        OffsetDateTime createdAt,
        List<DrawingResponse> drawings
) {
}
