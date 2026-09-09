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
import kr.co.csp.common.code.SystemCode.RptStatus;
import kr.co.csp.common.exception.DomainException;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 신고 — tb_csp_usr01 (논리명 report, DR-N06).
 *
 * updated_at이 없다. 사용자는 등록만 하고 수정하지 않으며, 관리자의 처리는
 * processed_at으로 기록한다 (설계 3-14). 그래서 Auditable을 상속하지 않는다.
 */
@Entity
@Table(name = "tb_csp_usr01")
@EntityListeners(AuditingEntityListener.class)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /** RPT_TYPE: RPT001 오탈자, RPT002 오답, RPT003 오분류 */
    @Column(name = "type_code", length = 20, nullable = false)
    private String typeCode;

    /** 필수, 길이 제한 없음 (R-29) */
    @Column(name = "contents", nullable = false)
    private String contents;

    @Column(name = "status_code", length = 20, nullable = false)
    private String statusCode = RptStatus.RECEIVED;

    /** 프록시 헤더 기준 요청자 IP (R-52 · DR-S01). 일일 제한 집계에 쓴다 (R-46). */
    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "created_ip", nullable = false, updatable = false)
    private InetAddress createdIp;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    protected Report() {
    }

    public static Report create(Long questionId, String typeCode, String contents, InetAddress createdIp) {
        Report report = new Report();
        report.questionId = questionId;
        report.typeCode = typeCode;
        report.contents = contents;
        report.createdIp = createdIp;
        report.statusCode = RptStatus.RECEIVED;
        return report;
    }

    /**
     * 상태 전이 (SCR-A02 · 설계 3-5).
     *
     * 접수 → 검토중 → 반영 / 기각. 이미 처리된 건은 다시 처리하지 않는다
     * (SCR-A02 예외 표 — "이미 처리된 건 재처리 불가").
     */
    public void changeStatus(String next) {
        if (isClosed()) {
            throw new DomainException("이미 처리된 신고입니다.");
        }
        if (!RptStatus.REVIEWING.equals(next)
                && !RptStatus.APPLIED.equals(next)
                && !RptStatus.REJECTED.equals(next)) {
            throw new DomainException("신고 상태로 바꿀 수 없는 값입니다.");
        }
        this.statusCode = next;
        // 검토중은 아직 처리가 끝난 것이 아니므로 처리일시를 남기지 않는다.
        if (RptStatus.APPLIED.equals(next) || RptStatus.REJECTED.equals(next)) {
            this.processedAt = OffsetDateTime.now();
        }
    }

    public boolean isClosed() {
        return RptStatus.APPLIED.equals(statusCode) || RptStatus.REJECTED.equals(statusCode);
    }

    public Long getReportId() {
        return reportId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getContents() {
        return contents;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }
}
