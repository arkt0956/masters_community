import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiError, api } from '../api/client';
import type { ExtraSolutionStatusItem } from '../api/types';
import StatusBadge from '../components/StatusBadge';

/**
 * 추가풀이 현황 조회 (SCR-007).
 *
 * ID와 비밀번호가 모두 맞아야 조회된다. 어느 쪽이 틀렸는지 알리지 않는다 (R-38).
 * 서버가 그 판단을 하므로 화면은 서버 메시지를 그대로 보여준다.
 */
export default function ExtraSolutionStatusPage() {
  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const [items, setItems] = useState<ExtraSolutionStatusItem[] | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function search() {
    setError('');
    if (!userName.trim() || !password) {
      setError('ID와 비밀번호를 모두 입력해 주세요.');
      return;
    }
    setLoading(true);
    try {
      setItems(await api.extraSolutionStatus({ userName, password }));
    } catch (e) {
      setItems(null);
      setError(e instanceof ApiError ? e.message : '조회하지 못했습니다.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="land">
      <Link className="crumb-btn" to="/">
        ‹ 메인
      </Link>
      <header className="sub-head">
        <h1>추가풀이 현황</h1>
        <p>등록에 사용한 ID와 비밀번호로 처리 상태를 확인합니다.</p>
      </header>

      <div className="field">
        <label htmlFor="st-id">ID</label>
        <input id="st-id" value={userName} onChange={(e) => setUserName(e.target.value)} />
      </div>
      <div className="field">
        <label htmlFor="st-pw">비밀번호</label>
        <input
          id="st-pw"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && search()}
        />
      </div>

      {error && <p className="err">{error}</p>}

      <div className="cta-wrap">
        <button className="go" onClick={search} disabled={loading}>
          {loading ? '조회 중…' : '조회'}
        </button>
      </div>

      {items && (
        <>
          <h2 className="sec-h">등록한 풀이 {items.length}건</h2>
          <div className="rows">
            {items.map((item) => (
              <div className="row" key={item.extraSolutionId}>
                <div className="meta">
                  <div className="rt">{item.questionTitle}</div>
                  <div className="rs">
                    {item.sourceLabel} · {new Date(item.createdAt).toLocaleDateString('ko-KR')}
                  </div>
                  {/* 반려 사유는 있으면 표시한다. 필수 입력이 아니다 (R-39) */}
                  {item.rejectReason && <div className="rbody">반려 사유: {item.rejectReason}</div>}
                </div>
                <StatusBadge statusCode={item.statusCode} statusName={item.statusName} />
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
