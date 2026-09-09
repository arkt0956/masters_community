-- DB설계서_ERD v0.18 · 2장 DDL 그대로. 명명은 DR-N01 · DR-N04, 주석은 DR-N07.
-- 테이블 생성 순서는 FK 의존을 따른다(con04가 usr02를 참조하므로 con04를 뒤로 뺐다).

-- ============================ 2-1. 콘텐츠 (con) ============================

CREATE TABLE tb_csp_con01 (
                                            exam_id     BIGINT GENERATED ALWAYS AS IDENTITY,
                                            exam_round  INT NOT NULL,
                                            is_public   BOOLEAN NOT NULL DEFAULT FALSE,
                                            created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_ip  INET NOT NULL,
    CONSTRAINT pk_tb_csp_con01 PRIMARY KEY (exam_id),
    CONSTRAINT uk_tb_csp_con01_round UNIQUE (exam_round)
    );

COMMENT ON TABLE  tb_csp_con01            IS '회차 — 시험 회차와 공개 여부';
COMMENT ON COLUMN tb_csp_con01.exam_id    IS '회차 ID (대리키). 회차 번호 정정이 가능하도록 자연키와 분리 (설계 3-1)';
COMMENT ON COLUMN tb_csp_con01.exam_round IS '회차 번호. 년도는 표기하지 않음 (안건6)';
COMMENT ON COLUMN tb_csp_con01.is_public  IS '공개 여부. 공개 회차만 목록 노출 (R-01)';
COMMENT ON COLUMN tb_csp_con01.created_at IS '등록일시';
COMMENT ON COLUMN tb_csp_con01.updated_at IS '수정일시. 수정 시 updated_ip와 함께 반드시 갱신 (DR-L02)';
COMMENT ON COLUMN tb_csp_con01.updated_ip IS '마지막 수정자 IP. 수정 시 updated_at과 함께 반드시 갱신 (DR-L02)';

CREATE TABLE tb_csp_con02 (
                                            question_id   BIGINT GENERATED ALWAYS AS IDENTITY,
                                            exam_id       BIGINT NOT NULL,
                                            session_no    INT NOT NULL,
                                            question_no   INT NOT NULL,
                                            category_code VARCHAR(20) NOT NULL,
    title         TEXT NOT NULL,
    contents      TEXT NOT NULL,
    status_code   VARCHAR(20) NOT NULL DEFAULT 'QST001',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_ip    INET NOT NULL,
    CONSTRAINT pk_tb_csp_con02 PRIMARY KEY (question_id),
    CONSTRAINT fk_tb_csp_con02_con01 FOREIGN KEY (exam_id) REFERENCES tb_csp_con01 (exam_id),
    CONSTRAINT uk_tb_csp_con02_no UNIQUE (exam_id, session_no, question_no)
    );

CREATE INDEX idx_tb_csp_con02_public
    ON tb_csp_con02 (exam_id, session_no) WHERE status_code = 'QST002';   -- R-15
CREATE INDEX idx_tb_csp_con02_category
    ON tb_csp_con02 (category_code) WHERE status_code = 'QST002';         -- R-22

COMMENT ON TABLE  tb_csp_con02               IS '문항 — 문제 본문·출처·과목·게시 상태';
COMMENT ON COLUMN tb_csp_con02.question_id   IS '문항 ID';
COMMENT ON COLUMN tb_csp_con02.exam_id       IS '소속 회차 (tb_csp_con01)';
COMMENT ON COLUMN tb_csp_con02.session_no    IS '교시. 교시 구성은 모든 회차 동일 (R-02)';
COMMENT ON COLUMN tb_csp_con02.question_no   IS '회차·교시 내 문항 번호 (R-04). 원문 게재이므로 이 값이 곧 원 기출의 문항 번호 (R-06)';
COMMENT ON COLUMN tb_csp_con02.category_code IS '과목 중분류 코드 (SUBJECT code_level=2, R-40). 큐넷 출제기준 체계, 개발규칙 DR-C06 · 부록 B';
COMMENT ON COLUMN tb_csp_con02.title         IS '문항 제목';
COMMENT ON COLUMN tb_csp_con02.contents      IS '문제 본문. 이미지 위치는 [[drawing:n]] 토큰으로 표기 (DR-F03)';
COMMENT ON COLUMN tb_csp_con02.status_code   IS '게시 상태 (Q_STATUS: QST001 작성중, QST002 게시됨, QST003 삭제)';
COMMENT ON COLUMN tb_csp_con02.created_at    IS '등록일시';
COMMENT ON COLUMN tb_csp_con02.updated_at    IS '수정일시. 수정 시 updated_ip와 함께 반드시 갱신 (DR-L02)';
COMMENT ON COLUMN tb_csp_con02.updated_ip    IS '마지막 수정자 IP. 수정 시 updated_at과 함께 반드시 갱신 (DR-L02)';

