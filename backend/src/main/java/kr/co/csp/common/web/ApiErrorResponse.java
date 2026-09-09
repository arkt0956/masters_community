package kr.co.csp.common.web;

/** 오류 응답 본문. 내부 구조를 담지 않는다 (DR-P05). */
public record ApiErrorResponse(String message) {
}
