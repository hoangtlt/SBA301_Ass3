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
    if (!selected.length) { setMessage('Chọn ít nhất một phòng.'); return; }
    setBusy(true); setMessage('');
    try {
      const body = { customerId: Number(customerId), rooms: selected.map((roomId) => ({ roomId, startDate, endDate })) };
      if (editingId) await api.put(`/staff/bookings/${editingId}`, body);
      else await api.post('/staff/bookings', body);
      setSelected([]); setShowForm(false); setMessage('Đã lưu đơn đặt phòng.'); await load();
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  function edit(booking) {
    setEditingId(booking.bookingReservationId); setCustomerId(String(booking.customerId));
    setStartDate(booking.bookingDetails[0].startDate); setEndDate(booking.bookingDetails[0].endDate);
    setSelected(booking.bookingDetails.map((d) => d.roomId)); setShowForm(true);
    setMessage('Ngày nhận và ngày trả trong form sẽ áp dụng cho tất cả phòng đã chọn.');
  }

  async function changeStatus(id, nextStatus) {
    if (!window.confirm(`Chuyển đơn #${id} sang "${bookingStatus[nextStatus]}"?`)) return;
    setBusy(true); setMessage('');
    try { await api.patch(`/staff/bookings/${id}/status`, { bookingStatus: nextStatus }); setMessage('Đã cập nhật trạng thái.'); await load(); }
    catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  async function remove(id) {
    if (!window.confirm(`Xóa đơn đã hủy #${id}?`)) return;
    setBusy(true); setMessage('');
    try { await api.delete(`/staff/bookings/${id}`); setMessage('Đã xóa đơn.'); await load(); }
    catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  const visibleBookings = bookings.filter((b) => (!status || b.bookingStatus === status)
    && (`${b.bookingReservationId} ${b.customerFullName}`.toLowerCase().includes(keyword.toLowerCase())));

  return <section className="panel"><h1>Quản lý đơn đặt phòng</h1>
    <div className="filters"><label>Tìm mã đơn hoặc tên khách<input value={keyword} onChange={(e) => setKeyword(e.target.value)} /></label>
      <label>Trạng thái<select value={status} onChange={(e) => setStatus(e.target.value)}><option value="">Tất cả</option>{Object.entries(bookingStatus).map(([key, label]) => <option key={key} value={key}>{label}</option>)}</select></label>
      <button onClick={() => { setEditingId(null); setCustomerId(''); setSelected([]); setStartDate(''); setEndDate(''); setShowForm(true); }}>Tạo đơn tại quầy</button>
    </div>
    {message && <p className="message" role="status">{message}</p>}
    {showForm && <div className="form-box"><h2>{editingId ? `Sửa đơn #${editingId}` : 'Tạo đơn tại quầy'}</h2><form className="form-grid" onSubmit={create}>
      <label>Khách hàng<select value={customerId} onChange={(e) => setCustomerId(e.target.value)} required><option value="">Chọn khách hàng</option>{customers.filter((c) => c.customerStatus === 1).map((c) => <option key={c.customerId} value={c.customerId}>{c.customerFullName} ({c.emailAddress})</option>)}</select></label>
      <label>Ngày nhận<input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} min={today()} required /></label>
      <label>Ngày trả<input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} min={startDate || today()} required /></label>
      <fieldset className="full"><legend>Chọn một hoặc nhiều phòng</legend>{rooms.map((r) => <label className="checkbox-label" key={r.roomId}>
        <input type="checkbox" checked={selected.includes(r.roomId)} onChange={() => setSelected(selected.includes(r.roomId) ? selected.filter((id) => id !== r.roomId) : [...selected, r.roomId])} /> Phòng {r.roomNumber} ({r.roomTypeName})
      </label>)}<p className="hint">Hệ thống kiểm tra trùng lịch khi lưu đơn.</p></fieldset>
      <div className="actions full"><button disabled={busy}>{busy ? 'Đang tạo...' : 'Lưu đơn'}</button><button type="button" className="secondary" onClick={() => setShowForm(false)}>Đóng</button></div>
    </form></div>}
    <BookingTable bookings={visibleBookings} staff onStatus={changeStatus} onDelete={remove} onEdit={edit} busy={busy} />
  </section>;
}
