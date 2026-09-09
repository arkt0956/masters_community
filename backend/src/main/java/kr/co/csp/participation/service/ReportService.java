package kr.co.csp.participation.service;

import java.net.InetAddress;
import java.util.List;
import kr.co.csp.common.code.CodeRegistry;
import kr.co.csp.common.code.SystemCode.Grp;
import kr.co.csp.common.code.SystemCode.QStatus;
import kr.co.csp.common.code.SystemCode.RptStatus;
import kr.co.csp.common.config.CspProperties;
import kr.co.csp.common.exception.NotFoundException;
import kr.co.csp.common.exception.RateLimitException;
import kr.co.csp.content.repository.QuestionRepository;
import kr.co.csp.participation.dto.ReportCreateRequest;
import kr.co.csp.participation.entity.Report;
import kr.co.csp.participation.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 신고 (SCR-005 · REQ-U04). */
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final QuestionRepository questionRepository;
    private final CodeRegistry codeRegistry;
    private final CspProperties properties;

    public ReportService(ReportRepository reportRepository, QuestionRepository questionRepository,
                         CodeRegistry codeRegistry, CspProperties properties) {
        this.reportRepository = reportRepository;
        this.questionRepository = questionRepository;
        this.codeRegistry = codeRegistry;
        this.properties = properties;
    }

    /**
     * 신고 접수. 등록 즉시 접수(RPS001) 상태로 저장한다 (SCR-005 비고).
     *
     * clientIp는 Controller가 넘긴다. Service에 HttpServletRequest를 넘기지 않는다 (DR-P01).
     */
    @Transactional
    public Report create(ReportCreateRequest request, InetAddress clientIp) {
        // 게시된 문항만 신고 대상이다. 비공개 문항 id로 신고를 넣으면
        // 그 id의 존재 여부가 새어 나간다 (R-15).
        questionRepository.findByQuestionIdAndStatusCode(request.questionId(), QStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("문항을 찾을 수 없습니다."));

        // 코드가 없으면 만들지 않고 예외를 던진다 (DR-C03).
        codeRegistry.resolve(Grp.RPT_TYPE, request.typeCode());

        assertWithinDailyLimit(clientIp);

        return reportRepository.save(Report.create(
                request.questionId(), request.typeCode(), request.contents(), clientIp));
    }

    /**
     * R-46 — 신고 10회/일.
     *
     * 왜 등록 직전에 확인하는가: 화면에서 막아도 API를 직접 호출할 수 있다 (DR-P04).
     */
    private void assertWithinDailyLimit(InetAddress clientIp) {
        long today = reportRepository.countByCreatedIpAndCreatedAtAfter(clientIp, DailyLimit.todayStart());
        if (today >= properties.limit().reportPerDay()) {
            throw new RateLimitException(
                    "오늘 신고 가능 횟수(%d회)를 모두 사용했습니다.".formatted(properties.limit().reportPerDay()));
        }
    }

    // ---------------------------------------------------------------- 관리자

    @Transactional(readOnly = true)
    public List<Report> findForAdmin(String statusCode) {
        return statusCode == null || statusCode.isBlank()
                ? reportRepository.findAllByOrderByCreatedAtDesc()
                : reportRepository.findByStatusCodeOrderByCreatedAtDesc(statusCode);
    }

    /**
     * 상태 전이 (SCR-A02 ④).
     *
     * 반영은 원문 수정 게시로 이어진다. 문항 수정 자체는 SCR-A04에서 하므로
     * 여기서는 신고 상태만 바꾼다.
     */
    @Transactional
    public Report changeStatus(Long reportId, String statusCode) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("신고를 찾을 수 없습니다."));
        codeRegistry.resolve(Grp.RPT_STATUS, statusCode);
        report.changeStatus(statusCode);
        return report;
    }

    /** SCR-A01 ① — 접수됨 + 검토중 합계 (설계 3-5). */
    @Transactional(readOnly = true)
    public long countPending() {
        return reportRepository.countByStatusCodeIn(List.of(RptStatus.RECEIVED, RptStatus.REVIEWING));
    }
}
