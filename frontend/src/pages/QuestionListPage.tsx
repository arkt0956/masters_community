import { useCallback, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api } from '../api/client';
import type { Drawing, QuestionListItem } from '../api/types';
import Lightbox from '../components/Lightbox';
import QuestionCard from '../components/QuestionCard';
import ReportModal from '../components/ReportModal';

/**
 * 문제 목록 / 상세 (SCR-004 · 공용).
 *
 * 세 경로(회차별·과목별·랜덤)가 이 화면을 함께 쓴다. 목록 데이터만 다르고
 * 상세 인터랙션은 동일하다 (화면정의서 2절 비고).
 *
 * 경로는 쿼리로 구분한다.
 *   /questions?examId=1&sessionNo=2   회차·교시
 *   /questions?categoryCode=STM005    과목
 *   /questions?mode=random            랜덤
 */
export default function QuestionListPage() {
  const [params] = useSearchParams();
  const [questions, setQuestions] = useState<QuestionListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [zoomed, setZoomed] = useState<Drawing | null>(null);
  const [reporting, setReporting] = useState<QuestionListItem | null>(null);

  const examId = params.get('examId');
  const sessionNo = params.get('sessionNo');
  const categoryCode = params.get('categoryCode');
  const random = params.get('mode') === 'random';

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      if (random) {
        setQuestions(await api.randomQuestions());
      } else if (categoryCode) {
        setQuestions(await api.categoryQuestions(categoryCode));
      } else if (examId && sessionNo) {
        setQuestions(await api.examQuestions(Number(examId), Number(sessionNo)));
      } else {
        setError('조회 조건이 없습니다.');
      }
    } catch {
      setError('문항을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    } finally {
      setLoading(false);
    }
  }, [random, categoryCode, examId, sessionNo]);

  useEffect(() => {
    void load();
  }, [load]);

  const { backTo, backLabel, title, subtitle } = context(
    random,
    categoryCode,
    questions,
    sessionNo,
  );

  return (
    <>
      <div className="topbar">
        <div className="topbar-in">
          <Link className="back" to={backTo}>
            <svg width="18" height="18" viewBox="0 0 20 20" fill="none" aria-hidden="true">
              <path
                d="M12.5 4L6.5 10l6 6"
                stroke="currentColor"
                strokeWidth="1.9"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
            <span>{backLabel}</span>
          </Link>
          <div className="crumb">
            <span>{title}</span>
            <small>{subtitle}</small>
          </div>
          {random && (
            <button className="reshuffle" type="button" onClick={() => void load()}>
              다시 섞기
            </button>
          )}
        </div>
      </div>

      <main className="wide">
        {loading && <p className="loading">불러오는 중…</p>}
        {error && <p className="err">{error}</p>}
        {!loading && !error && questions.length === 0 && (
          <div className="empty">표시할 문항이 없습니다.</div>
        )}

        {questions.map((question) => (
          <QuestionCard
            key={question.questionId}
            question={question}
            onZoom={setZoomed}
            onReport={setReporting}
          />
        ))}
      </main>

      <Lightbox drawing={zoomed} onClose={() => setZoomed(null)} />

      {reporting && (
        <ReportModal
          question={reporting}
          // 오분류 신고는 과목별 경로에서만 노출한다 (SCR-003 비고 · SCR-005 ②)
          allowMisclassification={Boolean(categoryCode)}
          onClose={() => setReporting(null)}
        />
      )}
    </>
  );
}

function context(
  random: boolean,
  categoryCode: string | null,
  questions: QuestionListItem[],
  sessionNo: string | null,
) {
  if (random) {
    return {
      backTo: '/',
      backLabel: '메인',
      title: '랜덤 연습',
      subtitle: `무작위 ${questions.length}문항`,
    };
  }
  if (categoryCode) {
    return {
      backTo: '/categories',
      backLabel: '과목',
      title: questions[0]?.categoryName ?? '과목별',
      subtitle: `${questions.length}문항`,
    };
  }
  const round = questions[0]?.examRound;
  return {
    backTo: '/exams',
    backLabel: '회차',
    title: round ? `제${round}회 ${sessionNo}교시` : '문항',
    subtitle: `${questions.length}문항`,
  };
}
