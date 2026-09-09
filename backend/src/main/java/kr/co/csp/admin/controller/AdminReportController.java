package kr.co.csp.admin.controller;

import jakarta.validation.Valid;
import java.util.List;
import kr.co.csp.admin.dto.AdminReportItem;
import kr.co.csp.admin.dto.ReportStatusChangeRequest;
import kr.co.csp.admin.service.AdminViewAssembler;
import kr.co.csp.participation.entity.Report;
import kr.co.csp.participation.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 신고 처리 (SCR-A02 · REQ-A01). */
@RestController
@RequestMapping("/admin/api/reports")
public class AdminReportController {

    private final ReportService reportService;
    private final AdminViewAssembler assembler;

    public AdminReportController(ReportService reportService, AdminViewAssembler assembler) {
        this.reportService = reportService;
        this.assembler = assembler;
    }

    /** statusCode를 비우면 전체다 (SCR-A02 ① 상태 필터의 "전체" 탭). */
    @GetMapping
    public List<AdminReportItem> reports(@RequestParam(required = false) String statusCode) {
        return assembler.reports(reportService.findForAdmin(statusCode));
    }

    /**
     * 상태 전이 (SCR-A02 ④).
     *
     * 반영은 원문 수정 게시로 이어지지만, 문항 수정 자체는 SCR-A04에서 한다.
     * 여기서 문항까지 건드리면 어느 화면이 무엇을 바꾸는지 흐려진다.
     */
    @PatchMapping("/{reportId}")
    public AdminReportItem changeStatus(@PathVariable Long reportId,
                                        @Valid @RequestBody ReportStatusChangeRequest request) {
        Report report = reportService.changeStatus(reportId, request.statusCode());
        return assembler.reports(List.of(report)).get(0);
    }
}
