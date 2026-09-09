import { drawingUrl } from '../api/client';
import type { Drawing } from '../api/types';

interface Props {
  drawing: Drawing;
  onZoom: (drawing: Drawing) => void;
}

/**
 * 도면 한 장 (SCR-004 ④).
 *
 * 원본 비율을 유지하고 확대할 수 있어야 한다 (R-11). CSS의 object-fit: contain이
 * 비율을 지키고, 확대는 오버레이가 맡는다.
 *
 * alt는 비워 둔다. 도면 내용은 본문이 설명하고 있어서, 파일명을 alt에 넣으면
 * 스크린리더가 의미 없는 문자열을 읽는다. 대신 figcaption에 식별 정보를 둔다.
 */
export default function DrawingFigure({ drawing, onZoom }: Props) {
  return (
    <figure className="dwg">
      <img
        src={drawingUrl(drawing)}
        alt=""
        onClick={() => onZoom(drawing)}
        loading="lazy"
      />
      <figcaption>
        도면 {drawing.drawingNo}
        <button className="zoom" type="button" onClick={() => onZoom(drawing)}>
          확대
        </button>
      </figcaption>
    </figure>
  );
}
