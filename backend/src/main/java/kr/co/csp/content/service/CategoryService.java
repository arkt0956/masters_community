package kr.co.csp.content.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.co.csp.common.code.CodeItem;
import kr.co.csp.common.code.CodeRegistry;
import kr.co.csp.common.code.SystemCode.Grp;
import kr.co.csp.common.code.SystemCode.QStatus;
import kr.co.csp.content.dto.CategoryResponse;
import kr.co.csp.content.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 과목 조회 (SCR-003).
 *
 * 중분류가 108개라 대분류를 먼저 고르는 2단계 UI가 필요하다 (DR-C06).
 * 이 서비스는 대분류 목록에 중분류를 중첩해 한 번에 내려준다.
 */
@Service
public class CategoryService {

    private final CodeRegistry codeRegistry;
    private final QuestionRepository questionRepository;

    public CategoryService(CodeRegistry codeRegistry, QuestionRepository questionRepository) {
        this.codeRegistry = codeRegistry;
        this.questionRepository = questionRepository;
    }

    /**
     * 문항이 있는 과목만 (SCR-003 예외 표 — 문항 0건인 과목은 목록에서 제외).
     *
     * 왜 코드 목록을 소스에 두지 않는가: 과목은 관리자 화면에서 추가·비활성화된다.
     * 소스에 박아두면 코드를 하나 늘릴 때마다 배포가 필요하다 (DR-C05).
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> findCategoriesWithQuestions() {
        Map<String, Long> countByCode = new HashMap<>();
        for (Object[] row : questionRepository.countByCategory(QStatus.PUBLISHED)) {
            countByCode.put((String) row[0], ((Number) row[1]).longValue());
        }

        // 활성 코드만 담긴다 (R-23).
        List<CodeItem> all = codeRegistry.getGroup(Grp.SUBJECT);

        return all.stream()
                .filter(c -> c.codeLevel() == 1)
                .map(major -> {
                    List<CategoryResponse> children = all.stream()
                            .filter(c -> c.codeLevel() == 2 && major.codeValue().equals(c.parentCode()))
                            .map(minor -> new CategoryResponse(minor.codeValue(), minor.codeName(),
                                    countByCode.getOrDefault(minor.codeValue(), 0L), List.of()))
                            .filter(minor -> minor.questionCount() > 0)
                            .toList();
                    long total = children.stream().mapToLong(CategoryResponse::questionCount).sum();
                    return new CategoryResponse(major.codeValue(), major.codeName(), total, children);
                })
                .filter(major -> major.questionCount() > 0)
                .toList();
    }
}
