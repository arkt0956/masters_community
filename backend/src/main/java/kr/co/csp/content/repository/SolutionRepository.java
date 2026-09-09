package kr.co.csp.content.repository;

import java.util.List;
import java.util.Optional;
import kr.co.csp.content.entity.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SolutionRepository extends JpaRepository<Solution, Long> {

    Optional<Solution> findByQuestionId(Long questionId);

    /**
     * 목록 화면의 "해설 보유 여부"(SCR-004 ⑤)만 알기 위한 조회.
     *
     * 왜 본문을 읽지 않는가: 해설 본문이 목록 응답에 섞이면 안 된다 (DR-A01).
     * questionId만 돌려주므로 본문이 딸려 나갈 여지가 없다.
     */
    @Query("select s.questionId from Solution s where s.questionId in :questionIds")
    List<Long> findQuestionIdsHavingSolution(List<Long> questionIds);
}
