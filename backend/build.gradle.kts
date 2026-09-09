plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "kr.co.csp"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        // 개발규칙 5장 확정 사항 — Java 21
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // DR-S02 — bcrypt만 쓴다. 관리자 인증은 서버 세션 직접 구현(R-55, 단일 WAS 전제)이므로
    // spring-boot-starter-security의 필터 체인 전체를 끌어오지 않는다.
    implementation("org.springframework.security:spring-security-crypto")

    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
