package kr.co.csp.admin.dto;

/**
 * 대시보드 (SCR-A01).
 *
 * 신고는 접수됨 + 검토중 합계, 추가풀이는 검토대기 + 검토중 합계다 (설계 3-5).
 * 대기 0건이어도 진입은 허용하므로 화면은 0을 그대로 표시한다.
 */
public record DashboardResponse(
        long reportPending,
        long extraSolutionPending,
        long questionDraft
) {
}
