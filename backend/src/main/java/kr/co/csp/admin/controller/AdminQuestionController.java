package kr.co.csp.admin.controller;

import jakarta.validation.Valid;
import java.util.List;
import kr.co.csp.admin.dto.AdminExamItem;
import kr.co.csp.admin.dto.AdminQuestionDetail;
import kr.co.csp.admin.dto.AdminQuestionItem;
import kr.co.csp.admin.dto.ExamSaveRequest;
import kr.co.csp.admin.dto.QuestionSaveRequest;
import kr.co.csp.admin.dto.SolutionSaveRequest;
import kr.co.csp.admin.service.AdminViewAssembler;
import kr.co.csp.content.dto.DrawingResponse;
import kr.co.csp.content.entity.Drawing;
import kr.co.csp.content.entity.DrawingOwner;
import kr.co.csp.content.entity.Exam;
import kr.co.csp.content.entity.Question;
import kr.co.csp.content.entity.Solution;
import kr.co.csp.content.service.ContentAdminService;
import kr.co.csp.content.service.DrawingService;
import kr.co.csp.content.service.ExamService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 문제·풀이 작성/게시 (SCR-A04 · REQ-A03 · R-13~R-17).
 *
 * 코드 수정·재배포 없이 콘텐츠를 추가할 수 있어야 한다 (R-13). 이 API가 그 수단이다.
 */
@RestController
@RequestMapping("/admin/api")
public class AdminQuestionController {

    private final ContentAdminService contentAdminService;
    private final ExamService examService;
    private final DrawingService drawingService;
    private final AdminViewAssembler assembler;

    public AdminQuestionController(ContentAdminService contentAdminService, ExamService examService,
                                   DrawingService drawingService, AdminViewAssembler assembler) {
        this.contentAdminService = contentAdminService;
        this.examService = examService;
        this.drawingService = drawingService;
        this.assembler = assembler;
    }

    // ---------------------------------------------------------------- 회차

    /** 비공개 회차도 보여야 편집할 수 있다. */
    @GetMapping("/exams")
    public List<AdminExamItem> exams() {
        return examService.findAllForAdmin().stream()
                .map(e -> new AdminExamItem(e.getExamId(), e.getExamRound(), e.isPublicExam()))
                .toList();
    }

    @PostMapping("/exams")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminExamItem createExam(@Valid @RequestBody ExamSaveRequest request) {
        Exam exam = contentAdminService.createExam(request.examRound(), request.isPublic());
        return new AdminExamItem(exam.getExamId(), exam.getExamRound(), exam.isPublicExam());
    }

    @PatchMapping("/exams/{examId}")
    public AdminExamItem modifyExam(@PathVariable Long examId, @Valid @RequestBody ExamSaveRequest request) {
        Exam exam = contentAdminService.modifyExam(examId, request.examRound(), request.isPublic());
        return new AdminExamItem(exam.getExamId(), exam.getExamRound(), exam.isPublicExam());
    }

    // ---------------------------------------------------------------- 문항

    /** statusCode를 비우면 삭제(QST003)를 뺀 전체다. 삭제 문항은 별도 필터로만 본다 (DR-L01). */
    @GetMapping("/questions")
    public List<AdminQuestionItem> questions(@RequestParam(required = false) String statusCode) {
        return assembler.questions(contentAdminService.findQuestions(statusCode));
    }

    @GetMapping("/questions/{questionId}")
    public AdminQuestionDetail question(@PathVariable Long questionId) {
        return assembler.questionDetail(contentAdminService.findQuestion(questionId));
    }

    /** 저장은 언제나 작성중 상태다. 비공개가 유지된다 (SCR-A04 이벤트 1). */
    @PostMapping("/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminQuestionDetail create(@Valid @RequestBody QuestionSaveRequest request) {
        Question question = contentAdminService.createQuestion(request.examId(), request.sessionNo(),
                request.questionNo(), request.categoryCode(), request.title(), request.contents());
        return assembler.questionDetail(question);
    }

    @PutMapping("/questions/{questionId}")
    public AdminQuestionDetail modify(@PathVariable Long questionId,
                                      @Valid @RequestBody QuestionSaveRequest request) {
        Question question = contentAdminService.modifyQuestion(questionId, request.examId(),
                request.sessionNo(), request.questionNo(), request.categoryCode(),
                request.title(), request.contents());
        return assembler.questionDetail(question);
    }

    /**
     * 게시 전환 (SCR-A04 ④).
     *
     * 게시 시점에 본문의 [[drawing:n]] 토큰을 검증한다. 고아 토큰·미참조 도면·중복이
     * 있으면 게시가 거부된다 (DR-F03).
     */
    @PatchMapping("/questions/{questionId}/publish")
    public AdminQuestionDetail publish(@PathVariable Long questionId) {
        return assembler.questionDetail(contentAdminService.publishQuestion(questionId));
    }

    @PatchMapping("/questions/{questionId}/unpublish")
    public AdminQuestionDetail unpublish(@PathVariable Long questionId) {
        return assembler.questionDetail(contentAdminService.unpublishQuestion(questionId));
    }

    /** 물리 삭제가 아니라 QST003 상태 전이다 (DR-L01). */
    @DeleteMapping("/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long questionId) {
        contentAdminService.deleteQuestion(questionId);
    }

    // ---------------------------------------------------------------- 해설

    /** 문항당 1건이므로 PUT(upsert)이다 (설계 3-3). */
    @PutMapping("/questions/{questionId}/solution")
    public AdminQuestionDetail saveSolution(@PathVariable Long questionId,
                                            @Valid @RequestBody SolutionSaveRequest request) {
        contentAdminService.saveSolution(questionId, request.contents());
        return assembler.questionDetail(contentAdminService.findQuestion(questionId));
    }

    // ---------------------------------------------------------------- 도면

    /**
     * 문항 도면 추가. 번호는 MAX(drawing_no) + 1이다 (DR-F01).
     *
     * 응답의 drawingNo를 편집 화면이 [[drawing:n]] 토큰으로 본문에 넣는다.
     * 사용자가 토큰을 직접 타이핑하게 두지 않는다 (DR-F03 에디터 절).
     */
    @PostMapping("/questions/{questionId}/drawings")
    @ResponseStatus(HttpStatus.CREATED)
    public DrawingResponse addQuestionDrawing(@PathVariable Long questionId,
                                              @RequestPart("file") MultipartFile file) {
        contentAdminService.findQuestion(questionId);
        Drawing drawing = drawingService.addDrawing(DrawingOwner.question(questionId), file);
        return DrawingResponse.from(drawing);
    }

    /** 해설 도면 추가. 채번 단위가 소유자별이라 문항 도면과 번호가 겹칠 수 있다 (DR-F01). */
    @PostMapping("/questions/{questionId}/solution/drawings")
    @ResponseStatus(HttpStatus.CREATED)
    public DrawingResponse addSolutionDrawing(@PathVariable Long questionId,
                                              @RequestPart("file") MultipartFile file) {
        Solution solution = contentAdminService.findSolution(questionId)
                .orElseThrow(() -> new kr.co.csp.common.exception.DomainException(
                        "해설을 먼저 저장한 뒤 도면을 올려 주세요."));
        Drawing drawing = drawingService.addDrawing(
                DrawingOwner.solution(solution.getSolutionId()), file);
        return DrawingResponse.from(drawing);
    }

    /** 삭제해도 번호를 당기지 않는다. 본문의 해당 토큰만 함께 지운다 (DR-F01). */
    @DeleteMapping("/drawings/{drawingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDrawing(@PathVariable Long drawingId) {
        drawingService.deleteDrawing(drawingId);
    }
}
