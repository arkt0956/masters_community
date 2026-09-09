package kr.co.csp.content.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.csp.common.code.CodeRegistry;
import kr.co.csp.common.code.SystemCode.Grp;
import kr.co.csp.common.code.SystemCode.QStatus;
import kr.co.csp.common.exception.NotFoundException;
import kr.co.csp.content.dto.DrawingResponse;
import kr.co.csp.content.dto.QuestionListItem;
import kr.co.csp.content.dto.SolutionResponse;
import kr.co.csp.content.entity.Drawing;
import kr.co.csp.content.entity.DrawingOwner;
import kr.co.csp.content.entity.Exam;
import kr.co.csp.content.entity.Question;
import kr.co.csp.content.entity.Solution;
import kr.co.csp.content.repository.DrawingRepository;
import kr.co.csp.content.repository.ExamRepository;
import kr.co.csp.content.repository.QuestionRepository;
import kr.co.csp.content.repository.SolutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 문항 조회 (SCR-004).
 *
 * 세 경로(회차별·과목별·랜덤)는 목록 데이터만 다르고 응답 형태는 같다.
 * 어느 경로든 게시 상태(QST002)가 아닌 문항은 나가지 않는다 (R-15).
 *
 * 이 서비스의 목록 메서드는 해설 본문을 절대 담지 않는다 (DR-A01).
 * 해설은 findSolution()으로만 나간다.
 */
@Service
public class QuestionQueryService {

    /** 랜덤 세트 크기. 한 화면에서 훑기에 적당한 분량으로 잡았다. */
    private static final int RANDOM_SIZE = 20;

    private final QuestionRepository questionRepository;
    private final SolutionRepository solutionRepository;
    private final DrawingRepository drawingRepository;
    private final ExamRepository examRepository;
    private final CodeRegistry codeRegistry;

    public QuestionQueryService(QuestionRepository questionRepository,
                                SolutionRepository solutionRepository,
                                DrawingRepository drawingRepository,
                                ExamRepository examRepository,
                                CodeRegistry codeRegistry) {
        this.questionRepository = questionRepository;
        this.solutionRepository = solutionRepository;
        this.drawingRepository = drawingRepository;
        this.examRepository = examRepository;
        this.codeRegistry = codeRegistry;
    }

    /** 회차·교시별 (SCR-002 → SCR-004). */
    @Transactional(readOnly = true)
    public List<QuestionListItem> findByExamSession(Long examId, int sessionNo) {
        return toListItems(questionRepository
                .findByExamIdAndSessionNoAndStatusCodeOrderByQuestionNoAsc(examId, sessionNo, QStatus.PUBLISHED));
    }

    /**
     * 과목별 (SCR-003 → SCR-004).
     *
     * 대분류·중분류 어느 쪽이 와도 받는다. 문항은 중분류만 저장하므로(DR-C04)
     * 대분류가 오면 parent_code로 조인해 찾는다.
     */
    @Transactional(readOnly = true)
    public List<QuestionListItem> findByCategory(String categoryCode) {
        boolean major = codeRegistry.getGroup(Grp.SUBJECT).stream()
                .anyMatch(c -> c.codeValue().equals(categoryCode) && c.codeLevel() == 1);
        List<Question> questions = major
                ? questionRepository.findByCategoryParent(categoryCode, QStatus.PUBLISHED)
                : questionRepository
                .findByCategoryCodeAndStatusCodeOrderByExamIdDescSessionNoAscQuestionNoAsc(
                        categoryCode, QStatus.PUBLISHED);
        return toListItems(questions);
    }

    /** 랜덤 연습 (SCR-001 ④). */
    @Transactional(readOnly = true)
    public List<QuestionListItem> findRandom() {
        return toListItems(questionRepository.findRandomPublished(RANDOM_SIZE));
    }

