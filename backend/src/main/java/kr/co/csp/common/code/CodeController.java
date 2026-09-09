package kr.co.csp.common.code;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 공통코드 조회 API (DR-C05 · DR-P07).
 *
 * 왜 조회만 두는가: 사용자 화면이 호출하는 API는 코드를 바꾸지 않는다.
 * 등록·수정은 관리자 인증 경로(/admin/api/codes)에만 있다 (DR-C03).
 */
@RestController
@RequestMapping("/api/codes")
public class CodeController {

    private final CodeRegistry codeRegistry;

    public CodeController(CodeRegistry codeRegistry) {
        this.codeRegistry = codeRegistry;
    }

    /** 활성 코드만 내려준다 (R-23). 프론트는 이 응답으로 드롭다운을 그린다. */
    @GetMapping("/{groupCode}")
    public List<CodeResponse> group(@PathVariable String groupCode) {
        return codeRegistry.getGroup(groupCode).stream().map(CodeResponse::from).toList();
    }

    public record CodeResponse(String codeValue, String codeName, int codeLevel,
                               String parentCode, int sortOrder) {

        static CodeResponse from(CodeItem item) {
            return new CodeResponse(item.codeValue(), item.codeName(), item.codeLevel(),
                    item.parentCode(), item.sortOrder());
        }
    }
}
