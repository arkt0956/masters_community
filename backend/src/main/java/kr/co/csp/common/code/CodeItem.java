package kr.co.csp.common.code;

/**
 * 캐시·응답에 쓰는 공통코드 한 건.
 *
 * 왜 엔티티를 그대로 캐시하지 않는가: 엔티티는 영속성 컨텍스트에 묶여 있어
 * 여러 요청이 공유하는 캐시에 담기에 안전하지 않다.
 */
public record CodeItem(
        String groupCode,
        String codeValue,
        String codeName,
        int codeLevel,
        String parentCode,
        int sortOrder,
        boolean active,
        boolean system
) {

    public static CodeItem from(CommonCode code) {
        return new CodeItem(code.getGroupCode(), code.getCodeValue(), code.getCodeName(),
                code.getCodeLevel(), code.getParentCode(), code.getSortOrder(),
                code.isActive(), code.isSystem());
    }
}
