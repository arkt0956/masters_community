package kr.co.csp.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 요청 IP를 RequestContext에 넣는다 (DR-S01).
 *
 * 왜 X-Forwarded-For를 직접 파싱하지 않는가:
 * 프록시가 여러 단이면 헤더가 쉼표로 이어진 목록이 되고, 어느 항목을 믿을지는
 * 신뢰 프록시 설정에 달려 있다. 직접 파싱하면 외부에서 위조한 값을 그대로 믿게 된다.
 * application.yml의 server.forward-headers-strategy=native와
 * server.tomcat.remoteip.internal-proxies가 이 판단을 대신하므로,
 * 여기서는 getRemoteAddr()의 결과만 받아 쓴다.
 */
@Component
@Order(1)
public class ClientIpFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        RequestContext.setClientIp(RequestContext.parse(request.getRemoteAddr()));
        try {
            chain.doFilter(request, response);
        } finally {
            // 스레드 재사용 때문에 반드시 지운다.
            RequestContext.clear();
        }
    }
}
