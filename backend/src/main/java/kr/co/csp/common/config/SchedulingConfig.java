package kr.co.csp.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄 작업 활성화.
 *
 * 단일 WAS 전제다. 세션 저장소를 두지 않는 것과 같은 전제이므로(application.yml) 분산 락을
 * 두지 않는다. WAS를 여러 대로 늘리면 청소 작업이 동시에 돌아 같은 파일을 지우려 한다.
 * 그 자체로 손실이 나지는 않지만(deleteIfExists), 그때는 이 전제를 다시 봐야 한다.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
