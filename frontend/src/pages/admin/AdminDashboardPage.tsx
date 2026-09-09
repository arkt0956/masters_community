import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client';
import type { Dashboard } from '../../api/types';

/**
 * 대시보드 (SCR-A01).
 *
 * 신고는 접수 + 검토중, 추가풀이는 검토대기 + 검토중 합계다 (설계 3-5).
 * 대기 0건이어도 진입은 허용한다.
 */
export default function AdminDashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);

  useEffect(() => {
    api.admin.dashboard().then(setData).catch(() => setData(null));
  }, []);

  return (
    <div className="admin-main">
      <h1 className="admin-h">대시보드</h1>
      <p className="admin-sub">처리할 일을 확인하고 각 화면으로 이동합니다.</p>

      <div className="dash">
        <Link to="/admin/reports">
          <span className="n">{data?.reportPending ?? '–'}</span>
          <span className="l">신고 처리</span>
          <span className="d">접수 · 검토중 건수</span>
        </Link>
        <Link to="/admin/extra-solutions">
          <span className="n">{data?.extraSolutionPending ?? '–'}</span>
          <span className="l">추가풀이 검토</span>
          <span className="d">검토대기 · 검토중 건수</span>
        </Link>
        <Link to="/admin/questions">
          <span className="n">{data?.questionDraft ?? '–'}</span>
          <span className="l">문제·풀이</span>
          <span className="d">작성중 문항 수</span>
        </Link>
      </div>
    </div>
  );
}
