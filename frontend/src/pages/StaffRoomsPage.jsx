/**
 * Author: HoangTLT
 */
import { useEffect, useState } from 'react';
import api, { errorMessage } from '../services/api';
import { money } from '../utils/format';

const emptyRoom = { roomNumber: '', roomDetailDescription: '', roomMaxCapacity: 1, roomTypeId: '', roomStatus: 1, roomPricePerDay: '' };

export default function StaffRoomsPage() {
  const [rooms, setRooms] = useState([]);
  const [types, setTypes] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [form, setForm] = useState({ ...emptyRoom });
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  async function load(event) {
    event?.preventDefault();
    try { const { data } = await api.get('/staff/rooms', { params: { keyword } }); setRooms(data); }
    catch (error) { setMessage(errorMessage(error)); }
  }
  useEffect(() => {
    load();
    api.get('/room-types').then(({ data }) => setTypes(data)).catch((error) => setMessage(errorMessage(error)));
  }, []);

  function change(event) { setForm({ ...form, [event.target.name]: event.target.value }); }

  function edit(room) {
    setEditingId(room.roomId);
    const { roomId, roomTypeName, ...fields } = room;
    setForm(fields); setShowForm(true); setMessage('');
  }

  async function save(event) {
    event.preventDefault(); setBusy(true); setMessage('');
    const body = { ...form, roomMaxCapacity: Number(form.roomMaxCapacity), roomTypeId: Number(form.roomTypeId), roomStatus: Number(form.roomStatus), roomPricePerDay: Number(form.roomPricePerDay) };
    try {
      if (editingId) await api.put(`/staff/rooms/${editingId}`, body);
      else await api.post('/staff/rooms', body);
      setShowForm(false); setMessage('Đã lưu phòng.'); await load();
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  async function remove(id) {
    if (!window.confirm('Xóa phòng? Phòng đã có lịch sử đặt sẽ chuyển sang ngừng hoạt động.')) return;
    setBusy(true); setMessage('');
    try { const { data } = await api.delete(`/staff/rooms/${id}`); setMessage(data.message); await load(); }
    catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  return <section className="panel"><h1>Quản lý phòng</h1>
    <form className="filters" onSubmit={load}><label>Tìm số phòng<input value={keyword} onChange={(e) => setKeyword(e.target.value)} /></label>
      <button>Tìm kiếm</button><button type="button" className="secondary" onClick={() => { setEditingId(null); setForm({ ...emptyRoom }); setShowForm(true); }}>Thêm phòng</button>
    </form>
    {message && <p className="message" role="status">{message}</p>}
    {showForm && <div className="form-box"><h2>{editingId ? 'Sửa phòng' : 'Thêm phòng'}</h2><form className="form-grid" onSubmit={save}>
      <label>Số phòng<input name="roomNumber" value={form.roomNumber} onChange={change} required maxLength={20} /></label>
      <label>Loại phòng<select name="roomTypeId" value={form.roomTypeId} onChange={change} required><option value="">Chọn loại phòng</option>{types.map((t) => <option key={t.roomTypeId} value={t.roomTypeId}>{t.roomTypeName}</option>)}</select></label>
      <label>Số người tối đa<input name="roomMaxCapacity" type="number" value={form.roomMaxCapacity} onChange={change} required min={1} max={100} /></label>
      <label>Giá mỗi đêm (VND)<input name="roomPricePerDay" type="number" value={form.roomPricePerDay} onChange={change} required min="0.01" step="0.01" /></label>
      <label>Trạng thái<select name="roomStatus" value={form.roomStatus} onChange={change}><option value={1}>Hoạt động</option><option value={0}>Ngừng hoạt động</option></select></label>
      <label>Mô tả<textarea name="roomDetailDescription" value={form.roomDetailDescription || ''} onChange={change} maxLength={500} /></label>
      <div className="actions full"><button disabled={busy}>{busy ? 'Đang lưu...' : 'Lưu phòng'}</button><button type="button" className="secondary" onClick={() => setShowForm(false)}>Đóng</button></div>
    </form></div>}
    <div className="table-wrap"><table><thead><tr><th>Phòng</th><th>Loại</th><th>Số người</th><th>Mô tả</th><th>Giá / đêm</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
      <tbody>{rooms.map((r) => <tr key={r.roomId}><td>{r.roomNumber}</td><td>{r.roomTypeName}</td><td>{r.roomMaxCapacity}</td><td>{r.roomDetailDescription}</td><td>{money(r.roomPricePerDay)}</td><td>{r.roomStatus === 1 ? 'Hoạt động' : 'Ngừng hoạt động'}</td>
        <td><div className="actions"><button className="secondary" disabled={busy} onClick={() => edit(r)}>Sửa</button><button className="danger" disabled={busy} onClick={() => remove(r.roomId)}>Xóa</button></div></td>
      </tr>)}</tbody>
    </table></div>
    {!rooms.length && <p>Không có phòng phù hợp.</p>}
  </section>;
}
