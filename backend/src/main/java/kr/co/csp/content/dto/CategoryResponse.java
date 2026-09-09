package kr.co.csp.content.dto;

import java.util.List;

/**
 * 과목 응답 (SCR-003).
 *
 * 중분류가 108개라 대분류를 먼저 고르는 2단계 구조다 (DR-C06).
 * questionCount는 그 대분류에 속한 중분류들의 합계다.
 *
 * 문항이 0건인 항목은 서비스가 걸러 낸다 (SCR-003 예외 표).
 */
public record CategoryResponse(
        String codeValue,
        String codeName,
        long questionCount,
        List<CategoryResponse> children
) {
}
