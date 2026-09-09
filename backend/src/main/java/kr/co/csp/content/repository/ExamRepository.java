package kr.co.csp.content.repository;

import java.util.List;
import java.util.Optional;
import kr.co.csp.content.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    /** R-01 — 공개 회차만 사용자 화면에 노출한다. */
    List<Exam> findByPublicExamTrueOrderByExamRoundDesc();

    List<Exam> findAllByOrderByExamRoundDesc();

    Optional<Exam> findByExamRound(int examRound);

    boolean existsByExamRound(int examRound);
}
