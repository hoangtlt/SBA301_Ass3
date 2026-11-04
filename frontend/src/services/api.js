/**
 * Author: HoangTLT
 */
import axios from 'axios';

// All HTTP requests go through this axios instance.
const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || '/api' });

export function getSession() {
  try { return JSON.parse(sessionStorage.getItem('session')); }
  catch { return null; }
}

export function saveSession(session) {
  sessionStorage.setItem('session', JSON.stringify(session));
}

export function clearSession() { sessionStorage.removeItem('session'); }

api.interceptors.request.use((config) => {
  const session = getSession();
  if (session?.token) config.headers.Authorization = `Bearer ${session.token}`;
  return config;
});

api.interceptors.response.use((response) => response, (error) => {
  if (error.response?.status === 401 && !error.config?.url?.startsWith('/auth/')) {
    clearSession();
    window.dispatchEvent(new Event('session-expired'));
  }
  return Promise.reject(error);
});

export function errorMessage(error) {
  if (!error.response) return 'Không kết nối được máy chủ. Hãy kiểm tra backend tại cổng 8080.';
  if (error.response.status === 401) return error.response.data?.message || 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.';
  if (error.response.status === 403) return 'Bạn không có quyền thực hiện thao tác này.';
  const fields = error.response.data?.errors;
  return fields ? Object.entries(fields).map(([key, value]) => `${key}: ${value}`).join('; ')
    : error.response.data?.message || 'Thao tác thất bại. Vui lòng thử lại.';
}

export default api;