CREATE TABLE tb_csp_con03 (
                                            solution_id BIGINT GENERATED ALWAYS AS IDENTITY,
                                            question_id BIGINT NOT NULL,
                                            contents    TEXT NOT NULL,
                                            created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_ip  INET NOT NULL,
    CONSTRAINT pk_tb_csp_con03 PRIMARY KEY (solution_id),
    CONSTRAINT fk_tb_csp_con03_con02 FOREIGN KEY (question_id) REFERENCES tb_csp_con02 (question_id),
    CONSTRAINT uk_tb_csp_con03_question UNIQUE (question_id)
    );

COMMENT ON TABLE  tb_csp_con03             IS '해설 — 모범답안. 문항과 1:0..1';
COMMENT ON COLUMN tb_csp_con03.solution_id IS '해설 ID';
COMMENT ON COLUMN tb_csp_con03.question_id IS '대상 문항. 문항당 해설 1건 (R-54)';
COMMENT ON COLUMN tb_csp_con03.contents    IS '모범답안 본문. 이미지 위치는 [[drawing:n]] 토큰. 목록 API에서 조회 금지 (R-09·R-54, DR-A01)';
COMMENT ON COLUMN tb_csp_con03.created_at  IS '등록일시';
COMMENT ON COLUMN tb_csp_con03.updated_at  IS '수정일시. 수정 시 updated_ip와 함께 반드시 갱신 (DR-L02)';
COMMENT ON COLUMN tb_csp_con03.updated_ip  IS '마지막 수정자 IP. 수정 시 updated_at과 함께 반드시 갱신 (DR-L02)';

-- ============================ 2-2. 사용자 참여 (usr) ============================

CREATE TABLE tb_csp_usr01 (
                                            report_id    BIGINT GENERATED ALWAYS AS IDENTITY,
                                            question_id  BIGINT NOT NULL,
                                            type_code    VARCHAR(20) NOT NULL,
    contents     TEXT NOT NULL,
    status_code  VARCHAR(20) NOT NULL DEFAULT 'RPS001',
    created_ip   INET NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ,
    CONSTRAINT pk_tb_csp_usr01 PRIMARY KEY (report_id),
    CONSTRAINT fk_tb_csp_usr01_con02 FOREIGN KEY (question_id) REFERENCES tb_csp_con02 (question_id)
    );

CREATE INDEX idx_tb_csp_usr01_ip ON tb_csp_usr01 (created_ip, created_at);  -- R-46 일 10회

COMMENT ON TABLE  tb_csp_usr01              IS '신고 — 오탈자·오답·오분류 신고';
COMMENT ON COLUMN tb_csp_usr01.report_id    IS '신고 ID';
COMMENT ON COLUMN tb_csp_usr01.question_id  IS '대상 문항';
COMMENT ON COLUMN tb_csp_usr01.type_code    IS '신고 유형 (RPT_TYPE: RPT001 오탈자, RPT002 오답, RPT003 오분류)';
COMMENT ON COLUMN tb_csp_usr01.contents     IS '상세 내용. 필수, 길이 제한 없음 (R-29)';
COMMENT ON COLUMN tb_csp_usr01.status_code  IS '처리 상태 (RPT_STATUS: RPS001 접수, RPS002 검토중, RPS003 반영, RPS004 기각 / R-51)';
COMMENT ON COLUMN tb_csp_usr01.created_ip   IS '요청자 IP. 프록시 헤더 기준 (R-52, DR-S01)';
COMMENT ON COLUMN tb_csp_usr01.created_at   IS '등록일시';
COMMENT ON COLUMN tb_csp_usr01.processed_at IS '관리자 처리일시';

