package kr.co.csp.common.code;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {

    List<CommonCode> findByGroupCodeOrderBySortOrderAscCodeValueAsc(String groupCode);

    Optional<CommonCode> findByGroupCodeAndCodeValue(String groupCode, String codeValue);

    boolean existsByGroupCodeAndParentCode(String groupCode, String parentCode);
}
