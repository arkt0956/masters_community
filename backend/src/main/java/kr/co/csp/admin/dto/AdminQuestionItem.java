package kr.co.csp.admin.dto;

/**
 * 관리자 문항 목록 (SCR-A04 ①②).
 *
 * 본문은 담지 않는다. 목록에서는 회차·교시·과목·상태·해설 보유 여부만 보면 된다.
 * 본문은 상세 조회에서 받는다.
 */
public record AdminQuestionItem(
        Long questionId,
        Long examId,
        int examRound,
        int sessionNo,
        int questionNo,
        String categoryCode,
        String categoryName,
        String title,
        String statusCode,
        String statusName,
        boolean hasSolution
) {
}
