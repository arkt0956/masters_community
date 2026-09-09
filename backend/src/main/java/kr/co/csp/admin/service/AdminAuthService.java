package kr.co.csp.admin.service;

import kr.co.csp.admin.entity.AdminAccount;
import kr.co.csp.admin.repository.AdminAccountRepository;
import kr.co.csp.common.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 인증 (SCR-A00 · SCR-A05 · R-50). */
@Service
public class AdminAuthService {

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 존재하지 않는 계정에 대해서도 같은 비용을 치르기 위한 해시.
     * 무작위 값의 해시라 어떤 입력과도 일치하지 않는다. 상수 문자열로 두면
     * bcrypt 형식이 어긋났을 때 검증이 즉시 실패해 시간 차이가 그대로 남는다.
     */
    private final String dummyHash;

    public AdminAuthService(AdminAccountRepository adminAccountRepository, PasswordEncoder passwordEncoder) {
        this.adminAccountRepository = adminAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode(java.util.UUID.randomUUID().toString());
    }

    /**
     * 로그인.
     *
     * 왜 실패 사유를 구분하지 않는가:
     * "ID가 없습니다"와 "비밀번호가 틀립니다"를 나누면 어떤 계정이 존재하는지
     * 알려주는 셈이 된다 (SCR-A05 예외 표, DR-P05).
     *
     * 왜 계정이 없을 때도 해시 비교를 하는가:
     * 계정이 없으면 즉시 반환하고 있으면 bcrypt를 돌리면, 응답 시간 차이로
     * 계정 존재 여부를 알아낼 수 있다. 없을 때도 더미 해시로 같은 비용을 치른다.
     */
    @Transactional
    public AdminAccount login(String loginId, String rawPassword) {
        AdminAccount account = adminAccountRepository.findByLoginId(loginId).orElse(null);

        if (account == null) {
            passwordEncoder.matches(rawPassword, dummyHash);
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        account.stampLogin();
        return account;
    }

    @Transactional(readOnly = true)
    public AdminAccount findById(Long adminId) {
        return adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."));
    }
}
