# CLAUDE.md

토목기술사 문제풀이 서비스 (CSP)

## 이 파일을 읽는 법

이 파일은 코드를 쓰기 전에 항상 지켜야 하는 것만 추린 요약이다. 전문은 `docs/개발규칙_v2.4.md`에 있다 — `docs/`는 저장소에 없으므로 작업자가 직접 채워야 한다 (「문서」 참고). 아래 「절대 하지 말 것」을 먼저 읽고, 판단이 서지 않으면 코드를 쓰지 말고 전문을 읽거나 물어본다.

---

## 프로젝트

기술사 기출문제를 회차·과목·랜덤으로 풀어보는 웹 서비스. 회원 가입 없이 전체 열람 가능하고, 콘텐츠 작성은 관리자만 한다.

| 영역 | 스택 |
| --- | --- |
| 백엔드 | Java 21 · Spring Boot 3.x · Spring Data JPA · Flyway |
| DB | PostgreSQL 18 |
| 프론트 | React |
| 인프라 | Nginx (단일 서버) |

---

## 절대 하지 말 것

### 테이블·컬럼을 추가하지 마라 — `DR-D01`

엔티티에 필드를 늘리거나 마이그레이션을 작성하지 않는다. `spring.jpa.hibernate.ddl-auto`는 `validate`에서 바꾸지 않는다.

스키마는 한 번 들어가면 되돌리기 비싸다. `ddl-auto=update`는 애플리케이션이 기동하면서 스키마를 조용히 바꾼다. 리뷰도 기록도 거치지 않는다.

필요하다고 판단되면 무엇이 왜 필요한지 제안하고 멈춘다. 요구사항 ID(R-xx)를 댈 수 없는 컬럼은 만들지 않는다.

### 공통코드를 추가하지 마라 — `DR-C03`

`tb_csp_cmn01`에 새 그룹코드나 코드값을 넣지 않는다. 마이그레이션·시드·테스트 픽스처 어디에도. `getOrCreate` 류 폴백도 만들지 않는다.

승인 없이 코드가 늘면 그룹 범용성과 접두어 고유성이 무너진다. 폴백은 오타를 새 코드로 등록해 조용히 체계를 오염시킨다.

코드가 없으면 예외를 던진다. 필요한 코드가 있으면 어떤 코드가 왜 필요한지 제안하고 멈춘다.

### 해설을 목록 응답에 넣지 마라 — `DR-A01`

목록 조회는 `tb_csp_con02`만 읽고 `tb_csp_con03`(해설)을 조인하지 않는다. 컨트롤러는 엔티티가 아니라 DTO를 반환한다.

해설은 풀이 후에 열어보는 것이다. 클라이언트에서 숨겨도 개발자 도구로 볼 수 있다. 서버가 아예 보내지 않아야 한다.

목록 DTO(`QuestionListItem`)에는 해설 필드가 없다. 해설은 상세 DTO(`QuestionDetail`)에만 있다.

### 문항을 물리 삭제하지 마라 — `DR-L01`

`DELETE`를 쓰지 않는다.

문항에 해설·도면·신고·추가풀이가 FK로 매달려 있다. 물리 삭제하면 사용자가 등록한 데이터가 함께 사라지거나 FK 제약에 걸린다.

`status_code`를 `QST003`(삭제)로 바꾼다.

### 수정할 때 `updated_at`·`updated_ip`를 빠뜨리지 마라 — `DR-L02`

`tb_csp_con01`·`con02`·`con03`을 수정하면 두 컬럼이 함께 갱신되어야 한다. 관리자 계정이 공유될 수 있어 잘못된 수정을 추적할 단서가 시각과 IP뿐이다.

엔티티가 `Auditable`을 상속하면 자동으로 채워진다. 단, `@Modifying` JPQL 벌크 수정은 리스너를 우회하므로 쿼리에 `updatedAt`·`updatedIp`를 직접 넣고, `@Modifying(clearAutomatically = true)`로 낡은 엔티티가 남지 않게 한다.

