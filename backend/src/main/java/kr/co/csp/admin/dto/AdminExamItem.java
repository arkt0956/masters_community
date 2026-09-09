package kr.co.csp.admin.dto;

/** 관리자 회차 목록. 비공개 회차도 포함한다. */
public record AdminExamItem(Long examId, int examRound, boolean isPublic) {
}