CREATE TABLE tb_csp_usr02 (
                                            extra_solution_id BIGINT GENERATED ALWAYS AS IDENTITY,
                                            question_id       BIGINT NOT NULL,
                                            user_name         VARCHAR(12) NOT NULL,
    password_hash     VARCHAR(72) NOT NULL,
    contents          TEXT NOT NULL,
    status_code       VARCHAR(20) NOT NULL DEFAULT 'EXS001',
    reject_reason     TEXT,
    created_ip        INET NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at      TIMESTAMPTZ,
    CONSTRAINT pk_tb_csp_usr02 PRIMARY KEY (extra_solution_id),
    CONSTRAINT fk_tb_csp_usr02_con02 FOREIGN KEY (question_id) REFERENCES tb_csp_con02 (question_id)
    );

CREATE INDEX idx_tb_csp_usr02_user ON tb_csp_usr02 (user_name);               -- R-36
CREATE INDEX idx_tb_csp_usr02_ip   ON tb_csp_usr02 (created_ip, created_at);  -- R-46 일 5회

COMMENT ON TABLE  tb_csp_usr02                   IS '추가풀이 — 사용자 등록 풀이. 검토 후 게시';
COMMENT ON COLUMN tb_csp_usr02.extra_solution_id IS '추가풀이 ID';
COMMENT ON COLUMN tb_csp_usr02.question_id       IS '대상 문항';
COMMENT ON COLUMN tb_csp_usr02.user_name         IS '작성자 닉네임 2~12자. 회원 계정 아님. 중복 허용';
COMMENT ON COLUMN tb_csp_usr02.password_hash     IS '현황 조회 키 해시 (bcrypt). 평문 저장 금지 (R-47, DR-S02)';
COMMENT ON COLUMN tb_csp_usr02.contents          IS '풀이 내용. 이미지 위치는 [[drawing:n]] 토큰 (DR-F03)';
COMMENT ON COLUMN tb_csp_usr02.status_code       IS '처리 상태 (ES_STATUS: EXS001 검토대기, EXS002 검토중, EXS003 게시, EXS004 반려 / R-37)';
COMMENT ON COLUMN tb_csp_usr02.reject_reason     IS '반려 사유. 필수 아님. 있으면 SCR-007 표시 (R-39)';
COMMENT ON COLUMN tb_csp_usr02.created_ip        IS '요청자 IP. 프록시 헤더 기준 (R-52, DR-S01)';
COMMENT ON COLUMN tb_csp_usr02.created_at        IS '등록일시';
COMMENT ON COLUMN tb_csp_usr02.processed_at      IS '관리자 처리일시';

-- ============================ 도면 (con04) ============================
-- 소유자에 usr02가 포함되므로 usr02 뒤에 만든다.

