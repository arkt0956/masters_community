import type {
  AdminExam,
  AdminExtraSolutionItem,
  AdminMe,
  AdminQuestionDetail,
  AdminQuestionItem,
  AdminReportItem,
  Category,
  Code,
  Dashboard,
  Drawing,
  Exam,
  ExtraSolutionPublic,
  ExtraSolutionStatusItem,
  QuestionListItem,
  Solution,
} from './types';

/**
 * WAS 호출.
 *
 * 개발에서는 Vite 프록시가, 운영에서는 Nginx가 같은 오리진으로 묶는다.
 * 그래서 base URL을 두지 않는다 (deploy/nginx.conf).
 */

/** 서버가 내려준 메시지를 그대로 화면에 쓴다. 메시지 문구는 서버가 정한다 (DR-P05). */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
  }

  /** 401 — 관리자 세션 만료. 화면이 로그인으로 보낸다 (R-55). */
  get isUnauthorized() {
    return this.status === 401;
  }

  /** 429 — IP 일일 제한 초과. "잘못 입력했다"와 다르게 안내한다 (R-46 · R-48). */
  get isRateLimited() {
    return this.status === 429;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    // 관리자 세션 쿠키를 함께 보낸다 (R-55).
    credentials: 'same-origin',
    ...init,
  });

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  const body = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new ApiError(response.status, body?.message ?? '요청을 처리하지 못했습니다.');
  }
  return body as T;
}

function json(method: string, body: unknown): RequestInit {
  return {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  };
}

/** 도면 이미지 주소. 저장 파일명이 아니라 file_uuid를 쓴다 (R-53). */
export function drawingUrl(drawing: Drawing): string {
  return `/api/drawings/${drawing.fileUuid}`;
}

export const api = {
  // ------------------------------------------------------------ 사용자
  exams: () => request<Exam[]>('/api/exams'),

  examQuestions: (examId: number, sessionNo: number) =>
    request<QuestionListItem[]>(`/api/exams/${examId}/questions?sessionNo=${sessionNo}`),

  categories: () => request<Category[]>('/api/categories'),

  categoryQuestions: (categoryCode: string) =>
    request<QuestionListItem[]>(`/api/categories/${encodeURIComponent(categoryCode)}/questions`),

  randomQuestions: () => request<QuestionListItem[]>('/api/questions/random'),

  /** 최초 1회만 호출하고 이후에는 받아둔 값을 쓴다 (R-05, 안건 7). 캐시는 호출부가 한다. */
  solution: (questionId: number) => request<Solution>(`/api/questions/${questionId}/solution`),

  extraSolutionsOf: (questionId: number) =>
    request<ExtraSolutionPublic[]>(`/api/questions/${questionId}/extra-solutions`),

  createReport: (body: { questionId: number; typeCode: string; contents: string }) =>
    request<void>('/api/reports', json('POST', body)),

  createExtraSolution: (body: {
    questionId: number;
    userName: string;
    password: string;
    contents: string;
  }) => request<void>('/api/extra-solutions', json('POST', body)),

  /** POST인 이유는 ID/비밀번호를 URL 쿼리에 노출하지 않기 위해서다 (SCR-007 비고). */
  extraSolutionStatus: (body: { userName: string; password: string }) =>
    request<ExtraSolutionStatusItem[]>('/api/extra-solutions/status', json('POST', body)),

  /** 드롭다운·상태 라벨은 이 응답으로 그린다. 하드코딩하지 않는다 (DR-C05 · DR-P07). */
  codes: (groupCode: string) => request<Code[]>(`/api/codes/${groupCode}`),

  // ------------------------------------------------------------ 관리자
  admin: {
    login: (body: { loginId: string; password: string }) =>
      request<AdminMe>('/admin/api/login', json('POST', body)),

    logout: () => request<void>('/admin/api/logout', { method: 'POST' }),

    me: () => request<AdminMe>('/admin/api/me'),

    dashboard: () => request<Dashboard>('/admin/api/dashboard'),

    reports: (statusCode?: string) =>
      request<AdminReportItem[]>(
        `/admin/api/reports${statusCode ? `?statusCode=${statusCode}` : ''}`,
      ),

    changeReportStatus: (reportId: number, statusCode: string) =>
      request<AdminReportItem>(`/admin/api/reports/${reportId}`, json('PATCH', { statusCode })),

    extraSolutions: (statusCode?: string) =>
      request<AdminExtraSolutionItem[]>(
        `/admin/api/extra-solutions${statusCode ? `?statusCode=${statusCode}` : ''}`,
      ),

    changeExtraSolutionStatus: (id: number, statusCode: string, rejectReason?: string) =>
      request<AdminExtraSolutionItem>(
        `/admin/api/extra-solutions/${id}`,
        json('PATCH', { statusCode, rejectReason: rejectReason ?? null }),
      ),

    exams: () => request<AdminExam[]>('/admin/api/exams'),

    createExam: (body: { examRound: number; isPublic: boolean }) =>
      request<AdminExam>('/admin/api/exams', json('POST', body)),

    questions: (statusCode?: string) =>
      request<AdminQuestionItem[]>(
        `/admin/api/questions${statusCode ? `?statusCode=${statusCode}` : ''}`,
      ),

    question: (questionId: number) =>
      request<AdminQuestionDetail>(`/admin/api/questions/${questionId}`),

    createQuestion: (body: {
      examId: number;
      sessionNo: number;
      questionNo: number;
      categoryCode: string;
      title: string;
      contents: string;
    }) => request<AdminQuestionDetail>('/admin/api/questions', json('POST', body)),

    updateQuestion: (
      questionId: number,
      body: {
        examId: number;
        sessionNo: number;
        questionNo: number;
        categoryCode: string;
        title: string;
        contents: string;
      },
    ) => request<AdminQuestionDetail>(`/admin/api/questions/${questionId}`, json('PUT', body)),

    /** 게시 시점에 [[drawing:n]] 토큰이 검증된다. 실패하면 400과 사유가 온다 (DR-F03). */
    publishQuestion: (questionId: number) =>
      request<AdminQuestionDetail>(`/admin/api/questions/${questionId}/publish`, {
        method: 'PATCH',
      }),

    unpublishQuestion: (questionId: number) =>
      request<AdminQuestionDetail>(`/admin/api/questions/${questionId}/unpublish`, {
        method: 'PATCH',
      }),

    /** 물리 삭제가 아니라 QST003 상태 전이다 (DR-L01). */
    deleteQuestion: (questionId: number) =>
      request<void>(`/admin/api/questions/${questionId}`, { method: 'DELETE' }),

    saveSolution: (questionId: number, contents: string) =>
      request<AdminQuestionDetail>(
        `/admin/api/questions/${questionId}/solution`,
        json('PUT', { contents }),
      ),

    addQuestionDrawing: (questionId: number, file: File) =>
      upload(`/admin/api/questions/${questionId}/drawings`, file),

    addSolutionDrawing: (questionId: number, file: File) =>
      upload(`/admin/api/questions/${questionId}/solution/drawings`, file),

    deleteDrawing: (drawingId: number) =>
      request<void>(`/admin/api/drawings/${drawingId}`, { method: 'DELETE' }),
  },
};

/** multipart는 Content-Type을 브라우저가 boundary와 함께 붙이므로 직접 넣지 않는다. */
function upload(path: string, file: File): Promise<Drawing> {
  const form = new FormData();
  form.append('file', file);
  return request<Drawing>(path, { method: 'POST', body: form });
}
