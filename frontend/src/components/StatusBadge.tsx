interface Props {
  statusCode: string;
  statusName: string;
}

/**
 * 상태 배지.
 *
 * 라벨은 서버가 내려준 codeName을 그대로 쓴다. 코드값으로 라벨을 만들면
 * 관리자가 코드명을 바꿔도 화면에 반영되지 않는다 (DR-C05 · DR-P07).
 *
 * 색만 코드값으로 정한다. 색은 표시 규칙이지 데이터가 아니다.
 */
export default function StatusBadge({ statusCode, statusName }: Props) {
  return (
    <span className="st" data-tone={tone(statusCode)}>
      {statusName}
    </span>
  );
}

function tone(statusCode: string): string {
  switch (statusCode) {
    case 'QST001': // 작성중
    case 'RPS001': // 접수
    case 'EXS001': // 검토대기
      return 'wait';
    case 'RPS002': // 검토중
    case 'EXS002':
      return 'progress';
    case 'QST002': // 게시됨
    case 'RPS003': // 반영
    case 'EXS003': // 게시
      return 'done';
    case 'QST003': // 삭제
    case 'RPS004': // 기각
    case 'EXS004': // 반려
      return 'reject';
    default:
      return 'neutral';
  }
}
