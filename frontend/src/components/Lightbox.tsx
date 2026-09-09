import { useEffect, useState } from 'react';
import { drawingUrl } from '../api/client';
import type { Drawing } from '../api/types';

interface Props {
  drawing: Drawing | null;
  onClose: () => void;
}

const STEPS = [0.5, 0.75, 1, 1.5, 2, 3, 4];
const FIT_INDEX = 2;

/**
 * 도면 확대 오버레이 (SCR-004 이벤트 2 · R-11).
 *
 * 원본 비율을 유지한다. width만 배율로 조정하고 height는 auto로 둔다.
 */
export default function Lightbox({ drawing, onClose }: Props) {
  const [step, setStep] = useState(FIT_INDEX);

  // 열릴 때마다 배율을 화면 맞춤으로 되돌린다. 앞 도면의 배율이 남으면 혼란스럽다.
  useEffect(() => {
    if (drawing) setStep(FIT_INDEX);
  }, [drawing]);

  useEffect(() => {
    if (!drawing) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [drawing, onClose]);

  if (!drawing) return null;

  const scale = STEPS[step];

  return (
    <div className="lb" role="dialog" aria-modal="true" aria-label="도면 확대">
      <div className="lb-bar">
        <span className="cap">
          도면 {drawing.drawingNo} · {Math.round(scale * 100)}%
        </span>
        <button type="button" onClick={() => setStep((s) => Math.max(0, s - 1))}>
          축소
        </button>
        <button type="button" onClick={() => setStep(FIT_INDEX)}>
          화면 맞춤
        </button>
        <button type="button" onClick={() => setStep((s) => Math.min(STEPS.length - 1, s + 1))}>
          확대
        </button>
        <button type="button" onClick={onClose}>
          닫기
        </button>
      </div>
      <div className="lb-scroll">
        <img src={drawingUrl(drawing)} alt="" style={{ width: `${scale * 100}%` }} />
      </div>
    </div>
  );
}
