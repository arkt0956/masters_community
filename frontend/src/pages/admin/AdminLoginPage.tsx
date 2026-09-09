import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ApiError, api } from '../../api/client';

/**
 * 관리자 로그인 (SCR-A05 · R-50).
 *
 * 숨김 경로로 접근한다. 다만 숨김 URL은 부가 조치일 뿐이며 실제 방어선은 인증이다 (R-49).
 * 실패 사유를 구분해 알리지 않는다 — 서버가 같은 문구를 돌려준다.
 */
export default function AdminLoginPage() {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);
  const navigate = useNavigate();

  async function submit() {
    setError('');
    setSending(true);
    try {
      await api.admin.login({ loginId, password });
      navigate('/admin', { replace: true });
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '로그인하지 못했습니다.');
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="admin-login">
      <h1>관리자 로그인</h1>
      <p>운영 화면에 접근하려면 로그인해야 합니다.</p>

      <div className="field">
        <label htmlFor="ad-id">아이디</label>
        <input id="ad-id" value={loginId} onChange={(e) => setLoginId(e.target.value)} />
      </div>
      <div className="field">
        <label htmlFor="ad-pw">비밀번호</label>
        <input
          id="ad-pw"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && submit()}
        />
      </div>

      {error && <p className="err">{error}</p>}

      <div className="cta-wrap">
        <button className="go" onClick={submit} disabled={sending}>
          {sending ? '확인 중…' : '로그인'}
        </button>
      </div>
    </div>
  );
}
