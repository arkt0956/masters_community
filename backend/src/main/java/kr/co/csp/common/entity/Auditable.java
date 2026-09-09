package kr.co.csp.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import kr.co.csp.common.web.RequestContext;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 수정 이력 자동 기록 (DR-L02).
 *
 * 손으로 세팅하지 않는 이유: Service가 열 개면 열 곳에서 빠뜨릴 수 있다.
 * 엔티티 리스너에 두면 JPA가 변경을 감지할 때마다 채운다.
 *
 * 처음 저장할 때도 채운다. 생성 시점의 값이 첫 번째 수정 이력이 된다.
 *
 * 한계: @Modifying JPQL 벌크 수정은 엔티티를 거치지 않으므로 이 리스너가 돌지 않는다.
 * 벌크 수정 쿼리에는 updatedAt·updatedIp를 직접 넣고 clearAutomatically = true를 켠다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "updated_ip", nullable = false)
    private InetAddress updatedIp;

    @PrePersist
    @PreUpdate
    void stampUpdatedIp() {
        // ClientIpFilter가 DR-S01 규칙으로 얻어 둔 요청 IP
        this.updatedIp = RequestContext.clientIp();
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public InetAddress getUpdatedIp() {
        return updatedIp;
    }
}
