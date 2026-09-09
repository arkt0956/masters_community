package kr.co.csp.participation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 추가풀이 등록 요청 (SCR-006 · POST /api/extra-solutions).
 *
 * 길이 제한은 화면과 같은 값을 쓴다 (DR-P07). 화면 검증은 사용자 편의이지 보안이 아니므로
 * 서버가 다시 검증한다 (DR-P04).
 */
public record ExtraSolutionCreateRequest(
        @NotNull(message = "대상 문항이 필요합니다.")
        Long questionId,

        @NotBlank(message = "ID(닉네임)를 입력해 주세요.")
        @Size(min = 2, max = 12, message = "ID는 2~12자로 입력해 주세요.")
        String userName,

        // 회원 비밀번호가 아니라 본인 등록건 조회 키다 (SCR-006 ②).
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 4, max = 64, message = "비밀번호는 4자 이상으로 입력해 주세요.")
        String password,

        @NotBlank(message = "풀이 내용을 입력해 주세요.")
        String contents
) {
}
