import { Fragment } from 'react';
import type { Drawing } from '../api/types';
import DrawingFigure from './DrawingFigure';

interface Props {
  contents: string;
  drawings: Drawing[];
  /** 해설·추가풀이 본문에만 true. 문제 본문·목록·관리자 화면은 기본 서체다 (DR-P08). */
  handwriting?: boolean;
  onZoom: (drawing: Drawing) => void;
}

const TOKEN = /(\[\[drawing:\d+]])/;
const TOKEN_ONE = /^\[\[drawing:(\d+)]]$/;

/**
 * 본문 렌더링 (DR-F03).
 *
 * 왜 dangerouslySetInnerHTML을 쓰지 않는가:
 * 텍스트 조각은 React가 자동으로 이스케이프한다. 그래서 사용자 입력(추가풀이)에서도
 * XSS가 발생하지 않는다. 이스케이프 먼저, 토큰 치환 나중이라는 순서가 여기서는
 * 구조적으로 지켜진다 — 치환 대상은 문자열이 아니라 이미 잘린 조각이다.
 *
 * 번호는 순서가 아니다. 표시 순서는 토큰이 놓인 위치가 정한다 (DR-F01).
 * 그래서 drawings 배열을 순회하지 않고 본문을 잘라 가며 그린다.
 */
export default function ContentsView({ contents, drawings, handwriting, onZoom }: Props) {
  const parts = contents.split(TOKEN);

  return (
    <div className={handwriting ? 'answer-body' : undefined}>
      {parts.map((part, index) => {
        const matched = part.match(TOKEN_ONE);

        if (!matched) {
          // 빈 조각은 그리지 않는다. 토큰이 줄 처음이나 끝에 있으면 생긴다.
          return part ? <p key={index}>{part.replace(/^\n+|\n+$/g, '')}</p> : null;
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
