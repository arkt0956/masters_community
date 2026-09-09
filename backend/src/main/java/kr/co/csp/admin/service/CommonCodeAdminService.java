package kr.co.csp.admin.service;

import java.util.List;
import kr.co.csp.common.code.CodeItem;
import kr.co.csp.common.code.CodeRegistry;
import kr.co.csp.common.code.CommonCode;
import kr.co.csp.common.code.CommonCodeRepository;
import kr.co.csp.common.exception.DomainException;
import kr.co.csp.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공통코드 관리 (SCR-A04 계열 · DR-C03 · DR-C05).
 *
 * 운영 중 코드 추가는 관리자 화면에서만 한다. 스크립트로 추가하지 않는다.
 * 새 그룹코드는 팀 합의와 접두어 표(DR-C02) 등록이 선행되어야 하므로,
 * 이 서비스는 기존 그룹에 코드를 더하는 것까지만 다룬다.
 */
@Service
public class CommonCodeAdminService {

    private final CommonCodeRepository repository;
    private final CodeRegistry codeRegistry;

    public CommonCodeAdminService(CommonCodeRepository repository, CodeRegistry codeRegistry) {
        this.repository = repository;
        this.codeRegistry = codeRegistry;
    }

    /** 비활성 코드까지 보여준다. 관리자는 다시 활성화할 수 있어야 한다 (DR-C05). */
    @Transactional(readOnly = true)
    public List<CodeItem> findGroup(String groupCode) {
        return codeRegistry.getGroup(groupCode, false);
    }

    /**
     * 코드 추가.
     *
     * 왜 새 그룹코드를 막는가: 그룹을 남발하면 3자 접두어가 금방 고갈되고,
     * 코드값만 보고 소속 그룹을 알 수 없게 된다 (DR-C01 · DR-C02).
     * 새 그룹은 팀 합의와 마이그레이션으로만 만든다.
     */
    @Transactional
    public CommonCode create(String groupCode, String codeValue, String codeName,
                             int codeLevel, String parentCode, int sortOrder) {
        if (codeRegistry.getGroup(groupCode, false).isEmpty()) {
            throw new DomainException("등록되지 않은 그룹코드입니다. 새 그룹은 팀 합의 후 마이그레이션으로 추가합니다.");
        }
        if (repository.findByGroupCodeAndCodeValue(groupCode, codeValue).isPresent()) {
            throw new DomainException("이미 등록된 코드값입니다.");
        }
        if (codeLevel == 2) {
            repository.findByGroupCodeAndCodeValue(groupCode, parentCode)
                    .orElseThrow(() -> new DomainException("상위 코드가 없습니다."));
        }
        // 형식 검사는 엔티티 생성 시점에 한다 (CodeValueValidator, DR-C02).
        CommonCode saved = repository.save(
                CommonCode.create(groupCode, codeValue, codeName, codeLevel, parentCode, sortOrder));
        codeRegistry.refresh(groupCode);
        return saved;
    }

    /**
     * 코드명·정렬 순서·활성 여부 수정.
     *
     * 왜 한 메서드인가: 코드명 변경과 활성 전환을 따로 호출하면 트랜잭션이 둘로 갈린다.
     * 시스템 코드에 이름 변경과 비활성화를 함께 보내면 이름은 커밋되고 비활성화만
     * 거부되어, 클라이언트는 실패로 아는데 이름은 이미 바뀌어 있는 상태가 된다.
     * PATCH 한 번은 전부 반영되거나 전부 반영되지 않아야 한다.
     *
     * 시스템 코드도 코드명·정렬 순서는 바꿀 수 있고, 비활성화만 막힌다 (DR-C05).
     * active가 null이면 활성 여부를 건드리지 않는다.
     */
    @Transactional
    public CommonCode update(String groupCode, String codeValue, String codeName,
                             int sortOrder, Boolean active) {
        CommonCode code = find(groupCode, codeValue);
        // 활성 여부를 먼저 검사한다. 거부될 변경이라면 이름도 바꾸지 않고 롤백된다.
        if (active != null) {
            code.changeActive(active);
        }
        code.rename(codeName, sortOrder);
        // 빠뜨리면 코드를 고쳐도 화면에 반영되지 않는다 (DR-C05).
        codeRegistry.refresh(groupCode);
        return code;
    }

    /**
     * 삭제.
     *
     * 시스템 코드는 삭제할 수 없다. 자식이 있는 대분류도 삭제하지 않는다.
     * 자식을 남기면 CHECK 제약(2단계는 parent_code 필수)이 깨진다.
     */
    @Transactional
    public void delete(String groupCode, String codeValue) {
        CommonCode code = find(groupCode, codeValue);
        code.assertDeletable();
        if (repository.existsByGroupCodeAndParentCode(groupCode, codeValue)) {
            throw new DomainException("하위 코드가 있어 삭제할 수 없습니다. 비활성화를 사용하세요.");
        }
        repository.delete(code);
        // 빠뜨리면 코드를 지워도 화면에 계속 나타난다 (DR-C05).
        codeRegistry.refresh(groupCode);
    }

    private CommonCode find(String groupCode, String codeValue) {
        return repository.findByGroupCodeAndCodeValue(groupCode, codeValue)
                .orElseThrow(() -> new NotFoundException("코드를 찾을 수 없습니다."));
    }
}
