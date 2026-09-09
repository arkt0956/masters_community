package kr.co.csp.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * 관리자 계정 — tb_csp_adm01 (논리명 admin_account, DR-N06).
 *
 * 추가풀이 조회 키와 별개인 계정이다 (R-50). 세션은 서버 메모리로 관리하므로
 * 세션 테이블을 두지 않는다 (R-55, 단일 WAS 전제).
 */
@Entity
@Table(name = "tb_csp_adm01")
public class AdminAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "login_id", length = 50, nullable = false)
    private String loginId;

    /** bcrypt 해시. 평문·양방향 암호화 금지 (DR-S02). */
    @Column(name = "password_hash", length = 72, nullable = false)
    private String passwordHash;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    protected AdminAccount() {
    }

    public static AdminAccount create(String loginId, String passwordHash) {
        AdminAccount account = new AdminAccount();
        account.loginId = loginId;
        account.passwordHash = passwordHash;
        return account;
    }

    public void stampLogin() {
        this.lastLoginAt = OffsetDateTime.now();
    }

    public Long getAdminId() {
        return adminId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
