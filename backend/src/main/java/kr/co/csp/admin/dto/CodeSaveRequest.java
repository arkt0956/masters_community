package kr.co.csp.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 공통코드 등록 요청 (DR-C02 · DR-C04).
 *
 * groupCode는 이미 존재하는 그룹이어야 한다. 새 그룹은 팀 합의와 마이그레이션으로만 만든다 (DR-C01).
 */
public record CodeSaveRequest(
        @NotBlank(message = "그룹코드가 필요합니다.") String groupCode,
        @NotBlank(message = "코드값이 필요합니다.") String codeValue,
        @NotBlank(message = "코드명이 필요합니다.") String codeName,
        @Min(value = 1, message = "계층은 1 또는 2입니다.")
        @Max(value = 2, message = "계층은 1 또는 2입니다.") int codeLevel,
        String parentCode,
        int sortOrder
) {
}
