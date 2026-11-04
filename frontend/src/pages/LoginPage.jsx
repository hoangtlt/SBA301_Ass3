/**
 * Author: HoangTLT
 */
import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import api, { errorMessage, saveSession } from '../services/api';

export default function LoginPage({ onLogin }) {
  const [form, setForm] = useState({ email: '', password: '' });
  const location = useLocation();
  const [message, setMessage] = useState(location.state?.message || '');
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();

  async function submit(event) {
    event.preventDefault(); setBusy(true); setMessage('');
    try {
      const { data } = await api.post('/auth/login', form);
      saveSession(data); onLogin(data);
      navigate(data.role === 'STAFF' ? '/staff/bookings' : '/');
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  return <section className="panel narrow">
    <h1>ÄÄƒng nháº­p</h1><p>DÃ¹ng email vÃ  máº­t kháº©u Ä‘á»ƒ vÃ o tÃ i khoáº£n.</p>
    {message && <p className="message" role="alert">{message}</p>}
    <form onSubmit={submit}>
      <label>Email<input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required maxLength={100} autoComplete="username" /></label>
      <label>Máº­t kháº©u<input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required maxLength={72} autoComplete="current-password" /></label>
      <button disabled={busy}>{busy ? 'Äang Ä‘Äƒng nháº­p...' : 'ÄÄƒng nháº­p'}</button>
    </form>
    <p>ChÆ°a cÃ³ tÃ i khoáº£n? <Link to="/register">ÄÄƒng kÃ½</Link></p>
  </section>;
}
