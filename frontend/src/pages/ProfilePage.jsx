/**
 * Author: HoangTLT
 */
import { useEffect, useState } from 'react';
import CustomerForm from '../components/CustomerForm';
import api, { errorMessage } from '../services/api';

export default function ProfilePage() {
  const [form, setForm] = useState(null);
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    api.get('/customer/profile').then(({ data }) => {
      const { customerId, ...fields } = data;
      setForm({ ...fields, password: '' });
    }).catch((error) => setMessage(errorMessage(error)));
  }, []);

  async function submit(event) {
    event.preventDefault(); setBusy(true); setMessage('');
    try {
      await api.put('/customer/profile', form);
      setForm({ ...form, password: '' }); setMessage('ÄÃ£ cáº­p nháº­t há»“ sÆ¡.');
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  return <section className="panel"><h1>Há»“ sÆ¡ cá»§a tÃ´i</h1>
    {message && <p className="message" role="status">{message}</p>}
    {form ? <CustomerForm form={form} setForm={setForm} onSubmit={submit} editing busy={busy} /> : <p>Äang táº£i há»“ sÆ¡...</p>}
  </section>;
}
