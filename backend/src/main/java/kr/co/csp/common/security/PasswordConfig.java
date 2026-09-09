package kr.co.csp.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 해시 (DR-S02).
 *
 * 왜 복호화 메서드를 만들지 않는가:
 * 필요하지 않고, 있으면 누군가 쓰게 된다.
 * 비밀번호를 잊은 사용자는 재발급이 아니라 재등록으로 처리한다.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
