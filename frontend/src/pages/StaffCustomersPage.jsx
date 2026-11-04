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
      setShowForm(false); setMessage('Đã lưu khách hàng.'); await load();
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  async function remove(id) {
    if (!window.confirm('Xóa khách hàng? Khách đã có đơn đặt phòng sẽ chuyển sang ngừng hoạt động.')) return;
    setBusy(true); setMessage('');
    try { await api.delete(`/staff/customers/${id}`); setMessage('Đã xử lý xóa khách hàng.'); await load(); }
    catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  return <section className="panel"><h1>Quản lý khách hàng</h1>
    <form className="filters" onSubmit={load}><label>Tìm tên hoặc email<input value={keyword} onChange={(e) => setKeyword(e.target.value)} /></label>
      <button>Tìm kiếm</button><button type="button" className="secondary" onClick={() => { setEditingId(null); setForm({ ...emptyCustomer }); setShowForm(true); }}>Thêm khách hàng</button>
    </form>
    {message && <p className="message" role="status">{message}</p>}
    {showForm && <div className="form-box"><h2>{editingId ? 'Sửa khách hàng' : 'Thêm khách hàng'}</h2>
      <CustomerForm form={form} setForm={setForm} onSubmit={save} editing={!!editingId} staff busy={busy} onCancel={() => setShowForm(false)} />
    </div>}
    <div className="table-wrap"><table><thead><tr><th>Mã</th><th>Họ tên</th><th>Email</th><th>Điện thoại</th><th>Ngày sinh</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
      <tbody>{customers.map((c) => <tr key={c.customerId}><td>{c.customerId}</td><td>{c.customerFullName}</td><td>{c.emailAddress}</td><td>{c.telephone}</td><td>{c.customerBirthday}</td><td>{c.customerStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'}</td>
        <td><div className="actions"><button className="secondary" disabled={busy} onClick={() => edit(c)}>Sửa</button><button className="danger" disabled={busy} onClick={() => remove(c.customerId)}>Xóa</button></div></td>
      </tr>)}</tbody>
    </table></div>
    {!customers.length && <p>Không có khách hàng phù hợp.</p>}
  </section>;
}