### 도면 번호를 재채번하지 마라 — `DR-F01`

`drawing_no`는 삭제해도 당기지 않는다. 빈 번호를 그대로 둔다.

본문 토큰 `[[drawing:n]]`이 이 번호를 참조한다. 재채번하면 본문 텍스트 안의 토큰까지 함께 고쳐야 하고, 한 군데라도 어긋나면 이미지가 엉뚱한 자리에 나타난다.

새 도면은 `MAX(drawing_no) + 1`. 삭제 시 본문의 해당 토큰만 함께 지운다.

### 비밀번호를 평문으로 저장하지 마라 — `DR-S02`

`password_hash`에 평문이나 양방향 암호화 결과를 넣지 않는다. 복호화 메서드를 만들지 않는다.

사람들은 비밀번호를 재사용한다. DB가 유출되면 사용자의 다른 계정까지 위험해진다.

`BCryptPasswordEncoder`로 `encode`, `matches`만 쓴다.

### 요청 IP를 `getRemoteAddr()`로 그냥 쓰지 마라 — `DR-S01`

프록시 헤더 설정 없이 `getRemoteAddr()`를 호출하지 않는다. `X-Forwarded-For`를 직접 파싱하지 않는다.

Nginx 뒤라서 항상 프록시 IP가 나온다. 이 값으로 일일 제한을 걸면 모든 사용자가 한 사람으로 집계된다.

`server.forward-headers-strategy=native`와 `server.tomcat.remoteip.internal-proxies` 설정에 의존한다. 설정 후에는 `getRemoteAddr()`가 실제 IP를 돌려준다.

---

## 테이블 매핑표

테이블명이 `tb_csp_con02` 형태라 이름만으로는 내용을 알 수 없다. 쿼리를 쓰기 전에 이 표를 본다.

| 물리 테이블명 | 논리명 | 한글명 |
| --- | --- | --- |
| `tb_csp_con01` | exam | 회차 |
| `tb_csp_con02` | question | 문항 |
| `tb_csp_con03` | solution | 해설 |
| `tb_csp_con04` | drawing | 도면 |
| `tb_csp_usr01` | report | 신고 |
| `tb_csp_usr02` | extra_solution | 추가풀이 |
| `tb_csp_adm01` | admin_account | 관리자 계정 |
| `tb_csp_cmn01` | common_code | 공통코드 |
| `tb_csp_cmn02` | lookup_attempt_log | 조회 시도 로그 |

코드에서는 논리명을 쓴다. 물리명은 `@Table(name=...)`과 네이티브 쿼리에만 나타난다 (`DR-N06`).

---

## 표준 컬럼 사전

같은 의미면 어느 테이블에서든 같은 이름을 쓴다 (`DR-N03`). `body` / `detail` / `content`가 섞이면 조인과 DTO 매핑에서 매번 확인이 필요하다. 새 컬럼은 이 표를 먼저 확인하고, 없으면 표에 추가한다.

| 의미 | 컬럼명 | 타입 |
| --- | --- | --- |
| 본문·내용 | `contents` | TEXT |
| 제목 | `title` | TEXT |
| 유형 구분 | `type_code` | VARCHAR(20) |
| 처리·게시 상태 | `status_code` | VARCHAR(20) |
| 분류 코드 | `category_code` | VARCHAR(20) |
| 도면 식별 번호 | `drawing_no` | INT |
| 정렬 순서 | `sort_order` | INT |
| 요청자 IP | `created_ip` | INET |
| 수정자 IP | `updated_ip` | INET |
| 등록·수정·처리 일시 | `created_at` · `updated_at` · `processed_at` | TIMESTAMPTZ |
| 사용자 표시명 | `user_name` | VARCHAR(12) |
| 비밀번호 해시 | `password_hash` | VARCHAR(72) |
| 파일 | `file_uuid` · `file_name` · `file_hash` · `file_type` | — |
| 공통코드 계층 | `code_level` · `parent_code` · `is_system` | — |

