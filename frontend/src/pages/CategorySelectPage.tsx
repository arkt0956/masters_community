import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import type { Category } from '../api/types';

/**
 * 과목 선택 (SCR-003).
 *
 * 중분류가 108개라 대분류를 먼저 고르는 2단계 UI다 (DR-C06).
 * 화면정의서 v0.4는 1단계 목록이지만, 과목 체계가 큐넷 출제기준으로 확정되면서
 * 2단계가 필요해졌다 (개발규칙 5장 「남은 미결」 — 화면정의서 수정 필요).
 *
 * 과목명을 하드코딩하지 않는다. 문항 수와 함께 서버가 내려준다 (DR-C05 · DR-P07).
 */
export default function CategorySelectPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [major, setMajor] = useState<Category | null>(null);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    api.categories().then(setCategories).catch(() => setError('과목을 불러오지 못했습니다.'));
  }, []);

  const list = major ? major.children : categories;

  return (
    <div className="land">
      {major ? (
        <button className="crumb-btn" type="button" onClick={() => setMajor(null)}>
          ‹ 대분류
        </button>
      ) : (
        <Link className="crumb-btn" to="/">
          ‹ 메인
        </Link>
      )}

      <header className="sub-head">
        <h1>{major ? major.codeName : '과목별'}</h1>
        <p>
          {major
            ? '세부 과목을 고르면 해당 과목의 문제를 모아서 봅니다.'
            : '과목을 고르면 회차와 관계없이 해당 과목의 문제를 모아서 봅니다.'}
        </p>
      </header>

      <ul className="menu">
        {major && (
          <li>
            {/* 대분류 전체 보기. 서버는 대분류 코드를 parent_code로 조인해 처리한다 */}
            <button
              type="button"
              onClick={() => navigate(`/questions?categoryCode=${major.codeValue}`)}
            >
              <span className="ico" aria-hidden="true">
                전
              </span>
              <span className="txt">
                <b>{major.codeName} 전체</b>
                <span>이 대분류의 모든 문항</span>
              </span>
              <span className="cat-count">{major.questionCount}문항</span>
            </button>
          </li>
        )}
        {list.map((category) => (
          <li key={category.codeValue}>
            <button
              type="button"
              onClick={() =>
                major
                  ? navigate(`/questions?categoryCode=${category.codeValue}`)
                  : setMajor(category)
              }
            >
              <span className="txt">
                <b>{category.codeName}</b>
                {!major && <span>세부 과목 {category.children.length}개</span>}
              </span>
              <span className="cat-count">{category.questionCount}문항</span>
              <span className="arw" aria-hidden="true">
                ›
              </span>
            </button>
          </li>
        ))}
      </ul>

      {/* 문항 0건인 과목은 서버가 제외한다 (SCR-003 예외 표) */}
      {list.length === 0 && !error && <p className="hint">표시할 과목이 없습니다.</p>}
      {error && <p className="err">{error}</p>}
      <p className="hint">문항이 있는 과목만 표시됩니다.</p>
    </div>
  );
}
