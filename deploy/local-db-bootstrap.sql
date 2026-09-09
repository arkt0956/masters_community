-- 로컬 개발용 DB 부트스트랩. superuser(postgres)로 한 번만 실행한다.
--
--   psql -d postgres -f deploy/local-db-bootstrap.sql
--
-- 접속 정보는 application-local.yml과 맞춘 값이다 (dcsp / dwas / dcspapp).
-- 로컬 전용이며 운영에서는 이 스크립트를 쓰지 않는다.
--
-- 여기서는 롤과 데이터베이스만 만든다. 테이블·인덱스·공통코드는 만들지 않는다.
-- 그 셋은 WAS가 기동할 때 Flyway가 만든다 (V1 · V2 · V3).
-- 스키마 생성 경로를 Flyway 하나로 유지하기 위해서다 (DR-D01).

-- 1) 애플리케이션 롤. 슈퍼유저 권한을 주지 않는다.
--    CREATE ROLE에는 IF NOT EXISTS가 없어 존재 확인을 직접 한다.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'dwas') THEN
        CREATE ROLE dwas WITH LOGIN PASSWORD 'dcspapp';
    END IF;
END
$$;

-- 2) 데이터베이스. 소유자를 dwas로 두어야 부팅 시 CREATE TABLE이 통과한다.
--    CREATE DATABASE는 트랜잭션·DO 블록 안에서 실행할 수 없어 조건 분기를 두지 않는다.
--    이미 있으면 이 문장만 에러가 난다. 그대로 두고 넘어가면 된다.
CREATE DATABASE dcsp
    OWNER      dwas
    ENCODING   'UTF8'
    LC_COLLATE 'C'
    LC_CTYPE   'C'
    TEMPLATE   template0;

-- 3) PostgreSQL 15부터 public 스키마의 CREATE 권한이 기본 회수되어 있다.
--    dwas가 DB 소유자라 지금은 불필요하지만, 다른 롤로 접속하도록 바꿀 때를 위해 남긴다.
\connect dcsp
ALTER SCHEMA public OWNER TO dwas;
