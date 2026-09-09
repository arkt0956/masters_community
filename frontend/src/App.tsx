import { BrowserRouter, Route, Routes } from 'react-router-dom';
import AdminLayout from './components/layout/AdminLayout';
import AppLayout from './components/layout/AppLayout';
import AboutPage from './pages/AboutPage';
import CategorySelectPage from './pages/CategorySelectPage';
import ExamSelectPage from './pages/ExamSelectPage';
import ExtraSolutionNewPage from './pages/ExtraSolutionNewPage';
import ExtraSolutionStatusPage from './pages/ExtraSolutionStatusPage';
import MainPage from './pages/MainPage';
import QuestionListPage from './pages/QuestionListPage';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminExtraSolutionPage from './pages/admin/AdminExtraSolutionPage';
import AdminLoginPage from './pages/admin/AdminLoginPage';
import AdminQuestionPage from './pages/admin/AdminQuestionPage';
import AdminReportPage from './pages/admin/AdminReportPage';

/**
 * 라우트 (화면정의서 2절 화면 목록).
 *
 * 모든 사용자 라우트는 AppLayout 아래에 둔다. 레이아웃 밖에 두면 공통 푸터가 사라지고,
 * 그 화면은 출처표시 없이 원문을 게재하는 셈이 된다 (DR-X01 · R-56).
 * 새 화면을 추가하는 PR은 리뷰에서 이걸 확인한다.
 *
 * 관리자 라우트는 AdminLayout 아래다. 푸터는 두지 않는다 — DR-X01이 요구하는 것은
 * 사용자 화면이다.
 */
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<MainPage />} />                                {/* SCR-001 */}
          <Route path="/exams" element={<ExamSelectPage />} />                     {/* SCR-002 */}
          <Route path="/categories" element={<CategorySelectPage />} />            {/* SCR-003 */}
          <Route path="/questions" element={<QuestionListPage />} />               {/* SCR-004 */}
          <Route path="/extra-solutions/new" element={<ExtraSolutionNewPage />} /> {/* SCR-006 */}
          <Route
            path="/extra-solutions/status"
            element={<ExtraSolutionStatusPage />}
          />                                                                        {/* SCR-007 */}
          <Route path="/about" element={<AboutPage />} />                          {/* SCR-008 */}
        </Route>

        {/* 숨김 경로. 실제 방어선은 서버 인증이다 (R-49) */}
        <Route path="/admin/login" element={<AdminLoginPage />} />                 {/* SCR-A05 */}

        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboardPage />} />                         {/* SCR-A01 */}
          <Route path="reports" element={<AdminReportPage />} />                   {/* SCR-A02 */}
          <Route path="extra-solutions" element={<AdminExtraSolutionPage />} />    {/* SCR-A03 */}
          <Route path="questions" element={<AdminQuestionPage />} />               {/* SCR-A04 */}
        </Route>

        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}

function NotFound() {
  return (
    <div className="land">
      <div className="empty" style={{ marginTop: 80 }}>
        요청하신 화면을 찾을 수 없습니다.
      </div>
    </div>
  );
}