    /**
     * 해설 조회 (R-05 · R-09 · SCR-004 ⑤).
     *
     * 왜 문항 상태를 먼저 확인하는가: 문항이 비공개인데 해설만 열리면
     * 비공개 콘텐츠가 새어 나간다 (R-15).
     */
    @Transactional(readOnly = true)
    public SolutionResponse findSolution(Long questionId) {
        questionRepository.findByQuestionIdAndStatusCode(questionId, QStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("문항을 찾을 수 없습니다."));

        Solution solution = solutionRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new NotFoundException("해설이 아직 준비되지 않았습니다."));

        List<DrawingResponse> drawings =
                drawingRepository.findBySolutionIdOrderByDrawingNoAsc(solution.getSolutionId())
                        .stream().map(DrawingResponse::from).toList();

        return new SolutionResponse(solution.getSolutionId(), solution.getContents(), drawings);
    }

    /**
     * 목록 DTO 조립.
     *
     * 도면과 해설 보유 여부를 문항마다 따로 조회하면 N+1이 된다. 문항 id 목록으로
     * 한 번씩만 조회해 메모리에서 붙인다.
     *
     * 해설은 questionId만 읽는다. 본문을 읽지 않으므로 목록에 섞일 수 없다 (DR-A01).
     */
    private List<QuestionListItem> toListItems(List<Question> questions) {
        if (questions.isEmpty()) {
            return List.of();
        }
        List<Long> questionIds = questions.stream().map(Question::getQuestionId).toList();

        Map<Long, List<DrawingResponse>> drawingsByQuestion = drawingRepository
                .findByQuestionIdIn(questionIds).stream()
                .sorted((a, b) -> Integer.compare(a.getDrawingNo(), b.getDrawingNo()))
                .collect(Collectors.groupingBy(d -> d.owner().id(),
                        Collectors.mapping(DrawingResponse::from, Collectors.toList())));

        Set<Long> withSolution = new HashSet<>(solutionRepository.findQuestionIdsHavingSolution(questionIds));

        Map<Long, Integer> roundByExam = examRoundMap(questions);

        return questions.stream()
                .map(q -> new QuestionListItem(
                        q.getQuestionId(),
                        roundByExam.getOrDefault(q.getExamId(), 0),
                        q.getSessionNo(),
                        q.getQuestionNo(),
                        q.getCategoryCode(),
                        codeRegistry.getName(Grp.SUBJECT, q.getCategoryCode()),
                        q.getTitle(),
                        q.getContents(),
                        drawingsByQuestion.getOrDefault(q.getQuestionId(), List.of()),
                        withSolution.contains(q.getQuestionId())))
                .toList();
    }

    /**
     * 출처 배지(R-06)에 회차 번호가 필요하다. 문항은 exam_id만 들고 있다.
     * 랜덤 세트는 회차가 여러 개 섞이므로 한 번에 조회해 맵으로 만든다.
     */
    private Map<Long, Integer> examRoundMap(List<Question> questions) {
        Set<Long> examIds = questions.stream().map(Question::getExamId).collect(Collectors.toSet());
        Map<Long, Integer> map = new HashMap<>();
        for (Exam exam : examRepository.findAllById(examIds)) {
            map.put(exam.getExamId(), exam.getExamRound());
        }
        return map;
    }

    /** 도면 파일 서빙에 쓰는 조회 (DrawingController). */
    @Transactional(readOnly = true)
    public Drawing findDrawingByFileUuid(java.util.UUID fileUuid) {
        return drawingRepository.findByFileUuid(fileUuid)
                .orElseThrow(() -> new NotFoundException("도면을 찾을 수 없습니다."));
    }

    /** 추가풀이 본문에 붙은 도면 (SCR-004 게시된 추가풀이 표시용). */
    @Transactional(readOnly = true)
    public List<DrawingResponse> findDrawings(DrawingOwner owner) {
        List<Drawing> drawings = switch (owner.type()) {
            case QUESTION -> drawingRepository.findByQuestionIdOrderByDrawingNoAsc(owner.id());
            case SOLUTION -> drawingRepository.findBySolutionIdOrderByDrawingNoAsc(owner.id());
            case EXTRA_SOLUTION -> drawingRepository.findByExtraSolutionIdOrderByDrawingNoAsc(owner.id());
        };
        return drawings.stream().map(DrawingResponse::from).collect(Collectors.toList());
    }
}
