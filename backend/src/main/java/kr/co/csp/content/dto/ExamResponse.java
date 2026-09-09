package kr.co.csp.content.dto;

import java.util.List;

/**
 * 회차 응답 (SCR-002 ①②).
 *
 * 교시는 별도 테이블이 아니라 문항의 session_no 집계다 (설계 3-2).
 * questionCount가 0인 교시는 아예 담기지 않으므로 화면은 받은 목록만 그리면 된다 (R-03).
 */
public record ExamResponse(
        Long examId,
        int examRound,
        List<SessionSummary> sessions
) {

    public record SessionSummary(int sessionNo, long questionCount) {
    }
}
