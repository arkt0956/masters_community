import { useEffect, useState } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { ApiError, api } from '../../api/client';
import type { AdminMe } from '../../api/types';

/**
 * 관리자 레이아웃 겸 인증 가드 (R-20 · R-49 · R-55).
 *
 * 화면 가드는 편의일 뿐이다. 실제 방어선은 서버의 AdminAuthInterceptor다.
 * 여기서 막지 못해도 API가 401을 돌려준다.
 *
 * 관리자 화면에는 공통 푸터를 두지 않는다. DR-X01이 요구하는 것은 사용자 화면이다.
 */
export default function AdminLayout() {
  const [me, setMe] = useState<AdminMe | null>(null);
  const [checking, setChecking] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    api.admin
      .me()
      .then(setMe)
      .catch((e) => {
        // 만료됐으면 즉시 차단하고 로그인으로 보낸다 (R-55).
        if (e instanceof ApiError && e.isUnauthorized) {
          navigate('/admin/login', { replace: true, state: { from: location.pathname } });
        }
      })
      .finally(() => setChecking(false));
  }, [navigate, location.pathname]);

  async function logout() {
    await api.admin.logout();
    navigate('/admin/login', { replace: true });
  }

  if (checking) {
    return <p className="loading">확인 중…</p>;
  }
  if (!me) {
    return null;
  }

  return (
    <>
      <div className="adminbar">
        <div className="adminbar-in">
          <Link className="aback" to="/admin">
            ‹ 대시보드
          </Link>
          <b>관리자</b>
          <span className="who">{me.loginId}</span>
          <button className="exit" type="button" onClick={logout}>
            로그아웃
          </button>
        </div>
      </div>
      <Outlet />
    </>
  );
}