접두어·접미어: PK/FK만 `_id`, 불리언은 `is_`, 일시는 `_at`, 코드는 `_code`. 구현 기술명을 컬럼에 넣지 않는다 (`sha256` ✗ → `file_hash` ✓).

---

## 시스템 코드

애플리케이션 로직이 분기에 쓰는 코드값. `is_system = TRUE`라 관리자도 지울 수 없다. 관리자가 `QST002`(게시됨)를 지우면 문항 목록이 빈 채로 나오는데 오류도 안 나기 때문이다.

개별 값만 상수로 둔다. 드롭다운용 목록은 항상 DB에서 읽는다 (`DR-C05`).

| 그룹 | 코드값 |
| --- | --- |
| `Q_STATUS` | `QST001` 작성중 · `QST002` 게시됨 · `QST003` 삭제 |
| `RPT_TYPE` | `RPT001` 오탈자 · `RPT002` 오답 · `RPT003` 오분류 |
| `RPT_STATUS` | `RPS001` 접수 · `RPS002` 검토중 · `RPS003` 반영 · `RPS004` 기각 |
| `ES_STATUS` | `EXS001` 검토대기 · `EXS002` 검토중 · `EXS003` 게시 · `EXS004` 반려 |
| `SUBJECT` | 큐넷 출제기준 체계. 대분류 13 · 중분류 108 (개발규칙 부록 B) |

코드값 접두어는 전역 고유다. 그룹명과 다를 수 있다 (`RPT_STATUS` → `RPS`).

---

## 본문과 이미지

본문 중간 이미지는 `[[drawing:n]]` 토큰으로 위치를 표시한다 (`DR-F03`). 적용 대상은 `tb_csp_con02` · `con03` · `usr02`의 `contents`다. 도면 테이블만으로는 "본문 뒤에 일괄 나열" 외의 배치를 저장할 수 없기 때문이다.

- 토큰 하나 = 이미지 하나. `n`은 `drawing_no`
- 번호는 연속이 아닐 수 있다. 번호를 순서로 해석하지 않는다. 순서는 토큰이 놓인 위치다
- 게시 시점에 검증한다: 고아 토큰 · 미참조 도면 · 토큰 중복. 셋 다 게시 거부
- 렌더링은 이스케이프 먼저, 토큰 치환 나중. 추가풀이는 사용자 입력이라 순서를 바꾸면 XSS가 된다

도면 소유자는 `question_id` · `solution_id` · `extra_solution_id` 중 정확히 하나만 채운다 (CHECK 제약).

---

## 코드 작성

| 규칙 | 내용 | 이유 |
| --- | --- | --- |
| 계층 (`DR-P01`) | Controller → Service → Repository. Service에 `HttpServletRequest`를 넘기지 않는다 | 계층이 섞이면 트랜잭션 경계가 흐려진다 |
| 패키지 (`DR-P02`) | 업무 단위(`content`, `participation`, `admin`, `common`) | 계층 단위면 기능 하나 고칠 때 여러 패키지를 오간다 |
| 트랜잭션 (`DR-P03`) | 조회는 `readOnly = true`. 여러 행을 함께 바꾸면 한 트랜잭션 | 중간에 실패하면 데이터가 반쯤 바뀐 채 확정된다 |
| 검증 (`DR-P04`) | 서버에서 다시 검증한다 | 화면 검증은 보안이 아니다 |
| 예외 (`DR-P05`) | 조회 실패 메시지는 원인을 구분하지 않는다 | "닉네임 없음"과 "비밀번호 틀림"을 나누면 존재 여부가 새어 나간다 |
| 주석 (`DR-T03`) | 무엇은 코드가 말하게, 주석에는 왜를 쓴다. 규칙 ID를 함께 적는다 | 왜는 코드만으로 알 수 없다 |

