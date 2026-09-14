package kr.co.csp.participation.repository;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.List;
import kr.co.csp.participation.entity.ExtraSolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExtraSolutionRepository extends JpaRepository<ExtraSolution, Long> {

    /** R-46 — 추가풀이 5회/일. idx_tb_csp_usr02_ip가 받는다. */
    long countByCreatedIpAndCreatedAtAfter(InetAddress createdIp, OffsetDateTime since);

    /**
     * 현황 조회 (R-36).
     *
     * 왜 닉네임으로만 조회하는가: 비밀번호는 bcrypt 해시라 DB에서 비교할 수 없다.
     * 같은 닉네임의 행을 모두 읽어 온 뒤 애플리케이션에서 matches로 걸러 낸다.
     * idx_tb_csp_usr02_user가 이 조회를 받는다.
     */
    List<ExtraSolution> findByUserNameOrderByCreatedAtDesc(String userName);

    List<ExtraSolution> findByStatusCodeOrderByCreatedAtDesc(String statusCode);

    List<ExtraSolution> findAllByOrderByCreatedAtDesc();

    /** 보관 기간이 지난 반려 건 (DR-F02). processed_at은 reject()가 채운다. */
    List<ExtraSolution> findByStatusCodeAndProcessedAtBefore(String statusCode, OffsetDateTime before);

    /** 문항 상세에 붙여 보여줄 게시된 추가풀이 (R-37). */
    List<ExtraSolution> findByQuestionIdAndStatusCodeOrderByCreatedAtDesc(Long questionId, String statusCode);

    /** SCR-A01 대시보드 — 검토대기 + 검토중 합계. */
    @Query("select count(e) from ExtraSolution e where e.statusCode in :statusCodes")
    long countByStatusCodeIn(@Param("statusCodes") List<String> statusCodes);
}
