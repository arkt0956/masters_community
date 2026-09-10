package kr.co.csp.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.csp.admin.dto.AdminExtraSolutionItem;
import kr.co.csp.admin.dto.AdminQuestionDetail;
import kr.co.csp.admin.dto.AdminQuestionItem;
import kr.co.csp.admin.dto.AdminReportItem;
import kr.co.csp.common.code.CodeRegistry;
import kr.co.csp.common.code.SystemCode.Grp;
import kr.co.csp.content.dto.DrawingResponse;
import kr.co.csp.content.entity.Drawing;
import kr.co.csp.content.entity.DrawingOwner;
import kr.co.csp.content.entity.Exam;
import kr.co.csp.content.entity.Question;
import kr.co.csp.content.entity.Solution;
import kr.co.csp.content.repository.ExamRepository;
import kr.co.csp.content.repository.QuestionRepository;
import kr.co.csp.content.repository.SolutionRepository;
import kr.co.csp.content.service.ContentAdminService;
import kr.co.csp.participation.entity.ExtraSolution;
import kr.co.csp.participation.entity.Report;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 화면 응답 조립.
 *
 * 왜 Controller가 아니라 여기서 조립하는가:
 * 신고·추가풀이 목록 모두 "대상 문항 제목 + 출처 표기"를 붙여야 한다 (SCR-A02 ② · SCR-A03 ②).
 * 각 Controller가 따로 만들면 문항·회차 조회가 중복되고 N+1이 생긴다.
 */
@Service
public class AdminViewAssembler {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final SolutionRepository solutionRepository;
    private final ContentAdminService contentAdminService;
    private final CodeRegistry codeRegistry;

    public AdminViewAssembler(QuestionRepository questionRepository, ExamRepository examRepository,
                              SolutionRepository solutionRepository,
                              ContentAdminService contentAdminService, CodeRegistry codeRegistry) {
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
        this.solutionRepository = solutionRepository;
        this.contentAdminService = contentAdminService;
        this.codeRegistry = codeRegistry;
    }

    @Transactional(readOnly = true)
    public List<AdminReportItem> reports(List<Report> reports) {
        Context context = contextOf(reports.stream().map(Report::getQuestionId).toList());
        return reports.stream()
                .map(r -> new AdminReportItem(
                        r.getReportId(),
                        r.getQuestionId(),
                        context.title(r.getQuestionId()),
                        context.sourceLabel(r.getQuestionId()),
                        r.getTypeCode(),
                        codeRegistry.getName(Grp.RPT_TYPE, r.getTypeCode()),
                        r.getContents(),
                        r.getStatusCode(),
                        codeRegistry.getName(Grp.RPT_STATUS, r.getStatusCode()),
                        r.getCreatedAt(),
                        r.getProcessedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminExtraSolutionItem> extraSolutions(List<ExtraSolution> rows) {
        Context context = contextOf(rows.stream().map(ExtraSolution::getQuestionId).toList());
        return rows.stream()
                .map(e -> new AdminExtraSolutionItem(
                        e.getExtraSolutionId(),
                        e.getQuestionId(),
                        context.title(e.getQuestionId()),
                        context.sourceLabel(e.getQuestionId()),
                        e.getUserName(),
                        e.getContents(),
                        e.getStatusCode(),
                        codeRegistry.getName(Grp.ES_STATUS, e.getStatusCode()),
                        e.getRejectReason(),
                        e.getCreatedAt(),
                        e.getProcessedAt(),
                        contentAdminService
                                .findDrawings(DrawingOwner.extraSolution(e.getExtraSolutionId()))
                                .stream().map(DrawingResponse::from).toList()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminQuestionItem> questions(List<Question> questions) {
        if (questions.isEmpty()) {
            return List.of();
        }
        List<Long> ids = questions.stream().map(Question::getQuestionId).toList();
        Set<Long> withSolution = Set.copyOf(solutionRepository.findQuestionIdsHavingSolution(ids));
        Map<Long, Integer> rounds = roundMap(questions);

        return questions.stream()
                .map(q -> new AdminQuestionItem(
                        q.getQuestionId(),
                        q.getExamId(),
                        rounds.getOrDefault(q.getExamId(), 0),
                        q.getSessionNo(),
                        q.getQuestionNo(),
                        q.getCategoryCode(),
                        codeRegistry.getName(Grp.SUBJECT, q.getCategoryCode()),
                        q.getTitle(),
                        q.getStatusCode(),
                        codeRegistry.getName(Grp.Q_STATUS, q.getStatusCode()),
                        withSolution.contains(q.getQuestionId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminQuestionDetail questionDetail(Question question) {
        List<DrawingResponse> questionDrawings = contentAdminService
                .findDrawings(DrawingOwner.question(question.getQuestionId()))
                .stream().map(DrawingResponse::from).toList();

        AdminQuestionDetail.SolutionPart solutionPart = contentAdminService
                .findSolution(question.getQuestionId())
                .map(this::toSolutionPart)
                .orElse(null);

        return new AdminQuestionDetail(
                question.getQuestionId(),
                question.getExamId(),
                contentAdminService.examRound(question.getExamId()),
                question.getSessionNo(),
                question.getQuestionNo(),
                question.getCategoryCode(),
                codeRegistry.getName(Grp.SUBJECT, question.getCategoryCode()),
                question.getTitle(),
                question.getContents(),
                question.getStatusCode(),
                codeRegistry.getName(Grp.Q_STATUS, question.getStatusCode()),
                questionDrawings,
                solutionPart);
    }

    private AdminQuestionDetail.SolutionPart toSolutionPart(Solution solution) {
        List<Drawing> drawings = contentAdminService.findDrawings(
                DrawingOwner.solution(solution.getSolutionId()));
        return new AdminQuestionDetail.SolutionPart(
                solution.getSolutionId(),
                solution.getContents(),
                drawings.stream().map(DrawingResponse::from).toList());
    }

    /** 문항 제목·출처 표기를 한 번에 붙이기 위한 조회 결과. */
    private Context contextOf(List<Long> questionIds) {
        List<Question> questions = questionRepository.findAllById(questionIds.stream().distinct().toList());
        Map<Long, Question> byId = questions.stream()
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));
        return new Context(byId, roundMap(questions));
    }

    private Map<Long, Integer> roundMap(List<Question> questions) {
        List<Long> examIds = questions.stream().map(Question::getExamId).distinct().toList();
        Map<Long, Integer> map = new HashMap<>();
        for (Exam exam : examRepository.findAllById(examIds)) {
            map.put(exam.getExamId(), exam.getExamRound());
        }
        return map;
    }

    private record Context(Map<Long, Question> questions, Map<Long, Integer> rounds) {

        String title(Long questionId) {
            Question question = questions.get(questionId);
            return question == null ? "(삭제된 문항)" : question.getTitle();
        }

        /** 출처 표기 (R-06) — 회차·교시·번호. */
        String sourceLabel(Long questionId) {
            Question question = questions.get(questionId);
            if (question == null) {
                return "";
            }
            return "제%d회 %d교시 %d번".formatted(
                    rounds.getOrDefault(question.getExamId(), 0),
                    question.getSessionNo(),
                    question.getQuestionNo());
        }
    }
}
