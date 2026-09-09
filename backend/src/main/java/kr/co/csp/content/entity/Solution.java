package kr.co.csp.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.csp.common.entity.Auditable;

/**
 * 해설 — tb_csp_con03 (논리명 solution, DR-N06).
 *
 * 문항과 1:0..1. question_id에 UNIQUE가 걸려 있어 문항 하나에 해설이 둘 생기지 않는다.
 * 목록 API는 이 테이블을 조인하지 않는다 (R-09 · R-54 · DR-A01).
 */
@Entity
@Table(name = "tb_csp_con03")
public class Solution extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solution_id")
    private Long solutionId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /** 이미지 위치는 [[drawing:n]] 토큰 (DR-F03). */
    @Column(name = "contents", nullable = false)
    private String contents;

    protected Solution() {
    }

    public static Solution create(Long questionId, String contents) {
        Solution solution = new Solution();
        solution.questionId = questionId;
        solution.contents = contents;
        return solution;
    }

    public void modify(String contents) {
        this.contents = contents;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getContents() {
        return contents;
    }
}
