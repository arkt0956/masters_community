package kr.co.csp.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화.
 *
 * 왜 필요한가: 이 어노테이션이 없으면 Auditable의 @LastModifiedDate가 동작하지 않는다.
 * updated_at이 갱신되지 않아도 오류가 나지 않으므로 빠뜨리면 알아채기 어렵다 (DR-L02).
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
