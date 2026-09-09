import { Link } from 'react-router-dom';

/**
 * 서비스 정보 — 출처표시 (SCR-008 · R-06 · R-56).
 *
 * 공공누리 제1유형은 출처표시를 이용 조건으로 둔다. 이 화면이 그 요건을 충족하는
 * 수단이며 임의 표기가 아니다.
 *
 * 왜 정적 코드인가: 출처표시는 법적 이용 조건이므로 관리자 임의 변경 대상이 아니다
 * (개발규칙 5장 확정 사항). 공통코드나 DB에 두지 않는다.
 */
export default function AboutPage() {
  return (
    <div className="land">
      <Link className="crumb-btn" to="/">
        ‹ 메인
      </Link>
      <header className="sub-head">
        <h1>서비스 정보</h1>
        <p>기출 원문의 출처와 이용 조건을 밝히고, 해설이 자체 작성물임을 구분해 알립니다.</p>
      </header>

      <ul className="about">
        <li>
          <span className="n">1</span>
          <dl>
            <dt>저작물명</dt>
            <dd>기술사 기출문제</dd>
          </dl>
        </li>
        <li>
          <span className="n">2</span>
          <dl>
            <dt>작성자 · 출처</dt>
            <dd>한국산업인력공단 기술자격출제실 · 한국산업인력공단</dd>
          </dl>
        </li>
        <li>
          <span className="n">3</span>
          <dl>
            <dt>이용 조건</dt>
            <dd>
              공공누리 제1유형 (출처표시)
              <small>개방일 2026-08-24. 제140회 이후 기출문제가 개방 대상입니다.</small>
              <span className="kogl">공공누리 제1유형 · 출처표시</span>
            </dd>
          </dl>
        </li>
        <li>
          <span className="n">4</span>
          <dl>
            <dt>자체 작성물</dt>
            <dd>
              해설 · 모범답안 · 답안 도면은 본 서비스가 작성했습니다
              <small>
                원문은 위 출처의 저작물이며, 해설 영역의 내용과 답안 도면은 본 서비스의
                작성물입니다.
              </small>
            </dd>
          </dl>
        </li>
        <li>
          <span className="n">5</span>
          <dl>
            <dt>원문 내려받기</dt>
            <dd>
              <a className="qnet" href="https://www.q-net.or.kr" target="_blank" rel="noopener noreferrer">
                Q-Net 자료실에서 원문 받기 →
              </a>
            </dd>
          </dl>
        </li>
        <li>
          <span className="n">6</span>
          <dl>
            {/* 웹폰트로 제공하면 파일이 이용자 브라우저로 전달되므로 배포에 해당한다 (R-57 · DR-P08) */}
            <dt>사용 서체</dt>
            <dd>
              나눔손글씨 딸에게 엄마가
              <small>
                SIL Open Font License. 지적재산권 네이버 · 네이버문화재단.{' '}
                <a
                  className="qnet"
                  href="https://help.naver.com/service/30016/contents/18088"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  라이선스 전문 →
                </a>
              </small>
            </dd>
          </dl>
        </li>
      </ul>
    </div>
  );
}
