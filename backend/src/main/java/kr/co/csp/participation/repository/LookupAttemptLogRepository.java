package kr.co.csp.participation.repository;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import kr.co.csp.participation.entity.LookupAttemptLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LookupAttemptLogRepository extends JpaRepository<LookupAttemptLog, Long> {

    /** R-48 — 실패 시도 횟수로 무차별 대입을 막는다. */
    long countByCreatedIpAndSuccessFalseAndCreatedAtAfter(InetAddress createdIp, OffsetDateTime since);

    /** 오래된 로그 정리 (설계 3-4 — 예: 30일). */
    long deleteByCreatedAtBefore(OffsetDateTime before);
}
