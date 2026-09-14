package kr.co.csp.participation.service;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import kr.co.csp.common.code.CodeRegistry;
import kr.co.csp.common.code.SystemCode.EsStatus;
import kr.co.csp.common.code.SystemCode.Grp;
import kr.co.csp.common.code.SystemCode.QStatus;
import kr.co.csp.common.config.CspProperties;
import kr.co.csp.common.exception.DomainException;
import kr.co.csp.common.exception.NotFoundException;
import kr.co.csp.common.exception.RateLimitException;
import kr.co.csp.content.dto.DrawingResponse;
import kr.co.csp.content.entity.DrawingOwner;
import kr.co.csp.content.entity.Exam;
import kr.co.csp.content.entity.Question;
import kr.co.csp.content.repository.ExamRepository;
import kr.co.csp.content.repository.QuestionRepository;
import kr.co.csp.content.service.DrawingService;
import kr.co.csp.content.service.DrawingToken;
import kr.co.csp.content.service.QuestionQueryService;
import kr.co.csp.participation.dto.ExtraSolutionCreateRequest;
import kr.co.csp.participation.dto.ExtraSolutionResponse;
import kr.co.csp.participation.dto.ExtraSolutionStatusItem;
import kr.co.csp.participation.entity.ExtraSolution;
import kr.co.csp.participation.entity.LookupAttemptLog;
import kr.co.csp.participation.repository.ExtraSolutionRepository;
import kr.co.csp.participation.repository.LookupAttemptLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** 추가풀이 (SCR-006 · SCR-007 · REQ-U05 · REQ-U06). */
@Service
public class ExtraSolutionService {

    private static final Logger log = LoggerFactory.getLogger(ExtraSolutionService.class);

    private final ExtraSolutionRepository extraSolutionRepository;
    private final LookupAttemptLogRepository lookupAttemptLogRepository;
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final QuestionQueryService questionQueryService;
    private final DrawingService drawingService;
    private final CodeRegistry codeRegistry;
    private final PasswordEncoder passwordEncoder;
    private final CspProperties properties;

