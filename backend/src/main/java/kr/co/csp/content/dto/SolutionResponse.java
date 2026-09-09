package kr.co.csp.content.dto;

import java.util.List;

/**
 * 해설 응답 (SCR-004 ⑤ 클릭 시 · GET /api/questions/{id}/solution).
 *
 * 해설 본문은 이 응답에만 있다. 목록 응답에는 없다 (R-09 · R-54 · DR-A01).
 * 프론트는 최초 1회만 받아 두고 이후에는 받아둔 값을 쓴다 (R-05, 안건 7).
 */
public record SolutionResponse(
        Long solutionId,
        String contents,
        List<DrawingResponse> drawings
) {
}
