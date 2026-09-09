package kr.co.csp.participation.controller;

import jakarta.validation.Valid;
import kr.co.csp.common.web.RequestContext;
import kr.co.csp.participation.dto.ReportCreateRequest;
import kr.co.csp.participation.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 신고 API (SCR-005). */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 신고 접수.
     *
     * 왜 응답에 아무것도 담지 않는가: 처리 결과는 개별 통지하지 않는다 (SCR-005 비고).
     * 신고 id를 돌려주면 조회할 수 있다는 오해를 준다.
     *
     * IP는 Controller에서 꺼내 Service에 인자로 넘긴다. Service는 HTTP 개념을 모른다 (DR-P01).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody ReportCreateRequest request) {
        reportService.create(request, RequestContext.clientIp());
    }
}