CREATE TABLE tb_csp_con04 (
                                            drawing_id        BIGINT GENERATED ALWAYS AS IDENTITY,
                                            question_id       BIGINT,
                                            solution_id       BIGINT,
                                            extra_solution_id BIGINT,
                                            drawing_no        INT NOT NULL,
                                            file_uuid         UUID NOT NULL DEFAULT gen_random_uuid(),
    file_name         VARCHAR(255) NOT NULL,
    file_hash         CHAR(64) NOT NULL,
    file_type         VARCHAR(100) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_tb_csp_con04 PRIMARY KEY (drawing_id),
    CONSTRAINT fk_tb_csp_con04_con02 FOREIGN KEY (question_id)       REFERENCES tb_csp_con02 (question_id),
    CONSTRAINT fk_tb_csp_con04_con03 FOREIGN KEY (solution_id)       REFERENCES tb_csp_con03 (solution_id),
    CONSTRAINT fk_tb_csp_con04_usr02 FOREIGN KEY (extra_solution_id) REFERENCES tb_csp_usr02 (extra_solution_id),
    CONSTRAINT uk_tb_csp_con04_file UNIQUE (file_uuid),
    CONSTRAINT ck_tb_csp_con04_no   CHECK (drawing_no >= 1),
    -- 소유자는 문항·해설·추가풀이 중 정확히 하나
    CONSTRAINT ck_tb_csp_con04_owner CHECK (
(question_id       IS NOT NULL)::int
    + (solution_id       IS NOT NULL)::int
    + (extra_solution_id IS NOT NULL)::int = 1
    )
    );

-- 소유자별로 도면 번호가 고유해야 한다.
-- 소유자 컬럼이 NULL인 행은 각 부분 인덱스에서 제외된다.
CREATE UNIQUE INDEX uk_tb_csp_con04_q_no ON tb_csp_con04 (question_id, drawing_no)
    WHERE question_id IS NOT NULL;
CREATE UNIQUE INDEX uk_tb_csp_con04_s_no ON tb_csp_con04 (solution_id, drawing_no)
    WHERE solution_id IS NOT NULL;
CREATE UNIQUE INDEX uk_tb_csp_con04_e_no ON tb_csp_con04 (extra_solution_id, drawing_no)
    WHERE extra_solution_id IS NOT NULL;

COMMENT ON TABLE  tb_csp_con04                   IS '도면 — 문항·해설·추가풀이에 첨부되는 이미지';
COMMENT ON COLUMN tb_csp_con04.drawing_id        IS '도면 ID';
COMMENT ON COLUMN tb_csp_con04.question_id       IS '소유 문항. solution_id·extra_solution_id와 함께 정확히 하나만 채운다';
COMMENT ON COLUMN tb_csp_con04.solution_id       IS '소유 해설. 셋 중 하나만';
COMMENT ON COLUMN tb_csp_con04.extra_solution_id IS '소유 추가풀이. 셋 중 하나만';
COMMENT ON COLUMN tb_csp_con04.drawing_no        IS '소유자 내 도면 번호. 본문의 [[drawing:n]] 토큰이 참조하는 식별자. 재채번하지 않으며 빈 번호를 허용한다 (DR-F01)';
COMMENT ON COLUMN tb_csp_con04.file_uuid         IS '저장 파일명. 경로 추측 차단 (R-53, DR-F02)';
COMMENT ON COLUMN tb_csp_con04.file_name         IS '업로드 시점 원본 파일명';
COMMENT ON COLUMN tb_csp_con04.file_hash         IS '등록 시점 원본 해시(SHA-256). 재인코딩 금지 검증용 (R-16)';
COMMENT ON COLUMN tb_csp_con04.file_type         IS '파일 형식(MIME). image/png 등. 서빙 시 Content-Type 값';
COMMENT ON COLUMN tb_csp_con04.created_at        IS '등록일시';

-- ============================ 2-3. 운영·공통 (adm, cmn) ============================

CREATE TABLE tb_csp_adm01 (
                                            admin_id      BIGINT GENERATED ALWAYS AS IDENTITY,
                                            login_id      VARCHAR(50) NOT NULL,
    password_hash VARCHAR(72) NOT NULL,
    last_login_at TIMESTAMPTZ,
    CONSTRAINT pk_tb_csp_adm01 PRIMARY KEY (admin_id),
    CONSTRAINT uk_tb_csp_adm01_login UNIQUE (login_id)
    );

