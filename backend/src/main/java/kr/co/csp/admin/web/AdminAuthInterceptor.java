package kr.co.csp.admin.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.csp.common.exception.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * /admin/** 전체 인증 (R-20 · R-49).
 *
 * 숨김 URL은 부가 조치일 뿐이며 실제 방어선은 인증이다. 경로를 아는 사람이
 * 직접 API를 호출해도 여기서 막힌다.
 *
 * 로그인 API만 예외다. WebConfig에서 제외 경로로 등록한다.
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!AdminSession.isAuthenticated(request)) {
            // 사유를 구분해 알리지 않는다. 미인증인지 만료인지 구분하면
            // 세션 상태를 탐색하는 단서가 된다 (SCR-A05 예외 표).
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return true;
    }
}
