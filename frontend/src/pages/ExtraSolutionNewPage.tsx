import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ApiError, api } from '../api/client';

/**
 * 첨부 상한. 서버의 csp.limit.extra-solution-files와 같은 값을 쓴다 (DR-P07).
 * 한 장당 용량은 spring.servlet.multipart.max-file-size와 맞춘다.
 */
const MAX_FILES = 5;
const MAX_MB = 10;

/**
 * 추가풀이 등록 (SCR-006).
 *
 * 등록 즉시 공개되지 않는다. 관리자 검토를 거친다.
 * 게시·반려 결과는 별도 알림 없이 SCR-007 현황 조회로만 확인하므로,
 * 완료 안내에 조회 방법과 ID·비밀번호 보관 필요성을 함께 고지한다 (안건 1 확정).
 */
export default function ExtraSolutionNewPage() {
  const [params] = useSearchParams();
  const questionId = Number(params.get('questionId'));

  const [userName, setUserName] = useState('');
  const [password, setPassword] = useState('');
  const [contents, setContents] = useState('');
  const [files, setFiles] = useState<File[]>([]);
  const [error, setError] = useState('');
  const [sending, setSending] = useState(false);
  const [done, setDone] = useState(false);

  /**
   * 파일을 목록에 더한다. 같은 input을 다시 열어도 앞서 고른 것이 유지되도록 누적한다.
   * 브라우저 기본 동작은 마지막 선택으로 교체하는 것이라 여러 번 나눠 고를 수 없다.
   */
  function addFiles(event: React.ChangeEvent<HTMLInputElement>) {
    const picked = Array.from(event.target.files ?? []);
    event.target.value = '';
    if (picked.length === 0) return;

    // 화면 검증은 사용자 편의다. 같은 규칙을 서버가 다시 검증한다 (DR-P04).
    const oversized = picked.find((file) => file.size > MAX_MB * 1024 * 1024);
    if (oversized) {
      setError(`${oversized.name} 은(는) ${MAX_MB}MB를 넘습니다.`);
      return;
    }
    if (files.length + picked.length > MAX_FILES) {
      setError(`이미지는 최대 ${MAX_FILES}장까지 첨부할 수 있습니다.`);
      return;
    }
    setError('');
    setFiles((prev) => [...prev, ...picked]);
  }

  function removeFile(index: number) {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  }

  async function submit() {
    setError('');
    // 화면 검증은 사용자 편의다. 같은 규칙을 서버가 다시 검증한다 (DR-P04 · DR-P07).
    if (userName.trim().length < 2 || userName.trim().length > 12) {
      setError('ID는 2~12자로 입력해 주세요.');
      return;
    }
    if (password.length < 4) {
      setError('비밀번호는 4자 이상으로 입력해 주세요.');
      return;
    }
    if (!contents.trim()) {
      setError('풀이 내용을 입력해 주세요.');
      return;
    }
    setSending(true);
    try {
      await api.createExtraSolution({ questionId, userName, password, contents }, files);
      setDone(true);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '등록하지 못했습니다.');
    } finally {
      setSending(false);
    }
  }

  if (!questionId) {
    return (
      <div className="land">
        <Link className="crumb-btn" to="/">
          ‹ 메인
        </Link>
        <div className="empty">대상 문항이 지정되지 않았습니다. 문제 화면에서 다시 시도해 주세요.</div>
      </div>
    );
  }

  if (done) {
    return (
      <div className="land">
        <header className="sub-head">
          <h1>등록되었습니다</h1>
          <p>관리자 검토를 거쳐 게시됩니다.</p>
        </header>
        <ul className="points">
          <li>
            <h3>결과는 현황 조회에서 확인합니다</h3>
            <p>
              게시·반려 결과는 별도로 안내되지 않습니다. 등록에 사용한 ID와 비밀번호로 추가풀이
              현황을 조회해 주세요.
            </p>
          </li>
          <li>
            <h3>ID와 비밀번호를 보관해 주세요</h3>
            <p>
              회원 계정이 아니라 본인 등록건 조회 키입니다. 재발급할 수 없으므로 잊으면 조회할 수
              없습니다.
            </p>
          </li>
        </ul>
        <div className="cta-wrap">
          <Link className="go" to="/extra-solutions/status" style={{ display: 'block', textAlign: 'center', textDecoration: 'none' }}>
            현황 조회로 이동
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="land">
      <button className="crumb-btn" type="button" onClick={() => history.back()}>
        ‹ 뒤로
      </button>
      <header className="sub-head">
        <h1>추가풀이 등록</h1>
        <p>등록한 풀이는 관리자 검토를 거쳐 게시됩니다. 등록 후에는 수정·삭제할 수 없습니다.</p>
      </header>

      <div className="field">
        <label htmlFor="es-id">ID (닉네임)</label>
        <input
          id="es-id"
          value={userName}
          maxLength={12}
          onChange={(e) => setUserName(e.target.value)}
          placeholder="2~12자"
        />
        <p className="hint">작성자 표시에 함께 쓰입니다. 같은 ID를 다른 사람이 쓸 수 있습니다.</p>
      </div>

      <div className="field">
        <label htmlFor="es-pw">비밀번호</label>
        <input
          id="es-pw"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="4자 이상"
        />
        <p className="hint">회원 비밀번호가 아니라 본인 등록건을 조회하는 키입니다.</p>
      </div>

      <div className="field">
        <label htmlFor="es-body">내용</label>
        <textarea
          id="es-body"
          value={contents}
          onChange={(e) => setContents(e.target.value)}
          placeholder="풀이 과정을 적어 주세요."
        />
      </div>

      <div className="field">
        <label htmlFor="es-files">이미지 (선택)</label>
        <input
          id="es-files"
          type="file"
          multiple
          accept="image/png,image/jpeg,image/webp"
          onChange={addFiles}
        />
        <p className="hint">
          손으로 푼 답안이나 도면을 사진으로 올릴 수 있습니다. 최대 {MAX_FILES}장, 한 장당{' '}
          {MAX_MB}MB까지이며 <strong>본문 뒤에 첨부한 순서대로</strong> 표시됩니다.
        </p>
        {files.length > 0 && (
          <ul className="filelist">
            {files.map((file, index) => (
              <li key={`${file.name}-${index}`}>
                <span className="fl-no">{index + 1}</span>
                <span className="fl-name">{file.name}</span>
                <span className="fl-size">{(file.size / 1024 / 1024).toFixed(1)}MB</span>
                <button type="button" className="fl-del" onClick={() => removeFile(index)}>
                  빼기
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {error && <p className="err">{error}</p>}

      <div className="cta-wrap">
        {/* 전송 중 중복 클릭 차단 (SCR-006 ④) */}
        <button className="go" onClick={submit} disabled={sending}>
          {sending ? '등록 중…' : '등록'}
        </button>
      </div>
    </div>
  );
}
