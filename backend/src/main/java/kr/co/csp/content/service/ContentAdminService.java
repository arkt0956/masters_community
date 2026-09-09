package kr.co.csp.content.service;

import java.util.List;
import java.util.Optional;
import kr.co.csp.common.code.CodeRegistry;
import kr.co.csp.common.code.SystemCode.Grp;
import kr.co.csp.common.code.SystemCode.QStatus;
import kr.co.csp.common.exception.DomainException;
import kr.co.csp.common.exception.NotFoundException;
import kr.co.csp.content.entity.Drawing;
import kr.co.csp.content.entity.DrawingOwner;
import kr.co.csp.content.entity.Exam;
import kr.co.csp.content.entity.Question;
import kr.co.csp.content.entity.Solution;
import kr.co.csp.content.repository.ExamRepository;
import kr.co.csp.content.repository.QuestionRepository;
import kr.co.csp.content.repository.SolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 콘텐츠 작성·게시 (SCR-A04 · REQ-A03 · R-13~R-17).
 *
 * 코드 수정·재배포 없이 콘텐츠를 추가할 수 있어야 한다 (R-13). 이 서비스가 그 수단이다.
 */
@Service
public class ContentAdminService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final SolutionRepository solutionRepository;
    private final DrawingService drawingService;
    private final CodeRegistry codeRegistry;

    public ContentAdminService(ExamRepository examRepository, QuestionRepository questionRepository,
                               SolutionRepository solutionRepository,
                               DrawingService drawingService, CodeRegistry codeRegistry) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.solutionRepository = solutionRepository;
        this.drawingService = drawingService;
        this.codeRegistry = codeRegistry;
    }

    // ---------------------------------------------------------------- 회차

    @Transactional
    public Exam createExam(int examRound, boolean publicExam) {
        if (examRepository.existsByExamRound(examRound)) {
            throw new DomainException("이미 등록된 회차입니다.");
        }
        return examRepository.save(Exam.create(examRound, publicExam));
    }

    @Transactional
    public Exam modifyExam(Long examId, int examRound, boolean publicExam) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("회차를 찾을 수 없습니다."));
        if (exam.getExamRound() != examRound && examRepository.existsByExamRound(examRound)) {
            throw new DomainException("이미 등록된 회차입니다.");
        }
        exam.modify(examRound, publicExam);
        return exam;
    }

    // ---------------------------------------------------------------- 문항

    /**
     * 문항 작성. 저장 시점 상태는 작성중이며 사용자에게 노출되지 않는다 (R-15).
     *
     * category_code는 SUBJECT 중분류여야 한다. 코드가 없으면 만들지 않고 예외를 던진다 (DR-C03).
     */
    @Transactional
    public Question createQuestion(Long examId, int sessionNo, int questionNo,
                                   String categoryCode, String title, String contents) {
        examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("회차를 찾을 수 없습니다."));
        assertLeafCategory(categoryCode);
        if (questionRepository.existsByExamIdAndSessionNoAndQuestionNo(examId, sessionNo, questionNo)) {
            throw new DomainException("같은 회차·교시에 같은 번호의 문항이 이미 있습니다.");
        }
        return questionRepository.save(
                Question.create(examId, sessionNo, questionNo, categoryCode, title, contents));
    }

    /**
     * 문항 전체 수정.
     *
     * examId를 함께 받는다. PUT은 보낸 표현으로 전체를 치환한다는 약속인데
     * 회차만 반영하지 않으면 요청은 성공하고 값은 그대로인 상태가 된다.
     *
     * 왜 중복 검사를 여기서 하는가: (exam_id, session_no, question_no)에 UNIQUE가
     * 걸려 있어 검사 없이 저장하면 DB 제약 위반이 커밋 시점에 터진다. 그 예외는
     * 업무 예외가 아니라서 500으로 나가고, 관리자는 무엇이 잘못됐는지 알 수 없다.
     */
    @Transactional
    public Question modifyQuestion(Long questionId, Long examId, int sessionNo, int questionNo,
                                   String categoryCode, String title, String contents) {
        Question question = findQuestion(questionId);
        examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("회차를 찾을 수 없습니다."));
        assertLeafCategory(categoryCode);

        boolean movedToAnotherSlot = !examId.equals(question.getExamId())
                || sessionNo != question.getSessionNo()
                || questionNo != question.getQuestionNo();
        if (movedToAnotherSlot
                && questionRepository.existsByExamIdAndSessionNoAndQuestionNo(examId, sessionNo, questionNo)) {
            throw new DomainException("같은 회차·교시에 같은 번호의 문항이 이미 있습니다.");
        }

        question.modify(examId, sessionNo, questionNo, categoryCode, title, contents);
        return question;
    }

    /**
     * 게시 (SCR-A04 ④).
     *
     * 왜 여기서 토큰을 검증하는가: 작성 중에는 이미지를 먼저 올리고 토큰을 나중에 넣으므로
     * 고아 토큰·미참조 도면이 정상적으로 존재한다. 검증은 게시 시점에만 한다 (DR-F03).
     *
     * 문제 본문과 해설 본문을 각각 검증한다. 도면 채번 단위가 소유자별이므로
     * 문항 도면과 해설 도면은 번호가 각각 1부터 시작한다 (DR-F01).
     */
    @Transactional
    public Question publishQuestion(Long questionId) {
        Question question = findQuestion(questionId);

        DrawingToken.validateForPublish(question.getContents(),
                drawingService.drawingNos(DrawingOwner.question(questionId)));

        solutionRepository.findByQuestionId(questionId).ifPresent(solution ->
                DrawingToken.validateForPublish(solution.getContents(),
                        drawingService.drawingNos(DrawingOwner.solution(solution.getSolutionId()))));

        question.publish();
        return question;
    }

    @Transactional
    public Question unpublishQuestion(Long questionId) {
        Question question = findQuestion(questionId);
        question.unpublish();
        return question;
    }

    /**
     * 삭제 (DR-L01).
     *
     * 물리 삭제하지 않고 QST003으로 바꾼다. 문항에 매달린 신고·추가풀이는
     * 그대로 보존하되 사용자에게 노출하지 않는다.
     */
    @Transactional
    public void deleteQuestion(Long questionId) {
        findQuestion(questionId).delete();
    }

    // ---------------------------------------------------------------- 해설

    /**
     * 해설 저장. 문항당 1건이므로 있으면 수정, 없으면 생성이다 (설계 3-3).
     *
     * 왜 upsert인가: 화면(SCR-A04 ③)이 문제·해설을 한 폼에서 다룬다.
     * 생성과 수정을 나누면 프론트가 해설 존재 여부를 먼저 조회해야 한다.
     */
    @Transactional
    public Solution saveSolution(Long questionId, String contents) {
        findQuestion(questionId);
        return solutionRepository.findByQuestionId(questionId)
                .map(solution -> {
                    solution.modify(contents);
                    return solution;
                })
                .orElseGet(() -> solutionRepository.save(Solution.create(questionId, contents)));
    }

    @Transactional(readOnly = true)
    public Optional<Solution> findSolution(Long questionId) {
        return solutionRepository.findByQuestionId(questionId);
    }

    // ---------------------------------------------------------------- 조회

    /** 관리자 목록. statusCode가 null이면 삭제를 뺀 전체다 (DR-L01 상태별 노출 범위). */
    @Transactional(readOnly = true)
    public List<Question> findQuestions(String statusCode) {
        return questionRepository.findForAdmin(statusCode);
    }

    @Transactional(readOnly = true)
    public Question findQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("문항을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Drawing> findDrawings(DrawingOwner owner) {
        return drawingService.findByOwner(owner);
    }

    @Transactional(readOnly = true)
    public long countDrafts() {
        return questionRepository.countByStatusCode(QStatus.DRAFT);
    }

    @Transactional(readOnly = true)
    public int examRound(Long examId) {
        return examRepository.findById(examId).map(Exam::getExamRound).orElse(0);
    }

    /**
     * 문항은 중분류(2단계) 코드만 저장한다 (DR-C06).
     *
     * 왜 대분류를 막는가: 같은 컬럼에 1단계와 2단계가 섞이면 "이 값이 말단인가"를
     * 매번 확인해야 한다.
     */
    private void assertLeafCategory(String categoryCode) {
        codeRegistry.resolve(Grp.SUBJECT, categoryCode);
        boolean leaf = codeRegistry.getGroup(Grp.SUBJECT).stream()
                .anyMatch(c -> c.codeValue().equals(categoryCode) && c.codeLevel() == 2);
        if (!leaf) {
            throw new DomainException("과목은 중분류를 지정해야 합니다.");
        }
    }
}
