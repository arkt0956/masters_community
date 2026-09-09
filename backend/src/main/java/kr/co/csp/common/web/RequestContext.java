package kr.co.csp.common.web;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 현재 요청의 클라이언트 IP를 담아 두는 자리 (DR-S01 · DR-L02).
 *
 * 왜 ThreadLocal인가: Auditable의 @PreUpdate는 JPA가 부르는 콜백이라 인자를 받을 수 없다.
 * Service까지 IP를 인자로 내려도 엔티티 리스너에는 닿지 않는다.
 *
 * 주의: 넣었으면 반드시 지운다. 톰캣이 스레드를 재사용하므로 남겨 두면
 * 다음 요청이 앞 요청의 IP를 기록한다. ClientIpFilter의 finally가 그 역할을 한다.
 */
public final class RequestContext {

    private static final ThreadLocal<InetAddress> CLIENT_IP = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void setClientIp(InetAddress ip) {
        CLIENT_IP.set(ip);
    }

    public static void clear() {
        CLIENT_IP.remove();
    }

    /**
     * 요청 밖(배치·테스트)에서 호출되면 루프백을 돌려준다.
     * 왜 예외를 던지지 않는가: updated_ip는 NOT NULL이다. 여기서 터지면
     * 추적 정보를 남기려다 저장 자체를 막는 셈이 된다.
     */
    public static InetAddress clientIp() {
        InetAddress ip = CLIENT_IP.get();
        return ip != null ? ip : loopback();
    }

    public static InetAddress parse(String raw) {
        try {
            return InetAddress.getByName(raw);
        } catch (UnknownHostException e) {
            return loopback();
        }
    }

    private static InetAddress loopback() {
        return InetAddress.getLoopbackAddress();
    }
}
