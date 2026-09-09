package kr.co.csp.content.controller;

import java.util.List;
import kr.co.csp.content.dto.CategoryResponse;
import kr.co.csp.content.dto.QuestionListItem;
import kr.co.csp.content.service.CategoryService;
import kr.co.csp.content.service.QuestionQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 과목 API (SCR-003). */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final QuestionQueryService questionQueryService;

    public CategoryController(CategoryService categoryService, QuestionQueryService questionQueryService) {
        this.categoryService = categoryService;
        this.questionQueryService = questionQueryService;
    }

    /** 대분류 → 중분류 2단계 목록. 문항 0건인 항목은 빠진다 (DR-C06). */
    @GetMapping
    public List<CategoryResponse> categories() {
        return categoryService.findCategoriesWithQuestions();
    }

    /**
     * 과목 문항 목록 (SCR-004 진입).
     * categoryCode에는 대분류(3자)와 중분류(6자) 어느 쪽이 와도 된다.
     */
    @GetMapping("/{categoryCode}/questions")
    public List<QuestionListItem> questions(@PathVariable String categoryCode) {
        return questionQueryService.findByCategory(categoryCode);
    }
}
