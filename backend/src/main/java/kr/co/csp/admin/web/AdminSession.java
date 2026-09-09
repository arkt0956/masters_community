package kr.co.csp.admin.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.co.csp.common.exception.UnauthorizedException;

/**
 * 관리자 세션 (R-55).
 *
 * 왜 서버 세션인가: 만료 시 즉시 차단되어야 한다. JWT는 발급한 토큰을 서버가
 * 회수할 수 없어 만료 전까지 유효하다.
 *
 * 단일 WAS 전제다. 인스턴스를 늘리면 세션 저장소를 외부로 빼야 한다.
 */
public final class AdminSession {

    static final String ATTRIBUTE = "CSP_ADMIN_ID";

    private AdminSession() {
    }

    public static void login(HttpServletRequest request, Long adminId) {
        // 로그인 시 세션을 새로 만든다. 기존 세션 id를 그대로 쓰면 세션 고정 공격에 노출된다.
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        request.getSession(true).setAttribute(ATTRIBUTE, adminId);
    }

    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static Long currentAdminId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object adminId = session == null ? null : session.getAttribute(ATTRIBUTE);
        if (adminId == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return (Long) adminId;
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(ATTRIBUTE) != null;
    }
}
