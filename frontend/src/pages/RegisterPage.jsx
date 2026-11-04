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
      navigate('/login', { state: { message: 'ÄÄƒng kÃ½ thÃ nh cÃ´ng. Báº¡n cÃ³ thá»ƒ Ä‘Äƒng nháº­p.' } });
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  return <section className="panel"><h1>ÄÄƒng kÃ½ tÃ i khoáº£n</h1>
    {message && <p className="message" role="alert">{message}</p>}
    <CustomerForm form={form} setForm={setForm} onSubmit={submit} busy={busy} />
  </section>;
}
