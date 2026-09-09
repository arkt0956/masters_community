package kr.co.csp.common.code;

import java.util.regex.Pattern;
import kr.co.csp.common.exception.DomainException;

/** 코드값 채번 형식 검사 (DR-C02). */
public final class CodeValueValidator {

    /** 대문자 3자(대분류) 또는 대문자 3자 + 숫자 3자리(그 외) */
    private static final Pattern PATTERN = Pattern.compile("^[A-Z]{3}(\\d{3})?$");

    private CodeValueValidator() {
    }

    /**
     * 왜 여기서 검사하는가:
     * DB의 PK는 중복만 막는다. 형식이 제각각이면 나중에 코드값만 보고
     * 소속 그룹을 알 수 없게 된다. 등록 시점에 형식을 강제한다.
     */
    public static void validate(String codeValue, int codeLevel) {
        if (codeValue == null || !PATTERN.matcher(codeValue).matches()) {
            throw new DomainException("코드값은 영문 대문자 3자 또는 대문자 3자 + 숫자 3자리여야 합니다.");
        }
        if (codeLevel == 1 && codeValue.length() != 3) {
            throw new DomainException("대분류 코드값은 영문 대문자 3자여야 합니다.");
        }
        if (codeLevel == 2 && codeValue.length() != 6) {
            throw new DomainException("중분류 코드값은 상위 약어 3자 + 숫자 3자리여야 합니다.");
        }
    }
}
