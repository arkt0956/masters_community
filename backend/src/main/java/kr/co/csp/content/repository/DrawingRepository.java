package kr.co.csp.content.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kr.co.csp.content.entity.Drawing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DrawingRepository extends JpaRepository<Drawing, Long> {

    List<Drawing> findByQuestionIdOrderByDrawingNoAsc(Long questionId);

    List<Drawing> findBySolutionIdOrderByDrawingNoAsc(Long solutionId);

    List<Drawing> findByExtraSolutionIdOrderByDrawingNoAsc(Long extraSolutionId);

    List<Drawing> findByQuestionIdIn(List<Long> questionIds);

    Optional<Drawing> findByFileUuid(UUID fileUuid);

    /**
     * 소유자별 최대 도면 번호 (DR-F01).
     *
     * 왜 COUNT가 아니라 MAX인가: 빈 번호를 재사용하지 않기 때문이다.
     * 1·3만 남은 상태에서 COUNT + 1을 쓰면 3이 나와 충돌한다.
     */
    @Query("select max(d.drawingNo) from Drawing d where d.questionId = :questionId")
    Optional<Integer> findMaxNoByQuestionId(Long questionId);

    @Query("select max(d.drawingNo) from Drawing d where d.solutionId = :solutionId")
    Optional<Integer> findMaxNoBySolutionId(Long solutionId);

    @Query("select max(d.drawingNo) from Drawing d where d.extraSolutionId = :extraSolutionId")
    Optional<Integer> findMaxNoByExtraSolutionId(Long extraSolutionId);
}
