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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
     *
     * 왜 멀티파트인가: 본문과 이미지를 한 요청으로 받아 한 트랜잭션에서 저장한다 (DR-F03).
     * 등록과 업로드를 나누면 그 사이에 남의 추가풀이에 파일을 붙이는 요청을 막을 수 없다.
     *
     * data 파트는 JSON이다. @RequestPart로 받아야 @Valid가 record에 그대로 걸린다.
     * files는 없을 수 있다 — 이미지 없는 등록이 기본 경로다.
     */
    @PostMapping(value = "/extra-solutions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestPart("data") ExtraSolutionCreateRequest request,
                       @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        extraSolutionService.create(request, files, RequestContext.clientIp());
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
