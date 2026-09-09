package kr.co.csp.admin.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.co.csp.admin.dto.AdminLoginRequest;
import kr.co.csp.admin.dto.AdminMeResponse;
import kr.co.csp.admin.entity.AdminAccount;
import kr.co.csp.admin.service.AdminAuthService;
import kr.co.csp.admin.web.AdminSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 인증 API (SCR-A00 · SCR-A05).
 *
 * 숨김 URL은 부가 조치일 뿐이며 실제 방어선은 인증이다 (R-49).
 * /admin/api/** 전체가 AdminAuthInterceptor의 대상이고, 이 컨트롤러의 login·logout만 예외다.
 */
@RestController
@RequestMapping("/admin/api")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * 로그인 (SCR-A05 ③).
     *
     * HttpServletRequest를 여기서만 다룬다. Service에는 넘기지 않는다 (DR-P01).
     */
    @PostMapping("/login")
    public AdminMeResponse login(@Valid @RequestBody AdminLoginRequest request,
                                 HttpServletRequest servletRequest) {
        AdminAccount account = adminAuthService.login(request.loginId(), request.password());
        AdminSession.login(servletRequest, account.getAdminId());
        return new AdminMeResponse(account.getLoginId(), account.getLastLoginAt());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest servletRequest) {
        AdminSession.logout(servletRequest);
    }

    /** 새로고침 후 세션이 살아 있는지 확인한다. 만료됐으면 401이 나가고 화면이 로그인으로 보낸다. */
    @GetMapping("/me")
    public AdminMeResponse me(HttpServletRequest servletRequest) {
        AdminAccount account = adminAuthService.findById(AdminSession.currentAdminId(servletRequest));
        return new AdminMeResponse(account.getLoginId(), account.getLastLoginAt());
    }
}
