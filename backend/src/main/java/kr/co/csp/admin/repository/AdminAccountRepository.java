package kr.co.csp.admin.repository;

import java.util.Optional;
import kr.co.csp.admin.entity.AdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {

    Optional<AdminAccount> findByLoginId(String loginId);
}
