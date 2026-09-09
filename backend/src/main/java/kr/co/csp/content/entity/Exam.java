package kr.co.csp.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.csp.common.entity.Auditable;

/**
 * 회차 — tb_csp_con01 (논리명 exam, DR-N06).
 *
 * exam_round에 UNIQUE가 걸려 있지만 PK는 대리키다. 회차를 잘못 등록했을 때
 * 문항이 이미 붙어 있어도 UPDATE 한 번으로 정정할 수 있다 (설계 3-1).
 */
@Entity
@Table(name = "tb_csp_con01")
public class Exam extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private Long examId;

    @Column(name = "exam_round", nullable = false)
    private int examRound;

    /** R-01 — 공개 회차만 사용자 목록에 노출한다. */
    @Column(name = "is_public", nullable = false)
    private boolean publicExam;

    protected Exam() {
    }

    public static Exam create(int examRound, boolean publicExam) {
        Exam exam = new Exam();
        exam.examRound = examRound;
        exam.publicExam = publicExam;
        return exam;
    }

    public void modify(int examRound, boolean publicExam) {
        this.examRound = examRound;
        this.publicExam = publicExam;
    }

    public Long getExamId() {
        return examId;
    }

    public int getExamRound() {
        return examRound;
    }

    public boolean isPublicExam() {
        return publicExam;
    }
}
