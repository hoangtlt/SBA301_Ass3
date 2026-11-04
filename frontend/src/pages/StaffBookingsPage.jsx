/**
 * Author: HoangTLT
 */
import { useEffect, useState } from 'react';
import BookingTable from '../components/BookingTable';
import api, { errorMessage } from '../services/api';
import { bookingStatus, today } from '../utils/format';

export default function StaffBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [customerId, setCustomerId] = useState('');
  const [selected, setSelected] = useState([]);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      const [bookingResult, customerResult, roomResult] = await Promise.all([
        api.get('/staff/bookings'), api.get('/staff/customers'), api.get('/rooms'),
      ]);
      setBookings(bookingResult.data); setCustomers(customerResult.data); setRooms(roomResult.data);
    } catch (error) { setMessage(errorMessage(error)); }
  }
  useEffect(() => { load(); }, []);

  async function create(event) {
    event.preventDefault();
    if (!selected.length) { setMessage('Chá»n Ã­t nháº¥t má»™t phÃ²ng.'); return; }
    setBusy(true); setMessage('');
    try {
      const body = { customerId: Number(customerId), rooms: selected.map((roomId) => ({ roomId, startDate, endDate })) };
      if (editingId) await api.put(`/staff/bookings/${editingId}`, body);
      else await api.post('/staff/bookings', body);
      setSelected([]); setShowForm(false); setMessage('ÄÃ£ lÆ°u Ä‘Æ¡n Ä‘áº·t phÃ²ng.'); await load();
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  function edit(booking) {
    setEditingId(booking.bookingReservationId); setCustomerId(String(booking.customerId));
    setStartDate(booking.bookingDetails[0].startDate); setEndDate(booking.bookingDetails[0].endDate);
    setSelected(booking.bookingDetails.map((d) => d.roomId)); setShowForm(true);
    setMessage('NgÃ y nháº­n vÃ  ngÃ y tráº£ trong form sáº½ Ã¡p dá»¥ng cho táº¥t cáº£ phÃ²ng Ä‘Ã£ chá»n.');
  }

  async function changeStatus(id, nextStatus) {
    if (!window.confirm(`Chuyá»ƒn Ä‘Æ¡n #${id} sang "${bookingStatus[nextStatus]}"?`)) return;
    setBusy(true); setMessage('');
    try { await api.patch(`/staff/bookings/${id}/status`, { bookingStatus: nextStatus }); setMessage('ÄÃ£ cáº­p nháº­t tráº¡ng thÃ¡i.'); await load(); }
    catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  async function remove(id) {
    if (!window.confirm(`XÃ³a Ä‘Æ¡n Ä‘Ã£ há»§y #${id}?`)) return;
    setBusy(true); setMessage('');
    try { await api.delete(`/staff/bookings/${id}`); setMessage('ÄÃ£ xÃ³a Ä‘Æ¡n.'); await load(); }
    catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  const visibleBookings = bookings.filter((b) => (!status || b.bookingStatus === status)
    && (`${b.bookingReservationId} ${b.customerFullName}`.toLowerCase().includes(keyword.toLowerCase())));

  return <section className="panel"><h1>Quáº£n lÃ½ Ä‘Æ¡n Ä‘áº·t phÃ²ng</h1>
    <div className="filters"><label>TÃ¬m mÃ£ Ä‘Æ¡n hoáº·c tÃªn khÃ¡ch<input value={keyword} onChange={(e) => setKeyword(e.target.value)} /></label>
      <label>Tráº¡ng thÃ¡i<select value={status} onChange={(e) => setStatus(e.target.value)}><option value="">Táº¥t cáº£</option>{Object.entries(bookingStatus).map(([key, label]) => <option key={key} value={key}>{label}</option>)}</select></label>
      <button onClick={() => { setEditingId(null); setCustomerId(''); setSelected([]); setStartDate(''); setEndDate(''); setShowForm(true); }}>Táº¡o Ä‘Æ¡n táº¡i quáº§y</button>
    </div>
    {message && <p className="message" role="status">{message}</p>}
    {showForm && <div className="form-box"><h2>{editingId ? `Sá»­a Ä‘Æ¡n #${editingId}` : 'Táº¡o Ä‘Æ¡n táº¡i quáº§y'}</h2><form className="form-grid" onSubmit={create}>
      <label>KhÃ¡ch hÃ ng<select value={customerId} onChange={(e) => setCustomerId(e.target.value)} required><option value="">Chá»n khÃ¡ch hÃ ng</option>{customers.filter((c) => c.customerStatus === 1).map((c) => <option key={c.customerId} value={c.customerId}>{c.customerFullName} ({c.emailAddress})</option>)}</select></label>
      <label>NgÃ y nháº­n<input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} min={today()} required /></label>
      <label>NgÃ y tráº£<input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} min={startDate || today()} required /></label>
      <fieldset className="full"><legend>Chá»n má»™t hoáº·c nhiá»u phÃ²ng</legend>{rooms.map((r) => <label className="checkbox-label" key={r.roomId}>
        <input type="checkbox" checked={selected.includes(r.roomId)} onChange={() => setSelected(selected.includes(r.roomId) ? selected.filter((id) => id !== r.roomId) : [...selected, r.roomId])} /> PhÃ²ng {r.roomNumber} ({r.roomTypeName})
      </label>)}<p className="hint">Há»‡ thá»‘ng kiá»ƒm tra trÃ¹ng lá»‹ch khi lÆ°u Ä‘Æ¡n.</p></fieldset>
      <div className="actions full"><button disabled={busy}>{busy ? 'Äang táº¡o...' : 'LÆ°u Ä‘Æ¡n'}</button><button type="button" className="secondary" onClick={() => setShowForm(false)}>ÄÃ³ng</button></div>
    </form></div>}
    <BookingTable bookings={visibleBookings} staff onStatus={changeStatus} onDelete={remove} onEdit={edit} busy={busy} />
  </section>;
}
