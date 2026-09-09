package kr.co.csp.common.code;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import kr.co.csp.common.exception.DomainException;

/** 공통코드 — tb_csp_cmn01 (DR-N06: 물리명은 @Table에만). */
@Entity
@Table(name = "tb_csp_cmn01")
@IdClass(CommonCodeId.class)
public class CommonCode {

    @Id
    @Column(name = "group_code", length = 20, nullable = false)
    private String groupCode;

    @Id
    @Column(name = "code_value", length = 20, nullable = false)
    private String codeValue;

    @Column(name = "code_name", length = 100, nullable = false)
    private String codeName;

    @Column(name = "code_level", nullable = false)
    private int codeLevel;

    @Column(name = "parent_code", length = 20)
    private String parentCode;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    protected CommonCode() {
    }

    public static CommonCode create(String groupCode, String codeValue, String codeName,
                                    int codeLevel, String parentCode, int sortOrder) {
        CodeValueValidator.validate(codeValue, codeLevel);
        CommonCode code = new CommonCode();
        code.groupCode = groupCode;
        code.codeValue = codeValue;
        code.codeName = codeName;
        code.codeLevel = codeLevel;
        code.parentCode = parentCode;
        code.sortOrder = sortOrder;
        code.active = true;
        // 관리자 화면에서 만든 코드는 시스템 코드가 아니다. 로직이 참조하는 코드는
        // 마이그레이션에서만 is_system = TRUE로 들어간다 (DR-C05).
        code.system = false;
        return code;
    }

    /**
     * 시스템 코드는 코드명·정렬 순서만 바꿀 수 있다 (DR-C05).
     *
     * 왜 막는가: Q_STATUS의 QST002(게시됨)를 지우면 문항 목록이 빈 채로 나오는데
     * 오류도 나지 않아 원인을 찾기 어렵다.
     */
    public void rename(String codeName, int sortOrder) {
        this.codeName = codeName;
        this.sortOrder = sortOrder;
    }

    public void changeActive(boolean active) {
        if (this.system && !active) {
            throw new DomainException("시스템 코드는 비활성화할 수 없습니다.");
        }
        this.active = active;
    }

    public void assertDeletable() {
        if (this.system) {
            throw new DomainException("시스템 코드는 삭제할 수 없습니다.");
        }
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public String getCodeName() {
        return codeName;
    }

    public int getCodeLevel() {
        return codeLevel;
    }

    public String getParentCode() {
        return parentCode;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isSystem() {
        return system;
    }
}
