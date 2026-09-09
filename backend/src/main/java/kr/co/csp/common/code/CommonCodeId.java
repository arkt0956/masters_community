package kr.co.csp.common.code;

import java.io.Serializable;
import java.util.Objects;

/** 공통코드 복합 PK (group_code, code_value). */
public class CommonCodeId implements Serializable {

    private String groupCode;
    private String codeValue;

    protected CommonCodeId() {
    }

    public CommonCodeId(String groupCode, String codeValue) {
        this.groupCode = groupCode;
        this.codeValue = codeValue;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public String getCodeValue() {
        return codeValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommonCodeId other)) {
            return false;
        }
        return Objects.equals(groupCode, other.groupCode) && Objects.equals(codeValue, other.codeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupCode, codeValue);
    }
}
