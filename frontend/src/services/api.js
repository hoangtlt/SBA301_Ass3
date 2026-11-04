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
  if (!error.response) return 'KhÃ´ng káº¿t ná»‘i Ä‘Æ°á»£c mÃ¡y chá»§. HÃ£y kiá»ƒm tra backend táº¡i cá»•ng 8080.';
  if (error.response.status === 401) return error.response.data?.message || 'PhiÃªn Ä‘Äƒng nháº­p háº¿t háº¡n. Vui lÃ²ng Ä‘Äƒng nháº­p láº¡i.';
  if (error.response.status === 403) return 'Báº¡n khÃ´ng cÃ³ quyá»n thá»±c hiá»‡n thao tÃ¡c nÃ y.';
  const fields = error.response.data?.errors;
  return fields ? Object.entries(fields).map(([key, value]) => `${key}: ${value}`).join('; ')
    : error.response.data?.message || 'Thao tÃ¡c tháº¥t báº¡i. Vui lÃ²ng thá»­ láº¡i.';
}

export default api;
