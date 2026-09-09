package kr.co.csp.content.repository;

import java.util.List;
import java.util.Optional;
import kr.co.csp.content.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 문항 조회 (DR-A01).
 *
 * 이 Repository는 tb_csp_con02만 읽는다. tb_csp_con03(해설)을 조인하는 메서드를
 * 여기에 추가하면 목록 응답에 해설이 섞일 길이 열린다. 해설은 SolutionRepository로만 읽는다.
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** 사용자 상세 조회. 게시 상태가 아니면 없는 것으로 취급한다 (R-15). */
    Optional<Question> findByQuestionIdAndStatusCode(Long questionId, String statusCode);

    List<Question> findByExamIdAndSessionNoAndStatusCodeOrderByQuestionNoAsc(
            Long examId, int sessionNo, String statusCode);

    /**
     * 과목 중분류로 조회 (R-22).
     * 문항은 중분류 값만 저장한다. 대분류 필터는 아래 findByCategoryParent를 쓴다 (DR-C04).
     */
    List<Question> findByCategoryCodeAndStatusCodeOrderByExamIdDescSessionNoAscQuestionNoAsc(
            String categoryCode, String statusCode);

    /**
     * 과목 대분류로 조회 (설계 3-11).
     *
     * 왜 서브쿼리인가: 문항이 대분류 코드를 직접 저장하지 않기 때문이다.
     * 같은 정보를 두 컬럼에 두면 중분류를 다른 대분류로 옮길 때 두 곳이 어긋난다.
     */
    @Query("""
            select q from Question q
            where q.statusCode = :statusCode
              and q.categoryCode in (
                  select c.codeValue from CommonCode c
                  where c.groupCode = 'SUBJECT' and c.parentCode = :parentCode and c.active = true)
            order by q.examId desc, q.sessionNo asc, q.questionNo asc
            """)
    List<Question> findByCategoryParent(@Param("parentCode") String parentCode,
                                        @Param("statusCode") String statusCode);

    /** 교시별 게시 문항 수 (R-03 — 콘텐츠 없는 교시는 선택 불가). */
    @Query("""
            select q.sessionNo, count(q)
            from Question q
            where q.examId = :examId and q.statusCode = :statusCode
            group by q.sessionNo
            order by q.sessionNo
            """)
    List<Object[]> countBySession(@Param("examId") Long examId, @Param("statusCode") String statusCode);

    /** 과목(중분류)별 게시 문항 수 (SCR-003 ②). */
    @Query("""
            select q.categoryCode, count(q)
            from Question q
            where q.statusCode = :statusCode
            group by q.categoryCode
            """)
    List<Object[]> countByCategory(@Param("statusCode") String statusCode);

    /**
     * 랜덤 세트 (SCR-001 ④).
     *
     * 왜 네이티브 쿼리인가: JPQL에 무작위 정렬이 없다. 물리 테이블명이 나타나는 것은
     * @Table과 네이티브 쿼리에서만 허용된다 (DR-N06).
     *
     * 왜 애플리케이션에서 셔플하지 않는가: 전체 문항을 다 읽어 와야 한다.
     */
    @Query(value = """
            select * from tb_csp_con02
            where status_code = 'QST002'
            order by random()
            limit :size
            """, nativeQuery = true)
    List<Question> findRandomPublished(@Param("size") int size);

    /** 관리자 기본 목록. 삭제 문항은 별도 필터를 켜야 보인다 (DR-L01). */
    @Query("""
            select q from Question q
            where (:statusCode is null and q.statusCode <> 'QST003' or q.statusCode = :statusCode)
            order by q.examId desc, q.sessionNo asc, q.questionNo asc
            """)
    List<Question> findForAdmin(@Param("statusCode") String statusCode);

    long countByStatusCode(String statusCode);

    boolean existsByExamIdAndSessionNoAndQuestionNo(Long examId, int sessionNo, int questionNo);

    List<Question> findByExamId(Long examId);
}
