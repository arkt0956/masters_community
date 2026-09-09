import { useCallback, useEffect, useState } from 'react';
import { ApiError, api } from '../../api/client';
import type { AdminReportItem, Code } from '../../api/types';
import StatusBadge from '../../components/StatusBadge';

/**
 * 신고 처리 (SCR-A02).
 *
 * 상태 탭을 하드코딩하지 않는다. RPT_STATUS 공통코드로 그린다 (DR-C05 · DR-P07).
 * 반영은 원문 수정 게시로 이어진다. 문항 수정 자체는 SCR-A04에서 한다.
 */
export default function AdminReportPage() {
  const [statuses, setStatuses] = useState<Code[]>([]);
  const [filter, setFilter] = useState('');
  const [items, setItems] = useState<AdminReportItem[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    api.codes('RPT_STATUS').then(setStatuses).catch(() => undefined);
  }, []);

  const load = useCallback(async () => {
    setError('');
    try {
      setItems(await api.admin.reports(filter || undefined));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '목록을 불러오지 못했습니다.');
    }
  }, [filter]);

  useEffect(() => {
    void load();
  }, [load]);

  async function change(reportId: number, statusCode: string) {
    try {
      await api.admin.changeReportStatus(reportId, statusCode);
      await load();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '상태를 바꾸지 못했습니다.');
    }
  }

  return (
    <div className="admin-main">
      <h1 className="admin-h">신고 처리</h1>
      <p className="admin-sub">반영은 원문 수정 게시로 이어집니다. 기각하면 원문은 그대로 둡니다.</p>

      <div className="tabs" role="tablist">
        <button role="tab" aria-selected={filter === ''} onClick={() => setFilter('')}>
          전체
        </button>
        {statuses.map((status) => (
          <button
            key={status.codeValue}
            role="tab"
            aria-selected={filter === status.codeValue}
            onClick={() => setFilter(status.codeValue)}
          >
            {status.codeName}
          </button>
        ))}
      </div>

      {error && <p className="err">{error}</p>}

      <div className="rows">
        {items.length === 0 && <div className="empty">해당 상태의 신고가 없습니다.</div>}
        {items.map((item) => {
          // 이미 처리된 건은 재처리할 수 없다 (SCR-A02 예외 표).
          const closed = item.statusCode === 'RPS003' || item.statusCode === 'RPS004';
          return (
            <div className="row" key={item.reportId}>
              <div className="meta">
                <div className="rt">
                  [{item.typeName}] {item.questionTitle}
                </div>
                <div className="rs">
                  {item.sourceLabel} · {new Date(item.createdAt).toLocaleString('ko-KR')}
                </div>
                <div className="rbody">{item.contents}</div>
                {!closed && (
                  <div className="acts">
                    {item.statusCode === 'RPS001' && (
                      <button className="mini sec" onClick={() => change(item.reportId, 'RPS002')}>
                        검토 시작
                      </button>
                    )}
                    <button className="mini" onClick={() => change(item.reportId, 'RPS003')}>
                      반영
                    </button>
                    <button className="mini sec" onClick={() => change(item.reportId, 'RPS004')}>
                      기각
                    </button>
                  </div>
                )}
              </div>
              <StatusBadge statusCode={item.statusCode} statusName={item.statusName} />
            </div>
          );
        })}
      </div>
    </div>
  );
}
