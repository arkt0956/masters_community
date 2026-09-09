package kr.co.csp.content.controller;

import java.nio.file.Path;
import java.util.UUID;
import kr.co.csp.content.entity.Drawing;
import kr.co.csp.content.service.DrawingStorage;
import kr.co.csp.content.service.QuestionQueryService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 도면 파일 서빙 (DR-F02 · R-53). */
@RestController
@RequestMapping("/api/drawings")
public class DrawingController {

    private final QuestionQueryService questionQueryService;
    private final DrawingStorage storage;

    public DrawingController(QuestionQueryService questionQueryService, DrawingStorage storage) {
        this.questionQueryService = questionQueryService;
        this.storage = storage;
    }

    /**
     * 경로에 원본 파일명이 아니라 file_uuid를 쓴다. UUID는 추측이 불가능하다 (R-53).
     *
     * Content-Type은 file_type 값을 쓴다. 저장 파일에 확장자가 없으므로 이 값이 없으면
     * 브라우저가 이미지를 렌더링하지 않고 다운로드한다 (DR-F02).
     *
     * SVG는 텍스트라 스크립트를 담을 수 있다. CSP로 스크립트 실행을 막는다.
     * 이미지 바이트는 등록 시점 원본 그대로다. 재인코딩하지 않는다 (R-16).
     */
    @GetMapping("/{fileUuid}")
    public ResponseEntity<Resource> serve(@PathVariable UUID fileUuid) {
        Drawing drawing = questionQueryService.findDrawingByFileUuid(fileUuid);
        Path path = storage.path(fileUuid);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(drawing.getFileType()))
                .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox")
                .header("X-Content-Type-Options", "nosniff")
                // 파일 내용은 바뀌지 않는다. UUID가 바뀌면 URL도 바뀌므로 길게 캐시해도 안전하다.
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(365)).cachePublic().immutable())
                .body(new FileSystemResource(path));
    }
}
