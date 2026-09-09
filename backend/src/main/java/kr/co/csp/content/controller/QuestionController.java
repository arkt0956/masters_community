package kr.co.csp.content.controller;

import java.util.List;
import kr.co.csp.content.dto.QuestionListItem;
import kr.co.csp.content.dto.SolutionResponse;
import kr.co.csp.content.service.QuestionQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 문항 API (SCR-004).
 *
 * 반환 타입이 엔티티(Question)가 되지 않도록 주의한다. 엔티티를 그대로 반환하면
 * 지연 로딩으로 해설이 딸려 나갈 수 있다 (DR-A01 · DR-P01).
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionQueryService questionQueryService;

    public QuestionController(QuestionQueryService questionQueryService) {
        this.questionQueryService = questionQueryService;
    }

    /** 랜덤 세트 (SCR-001 ④). 매 호출마다 순서가 달라진다 — "다시 섞기"가 재호출한다. */
    @GetMapping("/random")
    public List<QuestionListItem> random() {
        return questionQueryService.findRandom();
    }

    /**
     * 해설 (R-09 · SCR-004 ⑤).
     *
     * 왜 별도 API인가: 목록 응답에 해설이 섞이면 클라이언트에서 숨겨도
     * 개발자 도구로 볼 수 있다. 서버가 아예 보내지 않아야 한다 (DR-A01).
     */
    @GetMapping("/{questionId}/solution")
    public SolutionResponse solution(@PathVariable Long questionId) {
        return questionQueryService.findSolution(questionId);
    }
}
