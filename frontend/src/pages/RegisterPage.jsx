/**
 * Author: HoangTLT
 */
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import CustomerForm, { emptyCustomer } from '../components/CustomerForm';
import api, { errorMessage } from '../services/api';

export default function RegisterPage() {
  const [form, setForm] = useState({ ...emptyCustomer });
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();

  async function submit(event) {
    event.preventDefault(); setBusy(true); setMessage('');
    try {
      await api.post('/auth/register', form);
      navigate('/login', { state: { message: 'Đăng ký thành công. Bạn có thể đăng nhập.' } });
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  return <section className="panel"><h1>Đăng ký tài khoản</h1>
    {message && <p className="message" role="alert">{message}</p>}
    <CustomerForm form={form} setForm={setForm} onSubmit={submit} busy={busy} />
  </section>;
}
