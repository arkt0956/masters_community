package kr.co.csp.content.controller;

import java.util.List;
import kr.co.csp.content.dto.ExamResponse;
import kr.co.csp.content.dto.QuestionListItem;
import kr.co.csp.content.service.ExamService;
import kr.co.csp.content.service.QuestionQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 회차 API (SCR-001 · SCR-002). 경로는 논리명을 쓴다 (DR-N06). */
@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;
    private final QuestionQueryService questionQueryService;

    public ExamController(ExamService examService, QuestionQueryService questionQueryService) {
        this.examService = examService;
        this.questionQueryService = questionQueryService;
    }

    /**
     * 공개 회차와 교시별 문항 수 (R-01 · R-03).
     * SCR-001은 이 응답이 비어 있으면 "회차별·과목별·랜덤" 버튼을 비활성화한다.
     */
    @GetMapping
    public List<ExamResponse> exams() {
        return examService.findPublicExams();
    }

    /** 회차·교시 문항 목록 (SCR-004 진입). 해설은 포함하지 않는다 (DR-A01). */
    @GetMapping("/{examId}/questions")
    public List<QuestionListItem> questions(@PathVariable Long examId,
                                            @RequestParam int sessionNo) {
        return questionQueryService.findByExamSession(examId, sessionNo);
    }
}
