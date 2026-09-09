import { Outlet } from 'react-router-dom';
import AppFooter from './AppFooter';

/**
 * 사용자 공통 레이아웃 (DR-X01).
 *
 * 왜 레이아웃에 푸터를 두는가:
 * 모든 사용자 라우트는 이 레이아웃 아래에 둔다. 레이아웃 밖에 라우트를 두면
 * 푸터가 사라진다. 새 화면을 추가하는 PR은 리뷰에서 이걸 확인한다.
 */
export default function AppLayout() {
  return (
    <>
      <Outlet />
      <div className="land">
        <AppFooter />
      </div>
    </>
  );
}
