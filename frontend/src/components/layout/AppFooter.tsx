import { Link } from 'react-router-dom';

/**
 * 공통 푸터 (DR-X01 · R-56).
 *
 * 기출 원문을 게재하므로 공공누리 제1유형이 적용된다. 출처표시는 임의 표기가 아니라
 * 이용 조건이다. 링크가 노출되지 않는 화면이 하나라도 생기면, 그 화면은 이용 조건을
 * 벗어난 상태로 원문을 게재하는 셈이 된다.
 *
 * 접기·조건부 렌더링을 두지 않는다.
 */
export default function AppFooter() {
  return (
    <div className="sitefoot">
      <Link to="/about">서비스 정보 · 공공누리 제1유형 출처표시</Link>
    </div>
  );
}
