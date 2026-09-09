# 토목기술사 문제풀이 서비스 (CSP)

기술사 기출문제를 회차·과목·랜덤으로 풀어보는 웹 서비스. 회원 가입 없이 전체 열람할 수 있고,
콘텐츠 작성은 관리자만 한다.

WAS와 Web을 따로 띄운다. 운영에서는 Nginx가 둘을 같은 오리진으로 묶는다.

```
masters_community/
├─ backend/          WAS — Java 21 · Spring Boot 3.4 · JPA · Flyway
├─ frontend/         Web — React 18 · Vite · TypeScript
├─ deploy/           nginx.conf · docker-compose.yml (로컬 DB)
├─ docs/             개발규칙 · DB설계서 · 화면정의서 · ERD
└─ CLAUDE.md         코드 작성 전 반드시 읽는 규칙 요약
```

---

## 실행

### 1. DB

로컬에 PostgreSQL을 설치했다면 superuser로 롤과 DB만 만든다. 한 번이면 된다.

```bash
psql -d postgres -f deploy/local-db-bootstrap.sql
```

Docker를 쓴다면 이 단계 대신 아래 한 줄이다. 다만 컨테이너는 기본 프로파일 접속 정보
(`csp` / `csp` / `csp`)로 뜨므로, `local` 프로파일로 붙이려면 접속 정보를 맞춰야 한다.

```bash
docker compose -f deploy/docker-compose.yml up -d
```

테이블과 공통코드는 만들지 않아도 된다. WAS가 기동하면서 Flyway가 넣는다.
`V1` 스키마 · `V2` 시스템 코드 · `V3` 과목 코드 121개.

### 2. WAS

```bash
cd backend
CSP_ADMIN_ID=admin CSP_ADMIN_PASSWORD='<별도 전달>' ./gradlew bootRun --args='--spring.profiles.active=local'
```

`local` 프로파일은 접속 정보와 로그 레벨만 덮어쓴다. 스키마는 기본 프로파일과 같은 경로로,
같은 Flyway 마이그레이션으로 만들어진다.

화면에 볼 데이터가 필요하면 샘플을 한 번 넣는다. 회차 3 · 문항 6 · 해설 3이 들어간다.

```bash
psql -d dcsp -U dwas -f deploy/seed-local.sql
```

운영에 들어가면 안 되는 데이터라 마이그레이션으로 만들지 않았다. 여러 번 실행해도 안전하다.

초기 관리자 계정은 문서·저장소에 남기지 않고 환경변수로 전달한다 (R-50). 계정이 이미 있으면
환경변수는 무시된다. 환경변수 없이 띄우면 경고만 남고 계정이 만들어지지 않는다.

