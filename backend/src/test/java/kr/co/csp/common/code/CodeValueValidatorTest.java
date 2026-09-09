package kr.co.csp.common.code;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.csp.common.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** DR-C02 — 코드값 채번 형식. */
class CodeValueValidatorTest {

    @Test
    @DisplayName("대분류는 대문자 3자")
    void 대분류() {
        assertThatCode(() -> CodeValueValidator.validate("STM", 1)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("중분류는 상위 약어 3자 + 숫자 3자리")
    void 중분류() {
        assertThatCode(() -> CodeValueValidator.validate("STM005", 2)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("앞자리 0을 보존한다 — 숫자 타입으로 다루지 않는 이유 (DR-C02)")
    void 앞자리_0() {
        assertThatCode(() -> CodeValueValidator.validate("GEN001", 2)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("계층 없는 그룹의 RPT001 형식은 code_level=1에서 통과하지 못한다")
    void 계층_없는_그룹() {
        // 개발규칙 DR-C02의 채번 표는 계층 없는 그룹에 'RPT001' 형식을 허용하지만,
        // 같은 규칙에 실린 검증 코드는 code_level=1에 3자만 허용한다. 표와 코드가 어긋난다.
        // 여기서는 규칙에 실린 코드를 그대로 따랐다. 시스템 코드는 마이그레이션으로만
        // 들어오고 이 검증을 거치지 않으므로 현재 동작에는 영향이 없다.
        // 관리자 화면이 실제로 다루는 그룹은 SUBJECT뿐이고, 그 값들은 이 형식을 만족한다.
        assertThatThrownBy(() -> CodeValueValidator.validate("RPT001", 1))
                .isInstanceOf(DomainException.class);
    }

    @ParameterizedTest
    @DisplayName("형식에 맞지 않으면 등록 시점에 막는다")
    @ValueSource(strings = {"stm", "ST", "STMM", "STM05", "STM0055", "1234"})
    void 잘못된_형식(String codeValue) {
        assertThatThrownBy(() -> CodeValueValidator.validate(codeValue, 1))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("대분류 자리에 6자를 넣으면 막는다")
    void 대분류에_중분류_형식() {
        assertThatThrownBy(() -> CodeValueValidator.validate("STM005", 1))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("대분류");
    }

    @Test
    @DisplayName("중분류 자리에 3자를 넣으면 막는다")
    void 중분류에_대분류_형식() {
        assertThatThrownBy(() -> CodeValueValidator.validate("STM", 2))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("중분류");
    }
}
