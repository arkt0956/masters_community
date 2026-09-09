package kr.co.csp.admin.dto;

import jakarta.validation.constraints.Min;

/** 회차 등록·수정 요청. isPublic이 false면 사용자 화면에 나오지 않는다 (R-01). */
public record ExamSaveRequest(
        @Min(value = 1, message = "회차 번호는 1 이상이어야 합니다.") int examRound,
        boolean isPublic
) {
}
