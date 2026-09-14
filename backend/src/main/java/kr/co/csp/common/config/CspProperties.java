package kr.co.csp.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 서비스 설정값.
 *
 * 왜 설정으로 빼는가: 일일 제한 횟수(R-46·R-48)는 운영 중 조정될 수 있는 값이다.
 * 상수로 두면 조정할 때마다 재배포가 필요하다.
 */
@ConfigurationProperties(prefix = "csp")
public record CspProperties(String drawingDir, Limit limit, Retention retention) {

    public record Limit(
            /** R-46 — 신고 IP 일일 10회 */
            int reportPerDay,
            /** R-46 — 추가풀이 IP 일일 5회 */
            int extraSolutionPerDay,
            /** R-48 — 현황 조회 실패 IP 일일 제한 (무차별 대입 방지) */
            int lookupFailPerDay,
            /**
             * 추가풀이 1건에 첨부할 수 있는 이미지 수 (DR-F03).
             *
             * 등록 건수 제한(extraSolutionPerDay)과 곱해진 값이 IP당 하루 상한이 된다.
             * 별도 카운터를 두지 않는 이유는 파일이 추가풀이에 딸린 것이기 때문이다.
             */
            int extraSolutionFiles
    ) { }

    /** 보관 기간 (DR-F02). 운영 중 조정될 수 있어 설정으로 둔다. */
    public record Retention(
            /** 반려된 추가풀이를 지우기까지의 일수. 반려는 종료 상태라 되돌릴 수 없다 */
            int rejectedDays,
            /**
             * 고아 파일로 판정하기까지의 시간.
             *
             * 방금 업로드되어 아직 커밋되지 않은 파일은 DB에서 찾을 수 없어 고아로 보인다.
             * 이 시간이 지난 파일만 대상으로 해야 살아있는 파일을 지우지 않는다.
             */
            int orphanFileHours
    ) { }
}
