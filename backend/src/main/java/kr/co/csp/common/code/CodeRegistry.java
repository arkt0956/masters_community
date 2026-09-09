package kr.co.csp.common.code;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kr.co.csp.common.exception.DomainException;
import org.springframework.stereotype.Component;

/**
 * 공통코드 조회 캐시 (DR-C05).
 *
 * 이 캐시는 단일 WAS 전제다. 인스턴스를 늘리면 한 곳에서 refresh해도
 * 다른 곳은 낡은 상태로 남는다. 그때는 외부 캐시로 옮기거나 TTL을 둔다.
 */
@Component
public class CodeRegistry {

    private final CommonCodeRepository repository;
    private final Map<String, List<CodeItem>> cache = new ConcurrentHashMap<>();

    public CodeRegistry(CommonCodeRepository repository) {
        this.repository = repository;
    }

    /**
     * 활성 코드만 돌려주는 것이 기본이다.
     * 왜: 사용자 화면 드롭다운에 비활성 코드가 보이면 안 된다 (R-23).
     */
    public List<CodeItem> getGroup(String groupCode) {
        return getGroup(groupCode, true);
    }

    /**
     * activeOnly = false는 관리자 화면 전용이다.
     * 왜: 관리자는 비활성 코드까지 봐야 다시 활성화할 수 있다.
     */
    public List<CodeItem> getGroup(String groupCode, boolean activeOnly) {
        List<CodeItem> items = cache.computeIfAbsent(groupCode, this::load);
        return activeOnly ? items.stream().filter(CodeItem::active).toList() : items;
    }

    /** 특정 상위 코드에 속한 중분류만 (DR-C04 — 대분류 필터는 parent_code로). */
    public List<CodeItem> getChildren(String groupCode, String parentCode) {
        return getGroup(groupCode).stream()
                .filter(i -> parentCode.equals(i.parentCode()))
                .toList();
    }

    /**
     * 비활성 코드도 찾는다.
     * 왜: 과거 문항이 나중에 비활성화된 과목을 참조할 수 있다. 이름조차
     * 못 찾으면 화면이 깨진다. 삭제된 경우에는 코드값이라도 표시한다.
     */
    public String getName(String groupCode, String codeValue) {
        if (codeValue == null) {
            return null;
        }
        return getGroup(groupCode, false).stream()
                .filter(i -> i.codeValue().equals(codeValue))
                .map(CodeItem::codeName)
                .findFirst()
                .orElse(codeValue);
    }

    public boolean exists(String groupCode, String codeValue) {
        return getGroup(groupCode).stream().anyMatch(i -> i.codeValue().equals(codeValue));
    }

    /**
     * 코드가 없으면 만들지 말고 예외를 던진다 (DR-C03).
     *
     * 왜 생성하지 않는가:
     * getOrCreate로 바꾸면 오타가 새 코드로 등록된다. 아무도 모르는 사이에
     * 코드 체계가 오염되고, 나중에 정상 코드와 구분할 수 없다.
     */
    public String resolve(String groupCode, String codeValue) {
        if (!exists(groupCode, codeValue)) {
            throw new DomainException("등록되지 않았거나 사용하지 않는 코드입니다.");
        }
        return codeValue;
    }

    /**
     * 관리자가 코드를 변경한 직후 반드시 호출한다.
     * 왜: 빠뜨리면 코드를 추가해도 화면에 안 나타나고, 원인 찾기가 어렵다.
     * 코드 변경 서비스의 마지막 줄에 둔다.
     */
    public void refresh(String groupCode) {
        cache.remove(groupCode);
    }

    private List<CodeItem> load(String groupCode) {
        return repository.findByGroupCodeOrderBySortOrderAscCodeValueAsc(groupCode)
                .stream().map(CodeItem::from).toList();
    }
}
