package kr.co.csp.participation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 조회 시도 로그 — tb_csp_cmn02 (논리명 lookup_attempt_log, DR-N06).
 *
 * 왜 별도 테이블이 필요한가 (설계 3-4):
 * 등록 제한(R-46)은 신고·추가풀이 테이블의 created_ip + created_at 집계로 충분하다.
 * 하지만 현황 조회 실패는 어느 테이블에도 기록이 남지 않는다. 무차별 대입을 막으려면(R-48)
 * 실패 자체를 기록해야 한다.
 *
 * 성공·실패를 모두 남긴다. 오래된 로그는 주기 삭제한다(예: 30일).
 */
@Entity
@Table(name = "tb_csp_cmn02")
@EntityListeners(AuditingEntityListener.class)
public class LookupAttemptLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "created_ip", nullable = false, updatable = false)
    private InetAddress createdIp;

    @Column(name = "is_success", nullable = false)
    private boolean success;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected LookupAttemptLog() {
    }

    public static LookupAttemptLog of(InetAddress createdIp, boolean success) {
        LookupAttemptLog log = new LookupAttemptLog();
        log.createdIp = createdIp;
        log.success = success;
        return log;
    }

    public Long getLogId() {
        return logId;
    }

    public boolean isSuccess() {
        return success;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
