package kr.co.csp.admin.service;

import kr.co.csp.admin.entity.AdminAccount;
import kr.co.csp.admin.repository.AdminAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 초기 관리자 계정 생성 (R-50).
 *
 * 왜 마이그레이션에 INSERT를 넣지 않는가:
 * 초기 계정 정보는 문서·저장소에 남기지 않고 별도 전달한다. 마이그레이션 파일에
 * 넣으면 해시가 형상관리에 영원히 남고, 그 해시를 오프라인에서 깰 수 있다.
 *
 * 환경변수로 받아 계정이 하나도 없을 때만 만든다. 환경변수가 없으면 아무것도 하지 않고
 * 경고만 남긴다. 기동을 막지 않는 이유는 계정 생성을 운영자가 별도 절차로 할 수도 있기 때문이다.
 */
@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialLoginId;
    private final String initialPassword;

    public AdminAccountInitializer(AdminAccountRepository adminAccountRepository,
                                   PasswordEncoder passwordEncoder,
                                   @Value("${CSP_ADMIN_ID:}") String initialLoginId,
                                   @Value("${CSP_ADMIN_PASSWORD:}") String initialPassword) {
        this.adminAccountRepository = adminAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialLoginId = initialLoginId;
        this.initialPassword = initialPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminAccountRepository.count() > 0) {
            return;
        }
        if (initialLoginId.isBlank() || initialPassword.isBlank()) {
            log.warn("관리자 계정이 없습니다. CSP_ADMIN_ID · CSP_ADMIN_PASSWORD 환경변수로 초기 계정을 만드세요.");
            return;
        }
        adminAccountRepository.save(
                AdminAccount.create(initialLoginId, passwordEncoder.encode(initialPassword)));
        // 비밀번호는 로그에 남기지 않는다.
        log.info("초기 관리자 계정을 생성했습니다: {}", initialLoginId);
    }
}
