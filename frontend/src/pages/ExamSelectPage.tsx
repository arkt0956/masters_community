import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import type { Exam } from '../api/types';

/**
 * 회차·교시 선택 (SCR-002).
 *
 * 년도는 표기하지 않는다. 접근 축은 회차·교시다 (안건 6 확정).
 * 서버가 게시 문항이 있는 교시만 내려주므로, 받은 목록만 그리면
 * "콘텐츠 없는 교시 선택 불가"(R-03)가 지켜진다.
 */
export default function ExamSelectPage() {
  const [exams, setExams] = useState<Exam[]>([]);
  const [examId, setExamId] = useState('');
  const [sessionNo, setSessionNo] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    api
      .exams()
      .then((loaded) => {
        setExams(loaded);
        if (loaded.length > 0) setExamId(String(loaded[0].examId));
      })
      .catch(() => setError('회차를 불러오지 못했습니다.'));
  }, []);

  const selected = exams.find((e) => String(e.examId) === examId);

  return (
    <div className="land">
      <Link className="crumb-btn" to="/">
        ‹ 메인
      </Link>
      <header className="sub-head">
        <h1>회차별</h1>
        <p>회차와 교시를 고르면 그 시험의 문제를 순서대로 볼 수 있습니다.</p>
      </header>

      <div className="field">
        <label htmlFor="sel-round">회차</label>
        <select
          id="sel-round"
          value={examId}
          onChange={(e) => {
            setExamId(e.target.value);
            // 회차가 바뀌면 교시 구성이 달라질 수 있으므로 선택을 지운다 (R-02).
            setSessionNo('');
          }}
        >
          {exams.map((exam) => (
            <option key={exam.examId} value={exam.examId}>
              제{exam.examRound}회
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label htmlFor="sel-session">교시</label>
        <select
          id="sel-session"
          value={sessionNo}
          onChange={(e) => setSessionNo(e.target.value)}
          disabled={!selected}
        >
          <option value="">교시 선택</option>
          {selected?.sessions.map((session) => (
            <option key={session.sessionNo} value={session.sessionNo}>
              {session.sessionNo}교시 ({session.questionCount}문항)
            </option>
          ))}
        </select>
      </div>

      <p className="hint">공개된 문항이 있는 회차·교시만 선택할 수 있어요.</p>
      {error && <p className="err">{error}</p>}

      <div className="cta-wrap">
        {/* 교시 미선택 시 비활성 (SCR-002 유효성) */}
        <button
          className="go"
          disabled={!examId || !sessionNo}
          onClick={() => navigate(`/questions?examId=${examId}&sessionNo=${sessionNo}`)}
        >
          문항 보기
        </button>
      </div>
    </div>
  );
}
