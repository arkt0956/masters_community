import MarkdownIt from 'markdown-it';

/**
 * 본문 Markdown 렌더러 (DR-F03).
 *
 * 이 파일의 설정이 곧 XSS 방어선이다. ContentsView가 결과를 dangerouslySetInnerHTML로
 * 넣으므로, 그 한 줄이 안전한 근거는 여기 있는 옵션뿐이다.
 *
 * 새니타이저로 걸러내는 방식이 아니라 애초에 만들지 않는 방식이다. 걸러내는 쪽은
 * 우회 기법이 새로 나올 때마다 따라가야 한다.
 *
 * 적용 대상은 관리자가 쓰는 본문(문제·해설)뿐이다. 추가풀이는 평문 경로를 그대로 쓴다.
 */
const md = new MarkdownIt({
  // 원문의 원시 HTML을 태그로 해석하지 않고 이스케이프한다. 이 옵션을 켜면
  // DR-F03을 어기는 것이다. 켜야 할 이유가 생기면 규칙부터 고친다.
  html: false,
  // 기출 원문은 줄바꿈이 의미를 갖는다. 빈 줄 하나를 요구하면 입력한 모양과 달라진다.
  breaks: true,
  // 맨 URL을 링크로 바꾸지 않는다. 본문에 링크가 필요한 서비스가 아니다.
  linkify: false,
  typographer: false,
});

// 이미지는 [[drawing:n]] 토큰이 담당한다 (DR-F03). Markdown 이미지 문법을 열어 두면
// 본문에서 외부 URL을 부를 수 있고, 도면 소유·검증 체계 밖의 이미지가 생긴다.
md.disable('image');

// 링크 스킴 제한. markdown-it 기본값도 javascript:를 막지만, 방어를 설정에 기대지 않고
// 명시한다. 기본값이 바뀌어도 여기서 걸린다.
const ALLOWED_SCHEME = /^(https?:|mailto:|#|\/)/i;
const defaultValidateLink = md.validateLink.bind(md);
md.validateLink = (url: string) => ALLOWED_SCHEME.test(url.trim()) && defaultValidateLink(url);

// 표는 폭이 넘칠 수 있다. 표만 따로 감싸 가로 스크롤을 주고 본문은 그대로 둔다.
// 감싸지 않으면 표 하나 때문에 화면 전체가 좌우로 밀린다.
md.renderer.rules.table_open = () => '<div class="md-table">\n<table>\n';
md.renderer.rules.table_close = () => '</table>\n</div>\n';

/** 본문 조각 하나를 HTML로 바꾼다. 토큰으로 자른 뒤에 호출해야 한다 (DR-F03). */
export function renderMarkdown(source: string): string {
  return md.render(source);
}