### 3. Web

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
```

개발 서버가 `/api`와 `/admin/api`를 8080으로 프록시한다. 오리진이 같아야 관리자 세션 쿠키가
붙기 때문에 CORS 설정 대신 프록시를 쓴다.

### 4. 웹폰트

`frontend/public/fonts/README.md`의 안내에 따라 두 파일을 배치한다. 없어도 화면은 뜬다
(`font-display: swap`).

---

## 명령

| 목적 | 명령 |
| --- | --- |
| 백엔드 빌드 | `cd backend && ./gradlew build` |
| 백엔드 테스트 | `cd backend && ./gradlew test` |
| 단일 테스트 | `./gradlew test --tests 'kr.co.csp.content.service.DrawingTokenTest'` |
| 프론트 타입 검사 | `cd frontend && npm run typecheck` |
| 프론트 빌드 | `cd frontend && npm run build` |

---

## 화면과 API

화면 ID는 `docs/화면정의서_v0.4.pptx`를 따른다.

| 화면 | 경로 | 주요 API |
| --- | --- | --- |
| SCR-001 메인 | `/` | `GET /api/exams` |
| SCR-002 회차·교시 | `/exams` | `GET /api/exams` |
| SCR-003 과목 | `/categories` | `GET /api/categories` |
| SCR-004 문제 목록/상세 | `/questions` | `GET /api/exams/{id}/questions` · `/api/categories/{code}/questions` · `/api/questions/random` · `GET /api/questions/{id}/solution` |
| SCR-005 신고 (모달) | SCR-004 위 | `POST /api/reports` |
| SCR-006 추가풀이 등록 | `/extra-solutions/new` | `POST /api/extra-solutions` |
| SCR-007 추가풀이 현황 | `/extra-solutions/status` | `POST /api/extra-solutions/status` |
| SCR-008 서비스 정보 | `/about` | — (정적) |
| SCR-A05 관리자 로그인 | `/admin/login` | `POST /admin/api/login` |
| SCR-A01 대시보드 | `/admin` | `GET /admin/api/dashboard` |
| SCR-A02 신고 처리 | `/admin/reports` | `GET`·`PATCH /admin/api/reports` |
| SCR-A03 추가풀이 검토 | `/admin/extra-solutions` | `GET`·`PATCH /admin/api/extra-solutions` |
| SCR-A04 문제·풀이 작성 | `/admin/questions` | `GET`·`POST`·`PUT /admin/api/questions` |

`/admin/**` 전체가 인증 대상이다. 숨김 URL은 부가 조치일 뿐이며 실제 방어선은 인증이다 (R-49).

---

## 설계에서 눈여겨볼 지점

**해설은 목록 응답에 없다.** 문항(`tb_csp_con02`)과 해설(`tb_csp_con03`)이 다른 테이블이라,
목록 조회가 문항 테이블만 읽는 한 해설이 섞일 수 없다. 코드 리뷰가 아니라 스키마가 이를
보장한다. 목록 DTO(`QuestionListItem`)에는 해설 필드 자체가 없다 (`DR-A01` · R-54).

**본문 속 이미지 위치는 `[[drawing:n]]` 토큰이다.** 도면 테이블만으로는 "본문 뒤에 일괄 나열"
외의 배치를 저장할 수 없다. 본문을 HTML로 저장하는 방법도 있지만 추가풀이는 사용자 입력이라
XSS를 직접 막아야 한다. 토큰 방식은 본문을 이스케이프한 뒤 토큰만 치환하면 되므로 그 위험이
없다 — React가 텍스트 조각을 자동으로 이스케이프하므로 프론트에서는 구조적으로 지켜진다
(`DR-F03`).

**도면 번호는 순서가 아니라 식별자다.** 삭제해도 재채번하지 않으므로 1과 4만 남을 수 있다.
표시 순서는 본문에서 토큰이 놓인 위치가 정한다 (`DR-F01`).

**IP는 프록시 헤더에서 얻는다.** Nginx 뒤에 있어 설정 없이 `getRemoteAddr()`를 부르면 항상
프록시 IP가 나온다. 그 값으로 일일 제한을 걸면 모든 사용자가 한 사람으로 집계된다.
`nginx.conf`와 `application.yml`이 짝을 이룬다 (`DR-S01`).

**문항은 물리 삭제하지 않는다.** 해설·도면·신고·추가풀이가 FK로 매달려 있다. `status_code`를
`QST003`으로 바꾼다 (`DR-L01`).

---

## 문서

| 파일 | 내용 |
| --- | --- |
| `CLAUDE.md` | 코드 작성 전 지켜야 할 규칙 요약 |
| `docs/개발규칙_v2.4.md` | 규칙 전문 · 표준 컬럼 사전 · 과목 코드 목록 |
| `docs/DB설계서_ERD_v0.18.md` | 스키마 · DDL · 설계 판단 근거 |
| `docs/화면정의서_v0.4.pptx` | SCR-xxx 화면 |
| `docs/CSP_물리ERD.png` | 물리 ERD |

프로토타입: [kosw01/exam-variation-viewer](https://github.com/kosw01/exam-variation-viewer) —
시각 언어(색·타이포·카드 레이아웃)를 `frontend/src/styles/base.css`로 옮겨 왔다.
