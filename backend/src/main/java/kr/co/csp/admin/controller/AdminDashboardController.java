package kr.co.csp.admin.controller;

import kr.co.csp.admin.dto.DashboardResponse;
import kr.co.csp.content.service.ContentAdminService;
import kr.co.csp.participation.service.ExtraSolutionService;
import kr.co.csp.participation.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 대시보드 (SCR-A01). */
@RestController
@RequestMapping("/admin/api/dashboard")
public class AdminDashboardController {

    private final ReportService reportService;
    private final ExtraSolutionService extraSolutionService;
    private final ContentAdminService contentAdminService;

    public AdminDashboardController(ReportService reportService,
                                    ExtraSolutionService extraSolutionService,
                                    ContentAdminService contentAdminService) {
        this.reportService = reportService;
        this.extraSolutionService = extraSolutionService;
        this.contentAdminService = contentAdminService;
    }

    /** 대기 0건이어도 진입은 허용한다. 화면은 0을 그대로 표시한다 (SCR-A01 예외 표). */
    @GetMapping
    public DashboardResponse dashboard() {
        return new DashboardResponse(
                reportService.countPending(),
                extraSolutionService.countPending(),
                contentAdminService.countDrafts());
    }
}