COMMENT ON TABLE  tb_csp_adm01               IS '관리자 계정 — 관리자 로그인 계정 (R-50)';
COMMENT ON COLUMN tb_csp_adm01.admin_id      IS '관리자 ID';
COMMENT ON COLUMN tb_csp_adm01.login_id      IS '관리자 로그인 ID. 추가풀이 조회 키와 별개';
COMMENT ON COLUMN tb_csp_adm01.password_hash IS '비밀번호 해시 (bcrypt). 평문 저장 금지 (DR-S02)';
COMMENT ON COLUMN tb_csp_adm01.last_login_at IS '최종 로그인 일시';
-- 세션은 서버 메모리 관리(R-55, 단일 WAS 전제)로 세션 테이블을 두지 않는다.
-- 초기 계정은 문서에 기재하지 않고 별도 전달한다 (R-50).

CREATE TABLE tb_csp_cmn01 (
                                            group_code  VARCHAR(20) NOT NULL,
    code_value  VARCHAR(20) NOT NULL,
    code_name   VARCHAR(100) NOT NULL,
    code_level  INT NOT NULL DEFAULT 1,
    parent_code VARCHAR(20),
    sort_order  INT NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    is_system   BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_tb_csp_cmn01 PRIMARY KEY (group_code, code_value),
    CONSTRAINT fk_tb_csp_cmn01_parent FOREIGN KEY (group_code, parent_code)
    REFERENCES tb_csp_cmn01 (group_code, code_value),
    CONSTRAINT ck_tb_csp_cmn01_level CHECK (code_level IN (1, 2)),
    CONSTRAINT ck_tb_csp_cmn01_tree CHECK (
(code_level = 1 AND parent_code IS NULL) OR
(code_level = 2 AND parent_code IS NOT NULL)
    )
    );

CREATE INDEX idx_tb_csp_cmn01_parent ON tb_csp_cmn01 (group_code, parent_code);

COMMENT ON TABLE  tb_csp_cmn01             IS '공통코드 — 상태값·유형값 코드 마스터. 최대 2단계 계층 (안건10)';
COMMENT ON COLUMN tb_csp_cmn01.group_code  IS '코드 그룹: SUBJECT, Q_STATUS, RPT_TYPE, RPT_STATUS, ES_STATUS';
COMMENT ON COLUMN tb_csp_cmn01.code_value  IS '코드값. 같은 group_code 안에서 고유. 앞자리 0 보존 위해 문자열 (DR-C02)';
COMMENT ON COLUMN tb_csp_cmn01.code_name   IS '코드명. 화면 표시용';
COMMENT ON COLUMN tb_csp_cmn01.code_level  IS '계층 단계. 1 대분류, 2 중분류. 필수. 3단계 이상 불가 (DR-C04)';
COMMENT ON COLUMN tb_csp_cmn01.parent_code IS '상위 코드값. 1단계는 NULL, 2단계는 필수. 같은 group_code 내에서만 참조';
COMMENT ON COLUMN tb_csp_cmn01.sort_order  IS '화면 표시 순서';
COMMENT ON COLUMN tb_csp_cmn01.is_active   IS '사용 여부. 미사용 과목은 목록에서 제외 (R-23)';
COMMENT ON COLUMN tb_csp_cmn01.is_system   IS '시스템 코드 여부. TRUE면 관리자가 삭제·비활성화·코드값 변경 불가 (DR-C05)';

CREATE TABLE tb_csp_cmn02 (
                                            log_id     BIGINT GENERATED ALWAYS AS IDENTITY,
                                            created_ip INET NOT NULL,
                                            is_success BOOLEAN NOT NULL,
                                            created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_tb_csp_cmn02 PRIMARY KEY (log_id)
    );

CREATE INDEX idx_tb_csp_cmn02_ip ON tb_csp_cmn02 (created_ip, created_at);

COMMENT ON TABLE  tb_csp_cmn02            IS '조회 시도 로그 — 추가풀이 현황 조회 시도 기록 (R-48)';
COMMENT ON COLUMN tb_csp_cmn02.log_id     IS '로그 ID';
COMMENT ON COLUMN tb_csp_cmn02.created_ip IS '요청 IP (R-48)';
COMMENT ON COLUMN tb_csp_cmn02.is_success IS '조회 성공 여부';
COMMENT ON COLUMN tb_csp_cmn02.created_at IS '조회 시도 일시';
