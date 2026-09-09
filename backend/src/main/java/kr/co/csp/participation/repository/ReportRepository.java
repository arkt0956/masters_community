package kr.co.csp.participation.repository;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.List;
import kr.co.csp.participation.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * IP 일일 제한 집계 (R-46 — 신고 10회/일).
     *
     * 왜 별도 카운터 테이블을 두지 않는가: 이 테이블에 이미 created_ip와 created_at이
     * 있으므로 집계로 충분하다 (설계 3-4). idx_tb_csp_usr01_ip가 이 조회를 받는다.
     */
    long countByCreatedIpAndCreatedAtAfter(InetAddress createdIp, OffsetDateTime since);

    List<Report> findByStatusCodeOrderByCreatedAtDesc(String statusCode);

    List<Report> findAllByOrderByCreatedAtDesc();

    /** SCR-A01 대시보드 — 접수됨 + 검토중 합계 (설계 3-5). */
    @Query("select count(r) from Report r where r.statusCode in :statusCodes")
    long countByStatusCodeIn(@Param("statusCodes") List<String> statusCodes);
}
