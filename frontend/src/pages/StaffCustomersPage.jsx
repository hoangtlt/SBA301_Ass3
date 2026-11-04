/**
 * Author: HoangTLT
 */
import { useEffect, useState } from 'react';
import CustomerForm, { emptyCustomer } from '../components/CustomerForm';
import api, { errorMessage } from '../services/api';

export default function StaffCustomersPage() {
  const [customers, setCustomers] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [form, setForm] = useState({ ...emptyCustomer });
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  async function load(event) {
    event?.preventDefault();
    try { const { data } = await api.get('/staff/customers', { params: { keyword } }); setCustomers(data); }
    catch (error) { setMessage(errorMessage(error)); }
  }
  useEffect(() => { load(); }, []);

  function edit(customer) {
    const { customerId, ...fields } = customer;
    setEditingId(customerId); setForm({ ...fields, password: '' }); setShowForm(true); setMessage('');
  }

  async function save(event) {
    event.preventDefault(); setBusy(true); setMessage('');
    try {
      if (editingId) await api.put(`/staff/customers/${editingId}`, form);
      else await api.post('/staff/customers', form);
      setShowForm(false); setMessage('ÄÃ£ lÆ°u khÃ¡ch hÃ ng.'); await load();
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  async function remove(id) {
    if (!window.confirm('XÃ³a khÃ¡ch hÃ ng? KhÃ¡ch Ä‘Ã£ cÃ³ Ä‘Æ¡n Ä‘áº·t phÃ²ng sáº½ chuyá»ƒn sang ngá»«ng hoáº¡t Ä‘á»™ng.')) return;
    setBusy(true); setMessage('');
    try { await api.delete(`/staff/customers/${id}`); setMessage('ÄÃ£ xá»­ lÃ½ xÃ³a khÃ¡ch hÃ ng.'); await load(); }
    catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  return <section className="panel"><h1>Quáº£n lÃ½ khÃ¡ch hÃ ng</h1>
    <form className="filters" onSubmit={load}><label>TÃ¬m tÃªn hoáº·c email<input value={keyword} onChange={(e) => setKeyword(e.target.value)} /></label>
      <button>TÃ¬m kiáº¿m</button><button type="button" className="secondary" onClick={() => { setEditingId(null); setForm({ ...emptyCustomer }); setShowForm(true); }}>ThÃªm khÃ¡ch hÃ ng</button>
    </form>
    {message && <p className="message" role="status">{message}</p>}
    {showForm && <div className="form-box"><h2>{editingId ? 'Sá»­a khÃ¡ch hÃ ng' : 'ThÃªm khÃ¡ch hÃ ng'}</h2>
      <CustomerForm form={form} setForm={setForm} onSubmit={save} editing={!!editingId} staff busy={busy} onCancel={() => setShowForm(false)} />
    </div>}
    <div className="table-wrap"><table><thead><tr><th>MÃ£</th><th>Há» tÃªn</th><th>Email</th><th>Äiá»‡n thoáº¡i</th><th>NgÃ y sinh</th><th>Tráº¡ng thÃ¡i</th><th>Thao tÃ¡c</th></tr></thead>
      <tbody>{customers.map((c) => <tr key={c.customerId}><td>{c.customerId}</td><td>{c.customerFullName}</td><td>{c.emailAddress}</td><td>{c.telephone}</td><td>{c.customerBirthday}</td><td>{c.customerStatus === 1 ? 'Hoáº¡t Ä‘á»™ng' : 'Ngá»«ng hoáº¡t Ä‘á»™ng'}</td>
        <td><div className="actions"><button className="secondary" disabled={busy} onClick={() => edit(c)}>Sá»­a</button><button className="danger" disabled={busy} onClick={() => remove(c.customerId)}>XÃ³a</button></div></td>
      </tr>)}</tbody>
    </table></div>
    {!customers.length && <p>KhÃ´ng cÃ³ khÃ¡ch hÃ ng phÃ¹ há»£p.</p>}
  </section>;
}
