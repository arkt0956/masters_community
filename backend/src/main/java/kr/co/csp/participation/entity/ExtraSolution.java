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
import kr.co.csp.common.code.SystemCode.EsStatus;
import kr.co.csp.common.exception.DomainException;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 추가풀이 — tb_csp_usr02 (논리명 extra_solution, DR-N06).
 *
 * 사용자가 등록하고 관리자가 검토한 뒤 게시한다. 등록 후 사용자가 수정·삭제할 수 없다
 * (SCR-006 ③). 그래서 updated_at이 없고 processed_at만 있다.
 */
@Entity
@Table(name = "tb_csp_usr02")
@EntityListeners(AuditingEntityListener.class)
public class ExtraSolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "extra_solution_id")
    private Long extraSolutionId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /**
     * 작성자 닉네임 2~12자. 회원 계정이 아니며 중복을 허용한다.
     *
     * 왜 author_id가 아닌가: _id 접미어는 PK·FK에만 쓴다. 참조 관계가 아닌
     * 문자열 식별자에 붙이면 FK로 오인된다 (DR-N02).
     */
    @Column(name = "user_name", length = 12, nullable = false)
    private String userName;

    /** 현황 조회 키 해시 (bcrypt). 평문 저장 금지 (R-47 · DR-S02). */
    @Column(name = "password_hash", length = 72, nullable = false)
    private String passwordHash;

    /** 이미지 위치는 [[drawing:n]] 토큰 (DR-F03). */
    @Column(name = "contents", nullable = false)
    private String contents;

    @Column(name = "status_code", length = 20, nullable = false)
    private String statusCode = EsStatus.WAITING;

    /** 반려 사유. 필수 아님. 있으면 SCR-007에 표시한다 (R-39). */
    @Column(name = "reject_reason")
    private String rejectReason;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "created_ip", nullable = false, updatable = false)
    private InetAddress createdIp;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    protected ExtraSolution() {
    }

    public static ExtraSolution create(Long questionId, String userName, String passwordHash,
                                       String contents, InetAddress createdIp) {
        ExtraSolution extra = new ExtraSolution();
        extra.questionId = questionId;
        extra.userName = userName;
        extra.passwordHash = passwordHash;
        extra.contents = contents;
        extra.createdIp = createdIp;
        extra.statusCode = EsStatus.WAITING;
        return extra;
    }

    /** 검토대기 → 검토중 (SCR-A03 이벤트 1). */
    public void startReview() {
        if (!EsStatus.WAITING.equals(statusCode)) {
            throw new DomainException("검토대기 상태에서만 검토를 시작할 수 있습니다.");
        }
        this.statusCode = EsStatus.REVIEWING;
    }

    /** 게시 (SCR-A03 이벤트 2). 게시하면 사용자에게 공개된다. */
    public void publish() {
        assertOpen();
        this.statusCode = EsStatus.PUBLISHED;
        this.processedAt = OffsetDateTime.now();
    }

    /**
     * 반려 (SCR-A03 이벤트 3).
     *
     * 반려 통보는 별도 알림 없이 SCR-007 현황 조회로 갈음한다 (안건 1 확정).
     * 사유는 필수가 아니다 (R-39).
     */
    public void reject(String rejectReason) {
        assertOpen();
        this.statusCode = EsStatus.REJECTED;
        this.rejectReason = rejectReason;
        this.processedAt = OffsetDateTime.now();
    }

    public boolean isPublished() {
        return EsStatus.PUBLISHED.equals(statusCode);
    }

    private void assertOpen() {
        if (EsStatus.PUBLISHED.equals(statusCode) || EsStatus.REJECTED.equals(statusCode)) {
            throw new DomainException("이미 처리된 추가풀이입니다.");
        }
    }

    public Long getExtraSolutionId() {
        return extraSolutionId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getUserName() {
        return userName;
    }

    /**
     * 해시는 인증에만 쓴다.
     *
     * 비밀번호는 조회 키일 뿐이므로 관리자 화면에 노출하지 않는다 (SCR-A03 비고).
     * 관리자 응답 DTO에 이 값을 담지 않는다.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    public String getContents() {
        return contents;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }
}
