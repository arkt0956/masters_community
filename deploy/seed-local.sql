-- 로컬 개발용 샘플 콘텐츠. 화면에 볼 것이 있도록 최소한만 넣는다.
-- 애플리케이션이 자동으로 실행하지 않는다. 필요할 때 손으로 넣는다:
--
--   psql -d dcsp -U dwas -f deploy/seed-local.sql
--
-- Flyway 마이그레이션으로 만들지 않은 이유는 이 데이터가 운영에 들어가면 안 되기 때문이다.
--
-- 여러 번 실행해도 안전하도록 모든 INSERT를 멱등으로 두었다. 자연키(회차 번호 · 문항 번호)로
-- ON CONFLICT를 걸었다. PK는 GENERATED ALWAYS AS IDENTITY라 값을 직접 넣지 않고
-- 자연키로 조회해서 참조한다 — 재실행마다 ID가 달라져도 관계가 깨지지 않는다.
--
-- 공통코드는 여기서 만들지 않는다. category_code는 V3가 넣은 SUBJECT 코드를 참조할 뿐이다 (DR-C03).
-- 관리자 계정도 여기서 만들지 않는다. AdminAccountInitializer가 환경변수로 처리한다 (R-50).
-- 운영 DB에서는 절대 실행하지 않는다.

-- ---------------------------------------------------------------- 회차
INSERT INTO tb_csp_con01 (exam_round, is_public, updated_ip) VALUES
    (130, TRUE,  '127.0.0.1'),
    (129, TRUE,  '127.0.0.1'),
    (128, FALSE, '127.0.0.1')   -- 비공개 회차. 목록 필터(R-01) 확인용
ON CONFLICT (exam_round) DO NOTHING;

-- ---------------------------------------------------------------- 문항
-- status_code QST002(게시됨)만 목록에 나온다. QST001·QST003도 하나씩 넣어
-- 관리자 화면과 사용자 목록의 차이를 로컬에서 바로 볼 수 있게 한다.
INSERT INTO tb_csp_con02 (exam_id, session_no, question_no, category_code, title, contents, status_code, updated_ip)
SELECT e.exam_id, v.session_no, v.question_no, v.category_code, v.title, v.contents, v.status_code, '127.0.0.1'
FROM (VALUES
    (130, 1, 1, 'RCC004', '철근콘크리트 보의 휨 파괴 형태',
     '철근콘크리트 보의 휨 파괴 형태를 균형철근비와 관련지어 설명하시오.', 'QST002'),
    (130, 1, 2, 'RCC005', '전단철근의 최소 배치 기준',
     '철근콘크리트 부재에서 전단철근을 최소로 배치해야 하는 경우와 그 기준을 설명하시오.', 'QST002'),
    (130, 2, 1, 'STM001', '부정정 구조물의 해석법',
     '부정정 구조물의 해석 방법을 분류하고 각각의 적용 조건을 설명하시오.', 'QST002'),
    (129, 1, 1, 'GEN004', '설계 하중의 조합',
     '교량 설계 시 고려하는 하중의 종류와 하중조합 원칙을 설명하시오.', 'QST002'),
    (129, 1, 2, 'BRG001', '교량 받침의 종류',
     '교량 받침의 종류별 특징과 선정 시 고려사항을 설명하시오.', 'QST001'),  -- 작성중
    (129, 2, 1, 'RCC001', '콘크리트 배합강도',
     '콘크리트 배합강도 결정 절차를 설명하시오.', 'QST003')                  -- 삭제
) AS v(exam_round, session_no, question_no, category_code, title, contents, status_code)
JOIN tb_csp_con01 e ON e.exam_round = v.exam_round
ON CONFLICT (exam_id, session_no, question_no) DO NOTHING;

-- ---------------------------------------------------------------- 해설
-- 일부 문항에만 넣는다. 해설 없는 문항의 상세 화면(R-16)도 확인해야 하기 때문이다.
-- [[drawing:n]] 토큰은 쓰지 않는다. 도면 파일이 없으면 고아 토큰이 되어 게시 검증에 걸린다 (DR-F03).
INSERT INTO tb_csp_con03 (question_id, contents, updated_ip)
SELECT q.question_id, v.contents, '127.0.0.1'
FROM (VALUES
    (130, 1, 1, '균형철근비를 기준으로 과소철근보·균형보·과다철근보로 나뉜다. 과소철근보는 인장철근이 먼저 항복하여 연성 파괴를 보이므로 설계에서 이를 유도한다.'),
    (130, 1, 2, '계수전단력이 콘크리트 전단강도의 1/2을 초과하면 최소 전단철근을 배치한다. 슬래브·확대기초 등은 예외로 둔다.'),
    (129, 1, 1, '고정하중·활하중·충격·풍하중·지진하중으로 분류하고, 한계상태설계법에서는 하중계수를 곱한 조합 중 최대값으로 설계한다.')
) AS v(exam_round, session_no, question_no, contents)
JOIN tb_csp_con01 e ON e.exam_round = v.exam_round
JOIN tb_csp_con02 q ON q.exam_id = e.exam_id
                   AND q.session_no = v.session_no
                   AND q.question_no = v.question_no
ON CONFLICT (question_id) DO NOTHING;
