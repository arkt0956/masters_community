-- 시스템 공통코드 (DB설계서 2-4 · 개발규칙 DR-C05).
-- 애플리케이션 로직이 분기에 사용하므로 is_system = TRUE로 두어 관리자 삭제를 막는다.
-- DR-C03 — 이 목록에 없는 그룹코드·코드값을 승인 없이 추가하지 않는다.
INSERT INTO tb_csp_cmn01 (group_code, code_value, code_name, code_level, parent_code, sort_order, is_active, is_system) VALUES
    ('Q_STATUS',   'QST001', '작성중',   1, NULL, 1, TRUE, TRUE),
    ('Q_STATUS',   'QST002', '게시됨',   1, NULL, 2, TRUE, TRUE),
    ('Q_STATUS',   'QST003', '삭제',     1, NULL, 3, TRUE, TRUE),
    ('RPT_TYPE',   'RPT001', '오탈자',   1, NULL, 1, TRUE, TRUE),
    ('RPT_TYPE',   'RPT002', '오답',     1, NULL, 2, TRUE, TRUE),
    ('RPT_TYPE',   'RPT003', '오분류',   1, NULL, 3, TRUE, TRUE),
    ('RPT_STATUS', 'RPS001', '접수',     1, NULL, 1, TRUE, TRUE),
    ('RPT_STATUS', 'RPS002', '검토중',   1, NULL, 2, TRUE, TRUE),
    ('RPT_STATUS', 'RPS003', '반영',     1, NULL, 3, TRUE, TRUE),
    ('RPT_STATUS', 'RPS004', '기각',     1, NULL, 4, TRUE, TRUE),
    ('ES_STATUS',  'EXS001', '검토대기', 1, NULL, 1, TRUE, TRUE),
    ('ES_STATUS',  'EXS002', '검토중',   1, NULL, 2, TRUE, TRUE),
    ('ES_STATUS',  'EXS003', '게시',     1, NULL, 3, TRUE, TRUE),
    ('ES_STATUS',  'EXS004', '반려',     1, NULL, 4, TRUE, TRUE);
