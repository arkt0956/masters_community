package kr.co.csp.participation.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * IP 일일 제한의 "하루" 기준 (R-46 · R-48).
 *
 * 왜 24시간 슬라이딩이 아니라 날짜 경계인가:
 * 화면이 "오늘은 더 등록할 수 없습니다"라고 안내한다 (SCR-005 · SCR-006).
 * 슬라이딩 창이면 언제 풀리는지 사용자가 알 수 없다.
 *
 * 기준 시간대는 서비스 이용자 기준인 한국 시간이다. UTC로 자르면
 * 한국의 오전 9시에 카운트가 초기화된다.
 */
public final class DailyLimit {

    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private DailyLimit() {
    }

    /** 오늘 00:00 (KST). 이 시각 이후 건수를 센다. */
    public static OffsetDateTime todayStart() {
        return LocalDate.now(SERVICE_ZONE).atStartOfDay(SERVICE_ZONE).toOffsetDateTime();
    }
}
