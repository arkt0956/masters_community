package kr.co.csp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** 토목기술사 문제풀이 서비스 WAS. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class CspApplication {

    public static void main(String[] args) {
        SpringApplication.run(CspApplication.class, args);
    }
}
