package kr.co.csp.admin.controller;

import jakarta.validation.Valid;
import java.util.List;
import kr.co.csp.admin.dto.AdminExtraSolutionItem;
import kr.co.csp.admin.dto.ExtraSolutionStatusChangeRequest;
import kr.co.csp.admin.service.AdminViewAssembler;
import kr.co.csp.participation.entity.ExtraSolution;
import kr.co.csp.participation.service.ExtraSolutionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 추가풀이 검토 (SCR-A03 · REQ-A02). */
@RestController
@RequestMapping("/admin/api/extra-solutions")
public class AdminExtraSolutionController {

    private final ExtraSolutionService extraSolutionService;
    private final AdminViewAssembler assembler;

    public AdminExtraSolutionController(ExtraSolutionService extraSolutionService,
                                        AdminViewAssembler assembler) {
        this.extraSolutionService = extraSolutionService;
        this.assembler = assembler;
    }

    @GetMapping
    public List<AdminExtraSolutionItem> list(@RequestParam(required = false) String statusCode) {
        return assembler.extraSolutions(extraSolutionService.findForAdmin(statusCode));
    }

    /** 검토 시작 · 게시 · 반려 (SCR-A03 이벤트 1~3). 반려 사유는 선택 입력이다 (R-39). */
    @PatchMapping("/{extraSolutionId}")
    public AdminExtraSolutionItem changeStatus(@PathVariable Long extraSolutionId,
                                               @Valid @RequestBody ExtraSolutionStatusChangeRequest request) {
        ExtraSolution updated = extraSolutionService.changeStatus(
                extraSolutionId, request.statusCode(), request.rejectReason());
        return assembler.extraSolutions(List.of(updated)).get(0);
    }
}
