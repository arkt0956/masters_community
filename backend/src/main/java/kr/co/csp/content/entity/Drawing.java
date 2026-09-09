package kr.co.csp.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 도면 — tb_csp_con04 (논리명 drawing, DR-N06).
 *
 * updated_at이 없으므로 Auditable을 상속하지 않는다. 도면은 수정하지 않고
 * 지우고 다시 올린다 (DR-L02의 대상 테이블은 con01·con02·con03).
 */
@Entity
@Table(name = "tb_csp_con04")
@EntityListeners(AuditingEntityListener.class)
public class Drawing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drawing_id")
    private Long drawingId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "solution_id")
    private Long solutionId;

    @Column(name = "extra_solution_id")
    private Long extraSolutionId;

    /**
     * 소유자 안에서 도면을 가리키는 식별 번호. 순서가 아니다 (DR-F01).
     * 삭제해도 재채번하지 않으므로 연속이 아닐 수 있다.
     */
    @Column(name = "drawing_no", nullable = false)
    private int drawingNo;

    /** 저장 파일명. 원본 파일명을 경로에 쓰면 이름을 추측해 접근할 수 있다 (R-53, DR-F02). */
    @Column(name = "file_uuid", nullable = false)
    private UUID fileUuid;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    /**
     * 등록 시점 원본 SHA-256. 재인코딩 금지 검증용 (R-16).
     *
     * 설계서 DDL이 CHAR(64)다. String의 기본 매핑은 varchar라서
     * JdbcTypeCode를 지정하지 않으면 ddl-auto=validate가 기동을 막는다.
     * SHA-256 16진 표기는 길이가 항상 64라 고정 길이로 둔 것이다.
     */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "file_hash", length = 64, nullable = false)
    private String fileHash;

    /**
     * 서빙 시 Content-Type 값.
     * 저장 파일에 확장자가 없으므로 이 값이 없으면 브라우저가 이미지를 렌더링하지 않고
     * 다운로드한다 (DR-F02).
     */
    @Column(name = "file_type", length = 100, nullable = false)
    private String fileType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Drawing() {
    }

    public static Drawing of(DrawingOwner owner, int drawingNo, UUID fileUuid,
                             String fileName, String fileHash, String fileType) {
        Drawing drawing = new Drawing();
        switch (owner.type()) {
            case QUESTION -> drawing.questionId = owner.id();
            case SOLUTION -> drawing.solutionId = owner.id();
            case EXTRA_SOLUTION -> drawing.extraSolutionId = owner.id();
        }
        drawing.drawingNo = drawingNo;
        drawing.fileUuid = fileUuid;
        drawing.fileName = fileName;
        drawing.fileHash = fileHash;
        drawing.fileType = fileType;
        return drawing;
    }

    public DrawingOwner owner() {
        if (questionId != null) {
            return DrawingOwner.question(questionId);
        }
        if (solutionId != null) {
            return DrawingOwner.solution(solutionId);
        }
        return DrawingOwner.extraSolution(extraSolutionId);
    }

    public Long getDrawingId() {
        return drawingId;
    }

    public int getDrawingNo() {
        return drawingNo;
    }

    public UUID getFileUuid() {
        return fileUuid;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileHash() {
        return fileHash;
    }

    public String getFileType() {
        return fileType;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
