package kr.co.csp.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 공통코드 수정 요청.
 *
 * 코드값은 바꿀 수 없다. 코드값은 업무 데이터가 참조하는 값이라, 바꾸면
 * 이미 그 값으로 저장된 행이 이름조차 찾지 못하게 된다 (DR-C05).
 */
public record CodeUpdateRequest(
        @NotBlank(message = "코드명이 필요합니다.") String codeName,
        int sortOrder,
        Boolean active
) {
}
