import { Fragment } from 'react';
import type { Drawing } from '../api/types';
import DrawingFigure from './DrawingFigure';
import { renderMarkdown } from './markdown';

interface Props {
  contents: string;
  drawings: Drawing[];
  /** 해설·추가풀이 본문에만 true. 문제 본문·목록·관리자 화면은 기본 서체다 (DR-P08). */
  handwriting?: boolean;
  /**
   * 관리자가 쓴 본문(문제·해설)에만 true. 추가풀이는 평문이다 (DR-F03).
   *
   * 기본값을 false로 두어, 새로 쓰는 화면이 지정을 빠뜨리면 안전한 평문 경로로 간다.
   */
  markdown?: boolean;
  onZoom: (drawing: Drawing) => void;
}

const TOKEN = /(\[\[drawing:\d+]])/;
const TOKEN_ONE = /^\[\[drawing:(\d+)]]$/;

/**
 * 본문 렌더링 (DR-F03).
 *
 * 토큰으로 먼저 자르고, 잘린 조각을 각각 처리한다. 순서를 바꾸면 안 된다.
 * 본문 전체를 한 번에 변환한 뒤 토큰을 치환하면, 토큰이 코드 블록이나 표 셀 안에
 * 들어갔을 때 어디를 치환해야 하는지 알 수 없다.
 *
 * 조각을 처리하는 방식이 둘로 갈린다:
 *   · 평문(추가풀이) — React가 이스케이프한다. 사용자 입력이 태그가 될 경로가 없다.
 *   · Markdown(문제·해설) — markdown-it이 만든 HTML을 넣는다. 안전한 근거는
 *     markdown.ts의 설정(html: false 등)뿐이므로 그 파일을 함께 읽어야 한다.
 *
 * 번호는 순서가 아니다. 표시 순서는 토큰이 놓인 위치가 정한다 (DR-F01).
 * 그래서 drawings 배열을 순회하지 않고 본문을 잘라 가며 그린다.
 */
export default function ContentsView({
  contents,
  drawings,
  handwriting,
  markdown,
  onZoom,
}: Props) {
  const parts = contents.split(TOKEN);

  return (
    <div className={handwriting ? 'answer-body' : undefined}>
      {parts.map((part, index) => {
        const matched = part.match(TOKEN_ONE);

        if (!matched) {
          // 빈 조각은 그리지 않는다. 토큰이 줄 처음이나 끝에 있으면 생긴다.
          const text = part.replace(/^\n+|\n+$/g, '');
          if (!text) return null;
          return markdown ? (
            <div
              key={index}
              className="md"
              dangerouslySetInnerHTML={{ __html: renderMarkdown(text) }}
            />
          ) : (
            <p key={index}>{text}</p>
          );
        }

        const drawingNo = Number(matched[1]);
        const found = drawings.filter((d) => d.drawingNo === drawingNo);

        // 게시 시점 검증(DR-F03)을 통과했으면 고아 토큰은 없다. 그래도 방어적으로
        // 비워 둔다. 토큰 원문을 그대로 보여주면 사용자에게 의미 없는 문자열이 노출된다.
        return (
          <Fragment key={index}>
            {found.length > 0 && (
              <div className="dwgs">
                {found.map((drawing) => (
                  <DrawingFigure key={drawing.drawingId} drawing={drawing} onZoom={onZoom} />
                ))}
              </div>
            )}
          </Fragment>
        );
      })}
    </div>
  );
}
