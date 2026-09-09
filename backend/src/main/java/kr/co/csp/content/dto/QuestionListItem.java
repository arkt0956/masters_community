package kr.co.csp.content.dto;

import java.util.List;

/**
 * 문항 목록 항목 (SCR-004 진입 시 응답).
 *
 * 왜 DTO를 분리하는가 (DR-A01):
 * 엔티티를 그대로 반환하면 지연 로딩으로 해설이 딸려 나갈 수 있다.
 * 이 레코드에는 해설 필드가 아예 없으므로 서비스가 실수해도 여기서 잘린다.
 *
 * 문제 본문(contents)과 문제 도면(drawings)은 목록에 포함한다. 화면정의서 SCR-004의
 * 데이터 연동 표가 진입 시점에 본문·문제 도면까지 받는 것으로 정하고 있다.
 * 목록에서 빠지는 것은 해설(tb_csp_con03)과 답안 도면뿐이다.
 *
 * hasSolution은 "해설보기" 버튼의 활성 여부를 정하는 값이다. 본문이 아니라
 * 존재 여부만 담으므로 해설이 새어 나가지 않는다.
 */
public record QuestionListItem(
        Long questionId,
        int examRound,
        int sessionNo,
        int questionNo,
        String categoryCode,
        String categoryName,
        String title,
        String contents,
        List<DrawingResponse> drawings,
        boolean hasSolution
) {
}
