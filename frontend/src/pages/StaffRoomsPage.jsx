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
      setShowForm(false); setMessage('ÄÃ£ lÆ°u phÃ²ng.'); await load();
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  async function remove(id) {
    if (!window.confirm('XÃ³a phÃ²ng? PhÃ²ng Ä‘Ã£ cÃ³ lá»‹ch sá»­ Ä‘áº·t sáº½ chuyá»ƒn sang ngá»«ng hoáº¡t Ä‘á»™ng.')) return;
    setBusy(true); setMessage('');
    try { const { data } = await api.delete(`/staff/rooms/${id}`); setMessage(data.message); await load(); }
    catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  return <section className="panel"><h1>Quáº£n lÃ½ phÃ²ng</h1>
    <form className="filters" onSubmit={load}><label>TÃ¬m sá»‘ phÃ²ng<input value={keyword} onChange={(e) => setKeyword(e.target.value)} /></label>
      <button>TÃ¬m kiáº¿m</button><button type="button" className="secondary" onClick={() => { setEditingId(null); setForm({ ...emptyRoom }); setShowForm(true); }}>ThÃªm phÃ²ng</button>
    </form>
    {message && <p className="message" role="status">{message}</p>}
    {showForm && <div className="form-box"><h2>{editingId ? 'Sá»­a phÃ²ng' : 'ThÃªm phÃ²ng'}</h2><form className="form-grid" onSubmit={save}>
      <label>Sá»‘ phÃ²ng<input name="roomNumber" value={form.roomNumber} onChange={change} required maxLength={20} /></label>
      <label>Loáº¡i phÃ²ng<select name="roomTypeId" value={form.roomTypeId} onChange={change} required><option value="">Chá»n loáº¡i phÃ²ng</option>{types.map((t) => <option key={t.roomTypeId} value={t.roomTypeId}>{t.roomTypeName}</option>)}</select></label>
      <label>Sá»‘ ngÆ°á»i tá»‘i Ä‘a<input name="roomMaxCapacity" type="number" value={form.roomMaxCapacity} onChange={change} required min={1} max={100} /></label>
      <label>GiÃ¡ má»—i Ä‘Ãªm (VND)<input name="roomPricePerDay" type="number" value={form.roomPricePerDay} onChange={change} required min="0.01" step="0.01" /></label>
      <label>Tráº¡ng thÃ¡i<select name="roomStatus" value={form.roomStatus} onChange={change}><option value={1}>Hoáº¡t Ä‘á»™ng</option><option value={0}>Ngá»«ng hoáº¡t Ä‘á»™ng</option></select></label>
      <label>MÃ´ táº£<textarea name="roomDetailDescription" value={form.roomDetailDescription || ''} onChange={change} maxLength={500} /></label>
      <div className="actions full"><button disabled={busy}>{busy ? 'Äang lÆ°u...' : 'LÆ°u phÃ²ng'}</button><button type="button" className="secondary" onClick={() => setShowForm(false)}>ÄÃ³ng</button></div>
    </form></div>}
    <div className="table-wrap"><table><thead><tr><th>PhÃ²ng</th><th>Loáº¡i</th><th>Sá»‘ ngÆ°á»i</th><th>MÃ´ táº£</th><th>GiÃ¡ / Ä‘Ãªm</th><th>Tráº¡ng thÃ¡i</th><th>Thao tÃ¡c</th></tr></thead>
      <tbody>{rooms.map((r) => <tr key={r.roomId}><td>{r.roomNumber}</td><td>{r.roomTypeName}</td><td>{r.roomMaxCapacity}</td><td>{r.roomDetailDescription}</td><td>{money(r.roomPricePerDay)}</td><td>{r.roomStatus === 1 ? 'Hoáº¡t Ä‘á»™ng' : 'Ngá»«ng hoáº¡t Ä‘á»™ng'}</td>
        <td><div className="actions"><button className="secondary" disabled={busy} onClick={() => edit(r)}>Sá»­a</button><button className="danger" disabled={busy} onClick={() => remove(r.roomId)}>XÃ³a</button></div></td>
      </tr>)}</tbody>
    </table></div>
    {!rooms.length && <p>KhÃ´ng cÃ³ phÃ²ng phÃ¹ há»£p.</p>}
  </section>;
}
