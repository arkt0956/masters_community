package kr.co.csp.common.code;

/**
 * 로직이 분기에 쓰는 개별 코드값 (DR-C05).
 *
 * 왜 개별 값만 두는가: 드롭다운에 뿌릴 목록을 여기 두면 관리자가 코드를 하나 늘릴 때마다
 * 재배포가 필요해진다. 목록은 언제나 CodeRegistry로 DB에서 읽는다.
 */
public final class SystemCode {

    private SystemCode() {
    }

    /** 공통코드 그룹 (DR-N05) */
    public static final class Grp {
        public static final String SUBJECT = "SUBJECT";
        public static final String Q_STATUS = "Q_STATUS";
        public static final String RPT_TYPE = "RPT_TYPE";
        public static final String RPT_STATUS = "RPT_STATUS";
        public static final String ES_STATUS = "ES_STATUS";

        private Grp() {
        }
    }

    /** 문항 게시 상태 */
    public static final class QStatus {
        public static final String DRAFT = "QST001";      // 작성중
        public static final String PUBLISHED = "QST002";  // 게시됨
        public static final String DELETED = "QST003";    // 삭제 (DR-L01)

        private QStatus() {
        }
    }

    /** 신고 처리 상태 (설계 3-5 — 검토중 포함 4종) */
    public static final class RptStatus {
        public static final String RECEIVED = "RPS001";   // 접수
        public static final String REVIEWING = "RPS002";  // 검토중
        public static final String APPLIED = "RPS003";    // 반영
        public static final String REJECTED = "RPS004";   // 기각

        private RptStatus() {
        }
    }

    /** 추가풀이 처리 상태 (R-37) */
    public static final class EsStatus {
        public static final String WAITING = "EXS001";    // 검토대기
        public static final String REVIEWING = "EXS002";  // 검토중
        public static final String PUBLISHED = "EXS003";  // 게시
        public static final String REJECTED = "EXS004";   // 반려

        private EsStatus() {
        }
    }
}
