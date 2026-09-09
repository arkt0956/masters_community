package kr.co.csp.admin.dto;

import java.util.List;
import kr.co.csp.content.dto.DrawingResponse;

/**
 * 관리자 문항 상세 (SCR-A04 ③ 작성·수정 폼).
 *
 * 관리자 화면은 해설을 함께 받는다. DR-A01이 막는 것은 사용자 목록 응답이며,
 * 여기는 인증된 관리자만 접근하는 편집 화면이다 (R-20).
 */
public record AdminQuestionDetail(
        Long questionId,
        Long examId,
        int examRound,
        int sessionNo,
        int questionNo,
        String categoryCode,
        String categoryName,
        String title,
        String contents,
        String statusCode,
        String statusName,
        List<DrawingResponse> drawings,
        SolutionPart solution
) {

    /** 해설이 없으면 null이다 (문항과 1:0..1). */
    public record SolutionPart(Long solutionId, String contents, List<DrawingResponse> drawings) {
    }
}
