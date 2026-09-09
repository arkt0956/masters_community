package kr.co.csp.content.service;

import java.util.List;
import kr.co.csp.common.code.SystemCode.QStatus;
import kr.co.csp.content.dto.ExamResponse;
import kr.co.csp.content.entity.Exam;
import kr.co.csp.content.repository.ExamRepository;
import kr.co.csp.content.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 회차 조회 (SCR-001 · SCR-002). */
@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    public ExamService(ExamRepository examRepository, QuestionRepository questionRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * 사용자 화면용 회차 목록 (R-01 · R-03).
     *
     * 왜 교시를 함께 담는가: 교시는 테이블이 아니라 문항의 session_no 집계다(설계 3-2).
     * 화면이 교시 목록을 따로 요청하면 회차마다 왕복이 한 번씩 더 생긴다.
     *
     * 게시 문항이 하나도 없는 교시는 목록에 담기지 않는다. 화면은 받은 것만 그리면
     * "콘텐츠 없는 교시 선택 불가"(R-03)가 자연히 지켜진다.
     */
    @Transactional(readOnly = true)
    public List<ExamResponse> findPublicExams() {
        return examRepository.findByPublicExamTrueOrderByExamRoundDesc().stream()
                .map(this::toResponse)
                .filter(exam -> !exam.sessions().isEmpty())
                .toList();
    }

    /** 관리자 목록. 비공개 회차도 보여야 편집할 수 있다. */
    @Transactional(readOnly = true)
    public List<Exam> findAllForAdmin() {
        return examRepository.findAllByOrderByExamRoundDesc();
    }

    private ExamResponse toResponse(Exam exam) {
        List<ExamResponse.SessionSummary> sessions =
                questionRepository.countBySession(exam.getExamId(), QStatus.PUBLISHED).stream()
                        .map(row -> new ExamResponse.SessionSummary(
                                ((Number) row[0]).intValue(), ((Number) row[1]).longValue()))
                        .toList();
        return new ExamResponse(exam.getExamId(), exam.getExamRound(), sessions);
    }
}
