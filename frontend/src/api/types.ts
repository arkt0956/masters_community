/**
 * WAS 응답 타입.
 *
 * 백엔드 DTO와 1:1로 맞춘다. 목록 타입(QuestionListItem)에 해설 필드를 추가하지 않는다.
 * 서버가 보내지 않는 값이고, 타입에 있으면 언젠가 누군가 채우려 든다 (DR-A01).
 */

export interface Drawing {
  drawingId: number;
  drawingNo: number;
  fileUuid: string;
  fileName: string;
  fileType: string;
}

export interface SessionSummary {
  sessionNo: number;
  questionCount: number;
}

export interface Exam {
  examId: number;
  examRound: number;
  sessions: SessionSummary[];
}

export interface Category {
  codeValue: string;
  codeName: string;
  questionCount: number;
  children: Category[];
}

/** 목록 응답. 해설 본문은 없고 보유 여부(hasSolution)만 있다. */
export interface QuestionListItem {
  questionId: number;
  examRound: number;
  sessionNo: number;
  questionNo: number;
  categoryCode: string;
  categoryName: string;
  title: string;
  contents: string;
  drawings: Drawing[];
  hasSolution: boolean;
}

/** 해설. 별도 API로만 받는다 (R-09). */
export interface Solution {
  solutionId: number;
  contents: string;
  drawings: Drawing[];
}

export interface ExtraSolutionPublic {
  extraSolutionId: number;
  userName: string;
  contents: string;
  createdAt: string;
  drawings: Drawing[];
}

export interface ExtraSolutionStatusItem {
  extraSolutionId: number;
  questionId: number;
  questionTitle: string;
  sourceLabel: string;
  createdAt: string;
  statusCode: string;
  statusName: string;
  rejectReason: string | null;
}

/** 공통코드. 화면은 codeName을 그대로 쓴다. 코드값을 라벨로 바꾸지 않는다 (DR-P07). */
export interface Code {
  codeValue: string;
  codeName: string;
  codeLevel: number;
  parentCode: string | null;
  sortOrder: number;
}

// ------------------------------------------------------------------ 관리자

export interface AdminMe {
  loginId: string;
  lastLoginAt: string | null;
}

export interface Dashboard {
  reportPending: number;
  extraSolutionPending: number;
  questionDraft: number;
}

export interface AdminReportItem {
  reportId: number;
  questionId: number;
  questionTitle: string;
  sourceLabel: string;
  typeCode: string;
  typeName: string;
  contents: string;
  statusCode: string;
  statusName: string;
  createdAt: string;
  processedAt: string | null;
}

export interface AdminExtraSolutionItem {
  extraSolutionId: number;
  questionId: number;
  questionTitle: string;
  sourceLabel: string;
  userName: string;
  contents: string;
  statusCode: string;
  statusName: string;
  rejectReason: string | null;
  createdAt: string;
  processedAt: string | null;
}

export interface AdminQuestionItem {
  questionId: number;
  examId: number;
  examRound: number;
  sessionNo: number;
  questionNo: number;
  categoryCode: string;
  categoryName: string;
  title: string;
  statusCode: string;
  statusName: string;
  hasSolution: boolean;
}

export interface AdminQuestionDetail extends Omit<AdminQuestionItem, 'hasSolution'> {
  contents: string;
  drawings: Drawing[];
  solution: { solutionId: number; contents: string; drawings: Drawing[] } | null;
}

export interface AdminExam {
  examId: number;
  examRound: number;
  isPublic: boolean;
}