    public ExtraSolutionService(ExtraSolutionRepository extraSolutionRepository,
                                LookupAttemptLogRepository lookupAttemptLogRepository,
                                QuestionRepository questionRepository,
                                ExamRepository examRepository,
                                QuestionQueryService questionQueryService,
                                DrawingService drawingService,
                                CodeRegistry codeRegistry,
                                PasswordEncoder passwordEncoder,
                                CspProperties properties) {
        this.extraSolutionRepository = extraSolutionRepository;
        this.lookupAttemptLogRepository = lookupAttemptLogRepository;
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
        this.questionQueryService = questionQueryService;
        this.drawingService = drawingService;
        this.codeRegistry = codeRegistry;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    /**
     * 추가풀이 등록 (SCR-006).
     *
     * 등록 즉시 공개되지 않는다. 검토대기(EXS001)로 저장하고 관리자 검토를 거친다.
     * 비밀번호는 bcrypt 해시로만 저장한다. 평문도 양방향 암호화도 쓰지 않는다 (R-47 · DR-S02).
     *
     * 이미지는 본문과 같은 요청으로 받아 한 트랜잭션에서 저장한다 (DR-F03). 2단계로 나누면
     * 그 사이에 남의 추가풀이에 파일을 붙이는 요청을 막을 수 없다 — 로그인이 없어 소유자를
     * 확인할 방법이 없기 때문이다. 파일 저장이 실패하면 추가풀이도 함께 롤백된다 (DR-P03).
     */
    @Transactional
    public ExtraSolution create(ExtraSolutionCreateRequest request, List<MultipartFile> files,
                                InetAddress clientIp) {
        questionRepository.findByQuestionIdAndStatusCode(request.questionId(), QStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("문항을 찾을 수 없습니다."));

        assertWithinDailyLimit(clientIp);

        List<MultipartFile> attachments = files == null ? List.of()
                : files.stream().filter(f -> f != null && !f.isEmpty()).toList();
        assertWithinFileLimit(attachments);

        // 사용자가 친 토큰은 실제 도면과 맞을 수 없다. 서버가 지운다 (DR-F03 · DR-P04).
        ExtraSolution extra = extraSolutionRepository.save(ExtraSolution.create(
                request.questionId(),
                request.userName(),
                passwordEncoder.encode(request.password()),
                DrawingToken.removeAllTokens(request.contents()),
                clientIp));

        if (attachments.isEmpty()) {
            return extra;
        }

        // 도면 번호는 저장 후에야 정해진다. 받은 번호로 토큰을 만들어 본문 끝에 붙인다.
        DrawingOwner owner = DrawingOwner.extraSolution(extra.getExtraSolutionId());
        List<Integer> nos = attachments.stream()
                .map(file -> drawingService.addDrawing(owner, file).getDrawingNo())
                .toList();
        extra.attachDrawingTokens(DrawingToken.appendTokens(extra.getContents(), nos));
        return extra;
    }

    /** DR-F03 — 1건당 첨부 수. 건수 제한(R-46)과 곱한 값이 IP당 하루 상한이 된다. */
    private void assertWithinFileLimit(List<MultipartFile> files) {
        int max = properties.limit().extraSolutionFiles();
        if (files.size() > max) {
            throw new DomainException("이미지는 최대 %d장까지 첨부할 수 있습니다.".formatted(max));
        }
    }

    /** R-46 — 추가풀이 5회/일. 신고와 별개로 카운트한다 (안건 3 확정). */
    private void assertWithinDailyLimit(InetAddress clientIp) {
        long today = extraSolutionRepository
                .countByCreatedIpAndCreatedAtAfter(clientIp, DailyLimit.todayStart());
        if (today >= properties.limit().extraSolutionPerDay()) {
            throw new RateLimitException("오늘 추가풀이 등록 가능 횟수(%d회)를 모두 사용했습니다."
                    .formatted(properties.limit().extraSolutionPerDay()));
        }
    }

    /**
     * 현황 조회 (R-36 · SCR-007).
     *
     * 왜 원인을 구분하지 않는가 (DR-P05):
     * "ID가 없습니다"와 "비밀번호가 틀립니다"를 나누면 어떤 닉네임이 존재하는지
     * 알려주는 셈이 된다 (R-38).
     *
     * 왜 readOnly가 아닌가: 시도 로그를 남긴다. 성공·실패 모두 기록해야
     * 무차별 대입을 판별할 수 있다 (R-48).
     */
    @Transactional
    public List<ExtraSolutionStatusItem> findStatus(String userName, String rawPassword, InetAddress clientIp) {
        assertLookupNotBlocked(clientIp);

        List<ExtraSolution> matched = extraSolutionRepository
                .findByUserNameOrderByCreatedAtDesc(userName).stream()
                .filter(row -> passwordEncoder.matches(rawPassword, row.getPasswordHash()))
                .toList();

        lookupAttemptLogRepository.save(LookupAttemptLog.of(clientIp, !matched.isEmpty()));

        if (matched.isEmpty()) {
            throw new DomainException("일치하는 등록 내역이 없습니다. ID와 비밀번호를 확인해 주세요.");
        }

        Map<Long, Question> questions = questionMap(matched);
        Map<Long, Integer> rounds = examRoundMap(questions.values());

        return matched.stream()
                .map(row -> {
                    Question question = questions.get(row.getQuestionId());
                    return new ExtraSolutionStatusItem(
                            row.getExtraSolutionId(),
                            row.getQuestionId(),
                            question == null ? "(삭제된 문항)" : question.getTitle(),
                            question == null ? "" : sourceLabel(question, rounds),
                            row.getCreatedAt(),
                            row.getStatusCode(),
                            codeRegistry.getName(Grp.ES_STATUS, row.getStatusCode()),
                            row.getRejectReason());
                })
                .toList();
    }

    /**
     * R-48 — 무차별 대입 방지.
     *
     * 성공한 조회는 카운트하지 않는다. 본인이 여러 번 확인하는 것을 막을 이유가 없다.
     */
    private void assertLookupNotBlocked(InetAddress clientIp) {
        long fails = lookupAttemptLogRepository
                .countByCreatedIpAndSuccessFalseAndCreatedAtAfter(clientIp, DailyLimit.todayStart());
        if (fails >= properties.limit().lookupFailPerDay()) {
            throw new RateLimitException("조회 시도가 너무 많습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    /** 문항 상세에 붙는 게시된 추가풀이 (SCR-004). */
    @Transactional(readOnly = true)
    public List<ExtraSolutionResponse> findPublishedByQuestion(Long questionId) {
        return extraSolutionRepository
                .findByQuestionIdAndStatusCodeOrderByCreatedAtDesc(questionId, EsStatus.PUBLISHED).stream()
                .map(row -> {
                    List<DrawingResponse> drawings = questionQueryService
                            .findDrawings(DrawingOwner.extraSolution(row.getExtraSolutionId()));
                    return new ExtraSolutionResponse(row.getExtraSolutionId(), row.getUserName(),
                            row.getContents(), row.getCreatedAt(), drawings);
                })
                .toList();
    }

    // ---------------------------------------------------------------- 관리자

    @Transactional(readOnly = true)
    public List<ExtraSolution> findForAdmin(String statusCode) {
        return statusCode == null || statusCode.isBlank()
                ? extraSolutionRepository.findAllByOrderByCreatedAtDesc()
                : extraSolutionRepository.findByStatusCodeOrderByCreatedAtDesc(statusCode);
    }

    /**
     * 상태 전이 (SCR-A03).
     *
     * 검토대기 → 검토중 → 게시 / 반려. 반려 사유는 필수가 아니다 (R-39).
     */
    @Transactional
    public ExtraSolution changeStatus(Long extraSolutionId, String statusCode, String rejectReason) {
        ExtraSolution extra = extraSolutionRepository.findById(extraSolutionId)
                .orElseThrow(() -> new NotFoundException("추가풀이를 찾을 수 없습니다."));
        codeRegistry.resolve(Grp.ES_STATUS, statusCode);

        switch (statusCode) {
            case EsStatus.REVIEWING -> extra.startReview();
            case EsStatus.PUBLISHED -> extra.publish();
            case EsStatus.REJECTED -> extra.reject(rejectReason);
            default -> throw new DomainException("추가풀이 상태로 바꿀 수 없는 값입니다.");
        }
        return extra;
    }

    /**
     * 보관 기간이 지난 반려 건을 지운다 (DR-F02).
     *
     * 반려는 종료 상태라(assertOpen) 되돌릴 수 없다. 영구 보관할 이유가 없고, 그대로 두면
     * 디스크와 user_name·password_hash·created_ip가 무한정 쌓인다.
     *
     * 문항 물리 삭제 금지(DR-L01)의 예외가 아니다. 그 규칙은 tb_csp_con02가 대상이며
     * 사용자가 등록한 데이터가 함께 사라지는 것을 막으려는 것이다. 반려된 추가풀이는
     * 그 "사용자가 등록한 데이터" 자체이고 공개된 적이 없다.
     *
     * tb_csp_usr02를 참조하는 FK는 tb_csp_con04.extra_solution_id 하나다. 도면 행을
     * 먼저 지운다.
     *
     * 파일은 여기서 지우지 않고 file_uuid만 돌려준다. 호출부가 커밋 뒤에 지운다 —
     * 파일을 먼저 지우면 롤백됐을 때 DB에는 행이 있는데 파일이 없는 상태가 남는다.
     */
    @Transactional
    public List<UUID> purgeRejected(OffsetDateTime before) {
        List<ExtraSolution> expired =
                extraSolutionRepository.findByStatusCodeAndProcessedAtBefore(EsStatus.REJECTED, before);
        if (expired.isEmpty()) {
            return List.of();
        }

        List<UUID> fileUuids = new ArrayList<>();
        for (ExtraSolution extra : expired) {
            fileUuids.addAll(drawingService.deleteAllByExtraSolution(extra.getExtraSolutionId()));
        }
        extraSolutionRepository.deleteAll(expired);

        log.info("보관 기간이 지난 반려 추가풀이를 지웠습니다: {}건, 도면 {}개", expired.size(), fileUuids.size());
        return fileUuids;
    }

    /** SCR-A01 ② — 검토대기 + 검토중 합계. */
    @Transactional(readOnly = true)
    public long countPending() {
        return extraSolutionRepository.countByStatusCodeIn(List.of(EsStatus.WAITING, EsStatus.REVIEWING));
    }

    @Transactional(readOnly = true)
    public Map<Long, Question> questionMap(List<ExtraSolution> rows) {
        List<Long> ids = rows.stream().map(ExtraSolution::getQuestionId).distinct().toList();
        return questionRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Question::getQuestionId, q -> q));
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> examRoundMap(java.util.Collection<Question> questions) {
        List<Long> examIds = questions.stream().map(Question::getExamId).distinct().toList();
        Map<Long, Integer> map = new HashMap<>();
        for (Exam exam : examRepository.findAllById(examIds)) {
            map.put(exam.getExamId(), exam.getExamRound());
        }
        return map;
    }

    /** 출처 표기 (R-06) — 회차·교시·번호. */
    public String sourceLabel(Question question, Map<Long, Integer> rounds) {
        return "제%d회 %d교시 %d번".formatted(
                rounds.getOrDefault(question.getExamId(), 0),
                question.getSessionNo(),
                question.getQuestionNo());
    }
}
