package kr.co.csp.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.co.csp.common.code.SystemCode.QStatus;
import kr.co.csp.common.entity.Auditable;
import kr.co.csp.common.exception.DomainException;

/**
 * 문항 — tb_csp_con02 (논리명 question, DR-N06).
 *
 * 해설은 이 엔티티에 없다. 목록 조회가 해설을 실어 나르지 못하도록
 * 스키마 수준에서 분리했다 (설계 3-3, DR-A01).
 */
@Entity
@Table(name = "tb_csp_con02")
public class Question extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "session_no", nullable = false)
    private int sessionNo;

    @Column(name = "question_no", nullable = false)
    private int questionNo;

    /** SUBJECT 중분류(2단계) 값만 저장한다. 대분류는 parent_code로 조회한다 (DR-C04·DR-C06). */
    @Column(name = "category_code", length = 20, nullable = false)
    private String categoryCode;

    @Column(name = "title", nullable = false)
    private String title;

    /** 이미지 위치는 [[drawing:n]] 토큰으로 표기한다 (DR-F03). */
    @Column(name = "contents", nullable = false)
    private String contents;

    @Column(name = "status_code", length = 20, nullable = false)
    private String statusCode = QStatus.DRAFT;

    protected Question() {
    }

    public static Question create(Long examId, int sessionNo, int questionNo,
                                  String categoryCode, String title, String contents) {
        Question question = new Question();
        question.examId = examId;
        question.sessionNo = sessionNo;
        question.questionNo = questionNo;
        question.categoryCode = categoryCode;
        question.title = title;
        question.contents = contents;
        question.statusCode = QStatus.DRAFT;
        return question;
    }

    /**
     * 전체 수정.
     *
     * examId를 포함하는 이유: PUT은 보낸 표현으로 전체를 치환한다는 약속이다.
     * 회차만 빠뜨리면 요청은 200으로 성공하는데 값은 바뀌지 않아, 실패도 성공도
     * 아닌 상태가 된다. 회차를 잘못 지정한 문항을 옮기는 것은 정상적인 정정이다.
     *
     * (exam_id, session_no, question_no)에 UNIQUE가 걸려 있으므로 중복 검사는
     * Service가 먼저 한다. 여기까지 왔으면 저장 가능한 조합이다.
     */
    public void modify(Long examId, int sessionNo, int questionNo,
                       String categoryCode, String title, String contents) {
        assertEditable();
        this.examId = examId;
        this.sessionNo = sessionNo;
        this.questionNo = questionNo;
        this.categoryCode = categoryCode;
        this.title = title;
        this.contents = contents;
    }

    /**
     * 본문만 교체 (도면 삭제 시 토큰 정리용, DR-F01).
     *
     * 왜 modify를 쓰지 않는가: 도면을 지우려고 회차·번호·과목까지 넘기게 되면
     * 그 값들을 실수로 바꿔 쓸 여지가 생긴다. 바꿀 것만 받는다.
     */
    public void replaceContents(String contents) {
        assertEditable();
        this.contents = contents;
    }

    /** 게시 시점에만 도면 토큰을 검증한다. 검증은 Service가 한다 (DR-F03). */
    public void publish() {
        assertEditable();
        this.statusCode = QStatus.PUBLISHED;
    }

    public void unpublish() {
        assertEditable();
        this.statusCode = QStatus.DRAFT;
    }

    /**
     * 삭제는 상태 전이다 (DR-L01).
     *
     * 왜 DELETE를 쓰지 않는가: 해설·도면·신고·추가풀이가 FK로 매달려 있다.
     * 물리 삭제하면 사용자가 등록한 데이터가 함께 사라지거나 FK 제약에 걸린다.
     */
    public void delete() {
        this.statusCode = QStatus.DELETED;
    }

    public boolean isPublished() {
        return QStatus.PUBLISHED.equals(statusCode);
    }

    public boolean isDeleted() {
        return QStatus.DELETED.equals(statusCode);
    }

    /** 삭제 상태는 편집 대상이 아니다 (DR-L01의 상태별 노출 범위 표). */
    private void assertEditable() {
        if (isDeleted()) {
            throw new DomainException("삭제된 문항은 수정할 수 없습니다.");
        }
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Long getExamId() {
        return examId;
    }

    public int getSessionNo() {
        return sessionNo;
    }

    public int getQuestionNo() {
        return questionNo;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getTitle() {
        return title;
    }

    public String getContents() {
        return contents;
    }

    public String getStatusCode() {
        return statusCode;
    }
}
