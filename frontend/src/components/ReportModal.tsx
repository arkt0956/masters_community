import { useEffect, useState } from 'react';
import { ApiError, api } from '../api/client';
import type { Code, QuestionListItem } from '../api/types';

interface Props {
  question: QuestionListItem;
  /** 과목별 경로로 들어왔을 때만 오분류(RPT003)를 노출한다 (SCR-003 비고 · SCR-005 ②). */
  allowMisclassification: boolean;
  onClose: () => void;
}

/**
 * 신고 입력 (SCR-005).
 *
 * 신고 유형을 하드코딩하지 않는다. RPT_TYPE 공통코드를 받아 그린다 (DR-C05 · DR-P07).
 */
export default function ReportModal({ question, allowMisclassification, onClose }: Props) {
  const [types, setTypes] = useState<Code[]>([]);
  const [typeCode, setTypeCode] = useState('');
  const [contents, setContents] = useState('');
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);
  const [done, setDone] = useState(false);

  useEffect(() => {
    api.codes('RPT_TYPE').then(setTypes).catch(() => setError('신고 유형을 불러오지 못했습니다.'));
  }, []);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  // RPT003(오분류)는 과목별 경로에서만 고를 수 있다.
  const visibleTypes = types.filter((t) => allowMisclassification || t.codeValue !== 'RPT003');

  async function submit() {
    setError('');
    if (!typeCode) {
      setError('신고 유형을 선택해 주세요.');
      return;
    }
    // R-29 — 미입력만 제어한다. 최소·최대 길이 제한은 없다.
    if (!contents.trim()) {
      setError('어느 부분이 잘못됐는지 적어 주세요.');
      return;
    }
    setSending(true);
    try {
      await api.createReport({ questionId: question.questionId, typeCode, contents });
      setDone(true);
    } catch (e) {
      // 제한 초과와 검증 실패를 같은 문구로 묶지 않는다. 사용자가 할 일이 다르다.
      setError(e instanceof ApiError ? e.message : '신고를 접수하지 못했습니다.');
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="rp" role="dialog" aria-modal="true" aria-labelledby="rp-title">
      <div className="dim" onClick={onClose} />
      <div className="rp-sheet">
        {done ? (
          <div className="rp-done">
            <div className="ok" aria-hidden="true">
              ✓
            </div>
            <h2>신고가 접수되었습니다</h2>
            <p>관리자가 확인한 뒤 반영하거나 기각합니다.</p>
            {/* 결과 미통지 사실을 완료 안내에 반드시 명시한다 (SCR-005 ⑤) */}
            <p>처리 결과는 별도로 안내되지 않습니다.</p>
            <div className="rp-acts">
              <button className="btn" type="button" onClick={onClose}>
                확인
              </button>
            </div>
          </div>
        ) : (
          <>
            <div className="rp-head">
              <h2 id="rp-title">신고하기</h2>
              <button className="rp-x" type="button" onClick={onClose} aria-label="닫기">
                ✕
              </button>
            </div>
            <p className="rp-target">
              제{question.examRound}회 {question.sessionNo}교시 {question.questionNo}번 ·{' '}
              {question.title}
            </p>

            <span className="rp-label">어떤 문제인가요?</span>
            <div className="rp-types" role="radiogroup" aria-label="신고 유형">
              {visibleTypes.map((type) => (
                <label key={type.codeValue}>
                  <input
                    type="radio"
                    name="rptype"
                    value={type.codeValue}
                    checked={typeCode === type.codeValue}
                    onChange={() => setTypeCode(type.codeValue)}
                  />
                  <span>{type.codeName}</span>
                </label>
              ))}
            </div>

            <label className="rp-label" htmlFor="rp-detail">
              어느 부분이 잘못됐나요?
            </label>
            <textarea
              id="rp-detail"
              value={contents}
              onChange={(e) => setContents(e.target.value)}
              placeholder="예: 세 번째 문단의 수치 표기가 도면과 다릅니다. 어느 부분인지 적어 주세요."
            />

            {error && <p className="err">{error}</p>}

            <div className="rp-acts">
              <button className="btn ghost" type="button" onClick={onClose}>
                취소
              </button>
              {/* 전송 중 중복 클릭 차단 (SCR-005 ④) */}
              <button className="btn" type="button" onClick={submit} disabled={sending}>
                {sending ? '접수 중…' : '신고 접수'}
              </button>
            </div>
            <p className="rp-note">
              문제 오류로 접수된 건은 관리자가 원문과 대조해 판정합니다. 전사 오류면 반영하고 원문
              자체의 오류면 기각합니다.
            </p>
          </>
        )}
      </div>
    </div>
  );
}
