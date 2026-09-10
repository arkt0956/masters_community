import { useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import type { Drawing, ExtraSolutionPublic, QuestionListItem, Solution } from '../api/types';
import ContentsView from './ContentsView';

interface Props {
  question: QuestionListItem;
  onZoom: (drawing: Drawing) => void;
  onReport: (question: QuestionListItem) => void;
}

/**
 * 문항 카드 (SCR-004).
 *
 * 해설은 기본 미노출이다. 버튼을 눌렀을 때만 서버에서 받아 표시한다 (R-05 · R-09).
 * 두 번째부터는 받아둔 값을 쓴다 — 반복 클릭 부하를 막기 위해서다 (안건 7 확정).
 */
export default function QuestionCard({ question, onZoom, onReport }: Props) {
  const [open, setOpen] = useState(false);
  const [solution, setSolution] = useState<Solution | null>(null);
  const [extras, setExtras] = useState<ExtraSolutionPublic[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function toggle() {
    if (open) {
      setOpen(false);
      return;
    }
    // 이미 받아둔 값이 있으면 다시 요청하지 않는다 (R-09).
    if (solution) {
      setOpen(true);
      return;
    }
    setLoading(true);
    setError('');
    try {
      const [loaded, loadedExtras] = await Promise.all([
        api.solution(question.questionId),
        api.extraSolutionsOf(question.questionId),
      ]);
      setSolution(loaded);
      setExtras(loadedExtras);
      setOpen(true);
    } catch {
      // 실패하면 버튼 상태를 원복한다 (SCR-004 예외 표).
      setError('해설을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <article className={`item${open ? ' open' : ''}`}>
      <header className="item-head">
        <span className="no">문항 {question.questionNo}</span>
        {/* 출처 표기는 공공누리 제1유형의 이용 조건이므로 필수다 (R-06) */}
        <span className="stamp" role="note">
          <span className="stamp-k">출처</span>
          <span className="stamp-v">
            제{question.examRound}회 {question.sessionNo}교시 {question.questionNo}번
          </span>
        </span>
      </header>

      <div className="head-main">
        <h2>{question.title}</h2>
      </div>

      {/* 문제 본문은 기본 서체다. 원문 게재이므로 형태를 유지한다 (DR-P08).
          관리자가 쓴 본문이므로 Markdown으로 해석한다 (DR-F03) */}
      <div className="qbody">
        <ContentsView
          contents={question.contents}
          drawings={question.drawings}
          markdown
          onZoom={onZoom}
        />
      </div>

      <div className="titleblock">
        <span className="chip chip-cat">{question.categoryName}</span>
        <span className="chip">
          제{question.examRound}회 {question.sessionNo}교시
        </span>
        <span className="tb-action">
          <button className="btn ghost" type="button" onClick={() => onReport(question)}>
            신고
          </button>
          <Link
            className="btn ghost"
            to={`/extra-solutions/new?questionId=${question.questionId}`}
          >
            추가풀이
          </Link>
          {question.hasSolution ? (
            <button
              className="btn"
              type="button"
              onClick={toggle}
              disabled={loading}
              aria-expanded={open}
            >
              {loading ? '불러오는 중…' : open ? '해설 닫기' : '해설보기'}
            </button>
          ) : (
            <>
              <span className="pending">해설 준비 중</span>
              <button className="btn" type="button" disabled>
                해설보기
              </button>
            </>
          )}
        </span>
      </div>

      {error && (
        <div className="ans">
          <p className="err">{error}</p>
        </div>
      )}

      {open && solution && (
        <div className="ans">
          <div className="ans-in">
            <div className="ans-head">
              <span className="eyebrow">모범답안</span>
            </div>
            {/* 해설 본문에만 손글씨체 (DR-P08). 관리자가 쓴 본문이므로 Markdown (DR-F03) */}
            <ContentsView
              contents={solution.contents}
              drawings={solution.drawings}
              handwriting
              markdown
              onZoom={onZoom}
            />

            {extras.length > 0 && (
              <div style={{ marginTop: 20 }}>
                <div className="ans-head">
                  <span className="eyebrow">추가풀이</span>
                </div>
                {extras.map((extra) => (
                  <div className="extra" key={extra.extraSolutionId}>
                    <div className="extra-head">
                      <span className="extra-who">{extra.userName}</span>
                      <span className="extra-when">
                        {new Date(extra.createdAt).toLocaleDateString('ko-KR')}
                      </span>
                    </div>
                    {/* 추가풀이도 답안이므로 손글씨체다. 사용자 입력이지만 React가
                        텍스트를 이스케이프하므로 XSS는 발생하지 않는다 (DR-F03).
                        markdown을 켜지 않는다 — 로그인 없는 사용자 입력이라 서식을 열지
                        않기로 했다. 표가 필요하면 도면으로 올린다 */}
                    <ContentsView
                      contents={extra.contents}
                      drawings={extra.drawings}
                      handwriting
                      onZoom={onZoom}
                    />
                  </div>
                ))}
              </div>
            )}

            <div className="ans-foot">
              <button className="btn ghost" type="button" onClick={() => setOpen(false)}>
                해설 닫기
              </button>
            </div>
          </div>
        </div>
      )}
    </article>
  );
}
