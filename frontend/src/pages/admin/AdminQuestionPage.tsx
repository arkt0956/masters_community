import { useCallback, useEffect, useMemo, useState } from 'react';
import { ApiError, api, drawingUrl } from '../../api/client';
import type {
  AdminExam,
  AdminQuestionDetail,
  AdminQuestionItem,
  Code,
  Drawing,
} from '../../api/types';
import StatusBadge from '../../components/StatusBadge';

/**
 * 문제·풀이 작성 / 게시 (SCR-A04 · R-13~R-17).
 *
 * 코드 수정·재배포 없이 콘텐츠를 추가할 수 있어야 한다 (R-13). 이 화면이 그 수단이다.
 *
 * 도면은 슬롯 1개 = 파일 1개다 (개발규칙 5장 확정 사항). 업로드하면 서버가 정한
 * drawing_no를 받아 [[drawing:n]] 토큰을 본문에 넣어 준다. 사용자가 토큰을 직접
 * 타이핑하게 두지 않는다 (DR-F03 에디터 절).
 */
export default function AdminQuestionPage() {
  const [statuses, setStatuses] = useState<Code[]>([]);
  const [filter, setFilter] = useState('');
  const [items, setItems] = useState<AdminQuestionItem[]>([]);
  const [editing, setEditing] = useState<AdminQuestionDetail | 'new' | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.codes('Q_STATUS').then(setStatuses).catch(() => undefined);
  }, []);

  const load = useCallback(async () => {
    setError('');
    try {
      setItems(await api.admin.questions(filter || undefined));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '목록을 불러오지 못했습니다.');
    }
  }, [filter]);

  useEffect(() => {
    void load();
  }, [load]);

  async function open(questionId: number) {
    setEditing(await api.admin.question(questionId));
  }

  async function act(fn: () => Promise<unknown>) {
    setError('');
    try {
      await fn();
      await load();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '처리하지 못했습니다.');
    }
  }

  return (
    <div className="admin-main">
      <h1 className="admin-h">문제·풀이 게시</h1>
      <p className="admin-sub">
        작성중은 비공개, 게시됨은 사용자에게 공개됩니다. 게시할 때 본문의 도면 토큰이 검증됩니다.
      </p>

      <div className="tabs" role="tablist">
        <button role="tab" aria-selected={filter === ''} onClick={() => setFilter('')}>
          전체 (삭제 제외)
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
        <button className="mini" style={{ marginLeft: 'auto' }} onClick={() => setEditing('new')}>
          새 문항
        </button>
      </div>

      {error && <p className="err">{error}</p>}

      {editing && (
        <QuestionEditor
          detail={editing === 'new' ? null : editing}
          onClose={() => setEditing(null)}
          onSaved={async (saved) => {
            setEditing(saved);
            await load();
          }}
        />
      )}

      <div className="rows">
        {items.length === 0 && <div className="empty">해당 상태의 문항이 없습니다.</div>}
        {items.map((item) => (
          <div className="row" key={item.questionId}>
            <div className="meta">
              <div className="rt">{item.title}</div>
              <div className="rs">
                제{item.examRound}회 {item.sessionNo}교시 {item.questionNo}번 · {item.categoryName}{' '}
                · 해설 {item.hasSolution ? '있음' : '없음'}
              </div>
              <div className="acts">
                <button className="mini sec" onClick={() => open(item.questionId)}>
                  편집
                </button>
                {item.statusCode === 'QST001' ? (
                  <button
                    className="mini"
                    onClick={() => act(() => api.admin.publishQuestion(item.questionId))}
                  >
                    게시
                  </button>
                ) : (
                  <button
                    className="mini sec"
                    onClick={() => act(() => api.admin.unpublishQuestion(item.questionId))}
                  >
                    작성중으로
                  </button>
                )}
                {/* 물리 삭제가 아니라 QST003 상태 전이다 (DR-L01) */}
                <button
                  className="mini sec"
                  onClick={() => act(() => api.admin.deleteQuestion(item.questionId))}
                >
                  삭제
                </button>
              </div>
            </div>
            <StatusBadge statusCode={item.statusCode} statusName={item.statusName} />
          </div>
        ))}
      </div>
    </div>
  );
}

interface EditorProps {
  detail: AdminQuestionDetail | null;
  onClose: () => void;
  onSaved: (saved: AdminQuestionDetail) => Promise<void>;
}

