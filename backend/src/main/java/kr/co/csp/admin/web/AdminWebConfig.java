package kr.co.csp.admin.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 관리자 API 인증 인터셉터 등록 (R-20). */
@Configuration
public class AdminWebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;

    public AdminWebConfig(AdminAuthInterceptor adminAuthInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/api/**")
                // 로그인 자체는 인증 대상이 아니다. 로그아웃은 미인증이어도 무해하다.
                .excludePathPatterns("/admin/api/login", "/admin/api/logout");
    }
}
