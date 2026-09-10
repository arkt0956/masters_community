package kr.co.csp.common.config;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화.
 *
 * 왜 필요한가: 이 어노테이션이 없으면 Auditable의 @LastModifiedDate가 동작하지 않는다.
 * updated_at이 갱신되지 않아도 오류가 나지 않으므로 빠뜨리면 알아채기 어렵다 (DR-L02).
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaConfig {

    /**
     * 감사 시각 제공자.
     *
     * 기본 제공자(CurrentDateTimeProvider)는 LocalDateTime을 돌려준다. Auditable의
     * created_at·updated_at은 OffsetDateTime이므로(TIMESTAMPTZ, 표준 컬럼 사전) 변환에
     * 실패하고 저장이 통째로 막힌다. 조회는 멀쩡해서 쓰기를 시도할 때까지 드러나지 않는다.
     *
     * 오프셋은 JVM 기본 시간대를 따른다. jdbc.time_zone이 Asia/Seoul로 맞춰져 있다.
     */
    @Bean
    DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
