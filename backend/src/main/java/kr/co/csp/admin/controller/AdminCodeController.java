package kr.co.csp.admin.controller;

import jakarta.validation.Valid;
import java.util.List;
import kr.co.csp.admin.dto.CodeSaveRequest;
import kr.co.csp.admin.dto.CodeUpdateRequest;
import kr.co.csp.admin.service.CommonCodeAdminService;
import kr.co.csp.common.code.CodeItem;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 공통코드 관리 (DR-C03 · DR-C05).
 *
 * 왜 관리자 경로에만 두는가: 사용자 화면이 호출하는 /api/codes는 조회만 한다.
 * 등록·수정 API가 사용자 경로에 있으면 인증 없이 코드를 늘릴 수 있게 된다.
 */
@RestController
@RequestMapping("/admin/api/codes")
public class AdminCodeController {

    private final CommonCodeAdminService commonCodeAdminService;

    public AdminCodeController(CommonCodeAdminService commonCodeAdminService) {
        this.commonCodeAdminService = commonCodeAdminService;
    }

    /** 비활성 코드도 포함한다. 관리자는 다시 활성화할 수 있어야 한다. */
    @GetMapping("/{groupCode}")
    public List<CodeItem> group(@PathVariable String groupCode) {
        return commonCodeAdminService.findGroup(groupCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody CodeSaveRequest request) {
        commonCodeAdminService.create(request.groupCode(), request.codeValue(), request.codeName(),
                request.codeLevel(), request.parentCode(), request.sortOrder());
    }

    /**
     * 코드명·정렬 순서·활성 여부 수정.
     *
     * 코드값은 바꿀 수 없다. 시스템 코드는 코드명·정렬 순서만 바뀌고
     * 비활성화 시도는 예외가 된다 (DR-C05).
     *
     * 서비스 호출은 한 번이다. 두 번 나누면 트랜잭션이 갈려 일부만 반영될 수 있다.
     * 업무 흐름을 컨트롤러에서 조합하지 않는다 (DR-P01).
     */
    @PatchMapping("/{groupCode}/{codeValue}")
    public void update(@PathVariable String groupCode, @PathVariable String codeValue,
                       @Valid @RequestBody CodeUpdateRequest request) {
        commonCodeAdminService.update(groupCode, codeValue, request.codeName(),
                request.sortOrder(), request.active());
    }

    @DeleteMapping("/{groupCode}/{codeValue}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String groupCode, @PathVariable String codeValue) {
        commonCodeAdminService.delete(groupCode, codeValue);
    }
}
