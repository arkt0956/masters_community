import { useCallback, useEffect, useState } from 'react';
import { ApiError, api } from '../../api/client';
import type { AdminExtraSolutionItem, Code } from '../../api/types';
import StatusBadge from '../../components/StatusBadge';

/**
 * 추가풀이 검토 (SCR-A03).
 *
 * 비밀번호는 조회 키일 뿐이므로 화면에 노출하지 않는다. 서버 응답에도 담기지 않는다.
 * 반려 사유는 선택 입력이며, 입력하면 SCR-007에 표시된다 (R-39).
 */
export default function AdminExtraSolutionPage() {
  const [statuses, setStatuses] = useState<Code[]>([]);
  const [filter, setFilter] = useState('');
  const [items, setItems] = useState<AdminExtraSolutionItem[]>([]);
  const [reasons, setReasons] = useState<Record<number, string>>({});
  const [error, setError] = useState('');

  useEffect(() => {
    api.codes('ES_STATUS').then(setStatuses).catch(() => undefined);
  }, []);

  const load = useCallback(async () => {
    setError('');
    try {
      setItems(await api.admin.extraSolutions(filter || undefined));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '목록을 불러오지 못했습니다.');
    }
  }, [filter]);

  useEffect(() => {
    void load();
  }, [load]);

  async function change(id: number, statusCode: string) {
    try {
      await api.admin.changeExtraSolutionStatus(id, statusCode, reasons[id]);
      await load();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '상태를 바꾸지 못했습니다.');
    }
  }

  return (
    <div className="admin-main">
      <h1 className="admin-h">추가풀이 검토</h1>
      <p className="admin-sub">
        게시하면 사용자에게 공개됩니다. 반려 사유를 적으면 작성자가 현황 조회에서 볼 수 있습니다.
      </p>

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
        {items.length === 0 && <div className="empty">해당 상태의 등록건이 없습니다.</div>}
        {items.map((item) => {
          const closed = item.statusCode === 'EXS003' || item.statusCode === 'EXS004';
          return (
            <div className="row" key={item.extraSolutionId}>
              <div className="meta">
                <div className="rt">
                  {item.userName} · {item.questionTitle}
                </div>
                <div className="rs">
                  {item.sourceLabel} · {new Date(item.createdAt).toLocaleString('ko-KR')}
                </div>
                <div className="rbody">{item.contents}</div>
                {item.rejectReason && <div className="rs">반려 사유: {item.rejectReason}</div>}

                {!closed && (
                  <>
                    {item.statusCode === 'EXS001' && (
                      <div className="acts">
                        <button
                          className="mini sec"
                          onClick={() => change(item.extraSolutionId, 'EXS002')}
                        >
                          검토 시작
                        </button>
                      </div>
                    )}
                    {item.statusCode === 'EXS002' && (
                      <>
                        <textarea
                          className="reason"
                          placeholder="반려 사유 (선택)"
                          value={reasons[item.extraSolutionId] ?? ''}
                          onChange={(e) =>
                            setReasons((prev) => ({
                              ...prev,
                              [item.extraSolutionId]: e.target.value,
                            }))
                          }
                        />
                        <div className="acts">
                          <button
                            className="mini"
                            onClick={() => change(item.extraSolutionId, 'EXS003')}
                          >
                            게시
                          </button>
                          <button
                            className="mini sec"
                            onClick={() => change(item.extraSolutionId, 'EXS004')}
                          >
                            반려
                          </button>
                        </div>
                      </>
                    )}
                  </>
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