function QuestionEditor({ detail, onClose, onSaved }: EditorProps) {
  const [exams, setExams] = useState<AdminExam[]>([]);
  const [subjects, setSubjects] = useState<Code[]>([]);
  const [form, setForm] = useState({
    examId: detail?.examId ? String(detail.examId) : '',
    sessionNo: String(detail?.sessionNo ?? 1),
    questionNo: String(detail?.questionNo ?? 1),
    categoryCode: detail?.categoryCode ?? '',
    title: detail?.title ?? '',
    contents: detail?.contents ?? '',
  });
  const [solutionContents, setSolutionContents] = useState(detail?.solution?.contents ?? '');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.admin.exams().then(setExams).catch(() => undefined);
    // 과목은 관리자 경로로 받는다. 비활성 코드까지 보여야 기존 문항의 과목을 유지할 수 있다.
    api.codes('SUBJECT').then(setSubjects).catch(() => undefined);
  }, []);

  const majors = useMemo(() => subjects.filter((s) => s.codeLevel === 1), [subjects]);
  const currentMajor = useMemo(
    () => subjects.find((s) => s.codeValue === form.categoryCode)?.parentCode ?? '',
    [subjects, form.categoryCode],
  );
  const [major, setMajor] = useState('');
  useEffect(() => setMajor(currentMajor), [currentMajor]);

  const minors = useMemo(
    () => subjects.filter((s) => s.codeLevel === 2 && s.parentCode === major),
    [subjects, major],
  );

  async function save() {
    setError('');
    setSaving(true);
    try {
      const body = {
        examId: Number(form.examId),
        sessionNo: Number(form.sessionNo),
        questionNo: Number(form.questionNo),
        categoryCode: form.categoryCode,
        title: form.title,
        contents: form.contents,
      };
      let saved = detail
        ? await api.admin.updateQuestion(detail.questionId, body)
        : await api.admin.createQuestion(body);

      // 해설은 문항당 1건이므로 upsert다. 비어 있으면 저장하지 않는다.
      if (solutionContents.trim()) {
        saved = await api.admin.saveSolution(saved.questionId, solutionContents);
      }
      await onSaved(saved);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '저장하지 못했습니다.');
    } finally {
      setSaving(false);
    }
  }

  /** 업로드 후 받은 번호로 토큰을 본문 끝에 붙인다. 위치는 관리자가 옮기면 된다. */
  async function upload(target: 'question' | 'solution', file: File) {
    if (!detail) {
      setError('문항을 먼저 저장한 뒤 도면을 올려 주세요.');
      return;
    }
    setError('');
    try {
      const drawing =
        target === 'question'
          ? await api.admin.addQuestionDrawing(detail.questionId, file)
          : await api.admin.addSolutionDrawing(detail.questionId, file);

      const token = `\n\n[[drawing:${drawing.drawingNo}]]\n`;
      if (target === 'question') {
        setForm((prev) => ({ ...prev, contents: prev.contents + token }));
      } else {
        setSolutionContents((prev) => prev + token);
      }
      await onSaved(await api.admin.question(detail.questionId));
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '도면을 올리지 못했습니다.');
    }
  }

  async function removeDrawing(drawing: Drawing) {
    if (!detail) return;
    setError('');
    try {
      await api.admin.deleteDrawing(drawing.drawingId);
      // 서버가 본문의 해당 토큰도 함께 지운다 (DR-F01). 지운 결과를 다시 받아 온다.
      const reloaded = await api.admin.question(detail.questionId);
      setForm((prev) => ({ ...prev, contents: reloaded.contents }));
      setSolutionContents(reloaded.solution?.contents ?? '');
      await onSaved(reloaded);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '도면을 지우지 못했습니다.');
    }
  }

  return (
    <div className="editor">
      <h2>{detail ? `문항 편집 · #${detail.questionId}` : '새 문항'}</h2>

      <div className="grid2">
        <div className="field">
          <label htmlFor="q-exam">회차</label>
          <select
            id="q-exam"
            value={form.examId}
            onChange={(e) => setForm({ ...form, examId: e.target.value })}
          >
            <option value="">회차 선택</option>
            {exams.map((exam) => (
              <option key={exam.examId} value={exam.examId}>
                제{exam.examRound}회{exam.isPublic ? '' : ' (비공개)'}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="q-session">교시</label>
          <input
            id="q-session"
            type="number"
            min={1}
            value={form.sessionNo}
            onChange={(e) => setForm({ ...form, sessionNo: e.target.value })}
          />
        </div>
      </div>

      <div className="grid2">
        <div className="field">
          <label htmlFor="q-no">문항 번호</label>
          <input
            id="q-no"
            type="number"
            min={1}
            value={form.questionNo}
            onChange={(e) => setForm({ ...form, questionNo: e.target.value })}
          />
        </div>
        <div className="field">
          <label htmlFor="q-major">과목 대분류</label>
          <select
            id="q-major"
            value={major}
            onChange={(e) => {
              setMajor(e.target.value);
              // 대분류가 바뀌면 중분류 선택을 지운다. 문항은 중분류만 저장한다 (DR-C06).
              setForm({ ...form, categoryCode: '' });
            }}
          >
            <option value="">대분류 선택</option>
            {majors.map((m) => (
              <option key={m.codeValue} value={m.codeValue}>
                {m.codeName}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="field">
        <label htmlFor="q-cat">과목 중분류</label>
        <select
          id="q-cat"
          value={form.categoryCode}
          onChange={(e) => setForm({ ...form, categoryCode: e.target.value })}
          disabled={!major}
        >
          <option value="">중분류 선택</option>
          {minors.map((m) => (
            <option key={m.codeValue} value={m.codeValue}>
              {m.codeName}
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label htmlFor="q-title">제목</label>
        <input
          id="q-title"
          value={form.title}
          onChange={(e) => setForm({ ...form, title: e.target.value })}
        />
      </div>

      <div className="field">
        <label htmlFor="q-body">문제 본문</label>
        <textarea
          id="q-body"
          value={form.contents}
          onChange={(e) => setForm({ ...form, contents: e.target.value })}
        />
        <p className="hint">
          이미지 위치는 [[drawing:n]] 토큰으로 표시됩니다. 아래에서 도면을 올리면 토큰이 자동으로
          붙습니다. 토큰 위치를 옮기면 표시 순서가 바뀝니다.
        </p>
      </div>

      <DrawingSlots
        label="문제 도면"
        drawings={detail?.drawings ?? []}
        disabled={!detail}
        onUpload={(file) => upload('question', file)}
        onRemove={removeDrawing}
      />

      <div className="field">
        <label htmlFor="q-sol">해설 (모범답안)</label>
        <textarea
          id="q-sol"
          value={solutionContents}
          onChange={(e) => setSolutionContents(e.target.value)}
        />
      </div>

      <DrawingSlots
        label="답안 도면"
        drawings={detail?.solution?.drawings ?? []}
        disabled={!detail?.solution}
        onUpload={(file) => upload('solution', file)}
        onRemove={removeDrawing}
      />

      {error && <p className="err">{error}</p>}

      <div className="acts">
        <button className="mini" onClick={save} disabled={saving}>
          {saving ? '저장 중…' : '저장 (작성중 유지)'}
        </button>
        <button className="mini sec" onClick={onClose}>
          닫기
        </button>
      </div>
    </div>
  );
}

interface SlotProps {
  label: string;
  drawings: Drawing[];
  disabled: boolean;
  onUpload: (file: File) => void;
  onRemove: (drawing: Drawing) => void;
}

function DrawingSlots({ label, drawings, disabled, onUpload, onRemove }: SlotProps) {
  return (
    <div className="field">
      <label>{label}</label>
      <div className="slotbar">
        <input
          type="file"
          accept="image/png,image/jpeg,image/webp,image/svg+xml"
          disabled={disabled}
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) onUpload(file);
            e.target.value = '';
          }}
        />
        {disabled && <span className="file">본문을 먼저 저장해야 올릴 수 있습니다.</span>}
      </div>
      <div className="rows" style={{ marginTop: 8 }}>
        {drawings.map((drawing) => (
          <div className="dwg-admin" key={drawing.drawingId}>
            <img src={drawingUrl(drawing)} alt="" />
            <div className="meta">
              <div className="token">[[drawing:{drawing.drawingNo}]]</div>
              <div className="rs">{drawing.fileName}</div>
            </div>
            {/* 번호는 당기지 않는다. 본문의 해당 토큰만 함께 지워진다 (DR-F01) */}
            <button className="mini sec" onClick={() => onRemove(drawing)}>
              삭제
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
