import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/**
 * 개발 서버는 API를 WAS로 프록시한다.
 *
 * 왜 CORS 설정을 두지 않는가: 운영에서는 Nginx가 web과 was를 같은 오리진으로 묶는다
 * (deploy/nginx.conf). 개발에서만 오리진이 갈리므로 프록시로 맞춰 두면
 * 서버에 개발 전용 CORS 예외를 남기지 않아도 된다.
 *
 * 관리자 세션은 쿠키 기반이라(R-55) 프록시가 같은 오리진을 유지해야 한다.
 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: false },
      '/admin/api': { target: 'http://localhost:8080', changeOrigin: false },
    },
  },
});
