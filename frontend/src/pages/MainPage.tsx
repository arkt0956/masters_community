import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/client';

/**
 * 메인 — 이용 방식 선택 (SCR-001).
 *
 * 진입 시 GET /api/exams로 콘텐츠 보유 여부를 확인한다. 공개 콘텐츠가 0건이면
 * 세 버튼을 비활성화하고 사유를 함께 표시한다 (SCR-001 유효성·예외 표).
 */
export default function MainPage() {
  const [hasContent, setHasContent] = useState<boolean | null>(null);
  const [failed, setFailed] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    api
      .exams()
      .then((exams) => setHasContent(exams.length > 0))
      .catch(() => setFailed(true));
  }, []);

  const disabled = hasContent === false || failed;

  return (
    <div className="land">
      <header className="hero">
        <p className="kicker">토목기술사</p>
        <h1>
          기출문제를 풀고,
          <br />
          모범답안과 도면을 확인합니다.
        </h1>
        <p className="lead">
          기출문제와 해설을 회차·교시·과목별로 열람하는 서비스입니다. 로그인 없이 바로 볼 수
          있습니다.
        </p>
      </header>

      <h2 className="sec-h">어떻게 풀어볼까요?</h2>
      <ul className="menu">
        <li>
          <button type="button" disabled={disabled} onClick={() => navigate('/exams')}>
            <span className="ico" aria-hidden="true">
              회
            </span>
            <span className="txt">
              <b>회차별</b>
              <span>회차와 교시를 골라 그 시험 그대로 풀어봅니다</span>
            </span>
            <span className="arw" aria-hidden="true">
              ›
            </span>
          </button>
        </li>
        <li>
          <button type="button" disabled={disabled} onClick={() => navigate('/categories')}>
            <span className="ico" aria-hidden="true">
              과
            </span>
            <span className="txt">
              <b>과목별</b>
              <span>구조역학·교량공학 등 과목을 골라 모아 봅니다</span>
            </span>
            <span className="arw" aria-hidden="true">
              ›
            </span>
          </button>
        </li>
        <li>
          <button
            type="button"
            disabled={disabled}
            onClick={() => navigate('/questions?mode=random')}
          >
            <span className="ico" aria-hidden="true">
              랜
            </span>
            <span className="txt">
              <b>랜덤 연습</b>
              <span>무작위 순서로 바로 시작합니다</span>
            </span>
            <span className="arw" aria-hidden="true">
              ›
            </span>
          </button>
        </li>
        <li>
          <Link to="/extra-solutions/status">
            <span className="ico" aria-hidden="true">
              조
            </span>
            <span className="txt">
              <b>추가풀이 현황 조회</b>
              <span>등록한 풀이의 처리 상태를 확인합니다</span>
            </span>
            <span className="arw" aria-hidden="true">
              ›
            </span>
          </Link>
        </li>
      </ul>

      {failed && <p className="err">콘텐츠를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.</p>}
      {hasContent === false && <p className="hint">아직 공개된 문항이 없습니다.</p>}

      <h2 className="sec-h">이 서비스는</h2>
      <ul className="points">
        <li>
          <h3>기출 원문을 그대로 싣습니다</h3>
          <p>
            한국산업인력공단이 공공누리 제1유형으로 개방한 제140회 이후 기출문제입니다. 출처표시는
            이용 조건이므로 서비스 정보에 함께 밝힙니다.
          </p>
        </li>
        <li>
          <h3>문제와 해설이 한 곳에 있습니다</h3>
          <p>
            문항마다 모범답안 본문과 도면이 함께 붙어 있습니다. 문제를 먼저 풀고, 원할 때 문항별로
            해설을 엽니다.
          </p>
        </li>
        <li>
          <h3>해설은 자체 작성물입니다</h3>
          <p>
            모범답안과 답안 도면은 본 서비스가 작성했습니다. 원문과 자체 작성물의 경계를 화면에서
            구분해 표시합니다.
          </p>
        </li>
      </ul>

      {/* 관리자 진입은 숨김 URL이므로 이 화면에 노출하지 않는다 (NFR-04 · R-49) */}
    </div>
  );
}
