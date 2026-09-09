package kr.co.csp.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 서비스 설정값.
 *
 * 왜 설정으로 빼는가: 일일 제한 횟수(R-46·R-48)는 운영 중 조정될 수 있는 값이다.
 * 상수로 두면 조정할 때마다 재배포가 필요하다.
 */
@ConfigurationProperties(prefix = "csp")
public record CspProperties(String drawingDir, Limit limit) {

    public record Limit(
            /** R-46 — 신고 IP 일일 10회 */
            int reportPerDay,
            /** R-46 — 추가풀이 IP 일일 5회 */
            int extraSolutionPerDay,
            /** R-48 — 현황 조회 실패 IP 일일 제한 (무차별 대입 방지) */
            int lookupFailPerDay
    ) { }
}