손글씨체는 해설·추가풀이 **본문에만** `.answer-body` 클래스로 적용한다. 문제 본문·목록·관리자 화면은 기본 서체다. 본문 안에서 한글은 손글씨체 굵게, 기호·수식은 기본 서체 70% 크기다 — `@font-face`의 `unicode-range` + `size-adjust`로 나눈다 (`DR-P08`). 전역 `body`에 걸지 않는다.

프론트엔드: 공통코드 드롭다운을 하드코딩하지 않는다. 상태 라벨도 API의 `codeName`을 쓴다. 모든 사용자 라우트는 공통 레이아웃 아래에 둔다. 푸터의 출처표시 링크는 공공누리 이용 조건이다 (`DR-X01`).

---

## 작업 방식

- 스키마를 바꾸면 ERD 문서·부록 A 매핑표·Flyway 마이그레이션을 같은 PR에 포함한다
- 시드·마이그레이션의 `INSERT`는 스키마 변경만큼 중요하게 다룬다. 공통코드가 조용히 늘어나는 경로다
- 요구사항이 바뀌면 코드보다 문서를 먼저 고친다
- 확신이 서지 않으면 코드를 쓰지 말고 물어본다

---

## 코드 위치

WAS와 Web을 따로 띄운다. 빌드·실행 명령은 `README.md`에 있다.

```
backend/   Java 21 · Spring Boot 3.4 · JPA · Flyway
  src/main/resources/db/migration/   V1 스키마 · V2 시스템 코드 · V3 과목 코드
  src/main/java/kr/co/csp/
    common/        공통코드(CodeRegistry·SystemCode)·비밀번호·IP·예외
    content/       con — 회차·문항·해설·도면
    participation/ usr — 신고·추가풀이
    admin/         adm — 관리자 인증·운영 화면 API
frontend/  React 18 · Vite · TypeScript
  src/pages/       SCR-001~008 · admin/ SCR-A01~A05
  src/components/  ContentsView(토큰 렌더링)·QuestionCard·ReportModal
  src/styles/      base.css(디자인 시스템) · fonts.css(DR-P08)
deploy/    nginx.conf(DR-S01 프록시 헤더) · docker-compose.yml(로컬 DB)
```

규칙이 코드에 어떻게 박혀 있는지 찾을 때:

| 규칙 | 파일 |
| --- | --- |
| `DR-C05` 코드 캐시 | `common/code/CodeRegistry.java` |
| `DR-F01`·`DR-F03` 도면 번호·토큰 | `content/service/DrawingToken.java` · `DrawingService.java` |
| `DR-A01` 목록에 해설 없음 | `content/dto/QuestionListItem.java` |
| `DR-L02` 수정 이력 | `common/entity/Auditable.java` |
| `DR-S01` 요청자 IP | `common/web/ClientIpFilter.java` · `deploy/nginx.conf` |
| `DR-P08` 손글씨체 | `frontend/src/styles/fonts.css` |
| `DR-X01` 공통 푸터 | `frontend/src/components/layout/AppLayout.tsx` |

---

## 문서

`docs/`는 `.gitignore`로 제외되어 저장소에 올라가지 않는다. 저장소를 새로 받으면 이 경로가 비어 있으니, **작업자가 직접 아래 파일들을 `docs/`에 넣어야 한다.**

| 파일 | 내용 |
| --- | --- |
| `docs/개발규칙_v2.4.md` | 규칙 전문 · 표준 컬럼 사전 · 과목 코드 목록 |
| `docs/DB설계서_ERD_v0.18.md` | 스키마 · DDL · 설계 판단 근거 |
| `docs/화면정의서_v0.4.pptx` | SCR-xxx 화면 |
| `docs/CSP_물리ERD.png` | 물리 ERD |

요구사항정의서(R-xx)는 이 저장소에 없다. R-xx 번호는 위 세 문서가 인용하는 것을 따랐다.

## 구조
빌드 의존성 추가·제거는 승인 필요. 특히 DB/보안/인증 관련 라이브러리.