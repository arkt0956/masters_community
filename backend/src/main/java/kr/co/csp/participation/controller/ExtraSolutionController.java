package kr.co.csp.participation.controller;

import jakarta.validation.Valid;
import java.util.List;
import kr.co.csp.common.web.RequestContext;
import kr.co.csp.participation.dto.ExtraSolutionCreateRequest;
import kr.co.csp.participation.dto.ExtraSolutionResponse;
import kr.co.csp.participation.dto.ExtraSolutionStatusItem;
import kr.co.csp.participation.dto.ExtraSolutionStatusRequest;
import kr.co.csp.participation.service.ExtraSolutionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 추가풀이 API (SCR-006 · SCR-007). */
@RestController
@RequestMapping("/api")
public class ExtraSolutionController {

    private final ExtraSolutionService extraSolutionService;

    public ExtraSolutionController(ExtraSolutionService extraSolutionService) {
        this.extraSolutionService = extraSolutionService;
    }

    /**
     * 추가풀이 등록 (SCR-006 ④).
     *
     * 응답 본문이 없다. 게시 여부는 SCR-007 현황 조회로만 확인한다 (안건 1 확정).
     * 등록 완료 화면이 조회 방법과 ID·비밀번호 보관 필요성을 안내한다.
     */
    @PostMapping("/extra-solutions")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody ExtraSolutionCreateRequest request) {
        extraSolutionService.create(request, RequestContext.clientIp());
    }

    /**
     * 현황 조회 (SCR-007 ③).
     *
     * 왜 POST인가: ID/비밀번호를 URL 쿼리에 노출하지 않기 위해서다.
     */
    @PostMapping("/extra-solutions/status")
    public List<ExtraSolutionStatusItem> status(@Valid @RequestBody ExtraSolutionStatusRequest request) {
        return extraSolutionService.findStatus(
                request.userName(), request.password(), RequestContext.clientIp());
    }

    /** 문항에 붙은 게시된 추가풀이 (SCR-004). 게시 상태만 나간다. */
    @GetMapping("/questions/{questionId}/extra-solutions")
    public List<ExtraSolutionResponse> byQuestion(@PathVariable Long questionId) {
        return extraSolutionService.findPublishedByQuestion(questionId);
    }
}
