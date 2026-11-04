/**
 * Author: HoangTLT
 */
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api, { errorMessage } from '../services/api';
import { money, nights, today } from '../utils/format';

export default function RoomsPage({ session }) {
  const [rooms, setRooms] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [selected, setSelected] = useState([]);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();

  async function load(event) {
    event?.preventDefault(); setLoading(true); setMessage('');
    try {
      const { data } = await api.get('/rooms', { params: { keyword, startDate: startDate || undefined, endDate: endDate || undefined } });
      setRooms(data); setSelected([]);
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setLoading(false); }
  }
  useEffect(() => { load(); }, []);

  function toggle(roomId) {
    setSelected(selected.includes(roomId) ? selected.filter((id) => id !== roomId) : [...selected, roomId]);
  }

  async function book() {
    setMessage('');
    if (!startDate || !endDate || nights(startDate, endDate) <= 0) { setMessage('Chọn ngày nhận và ngày trả hợp lệ trước khi đặt.'); return; }
    setBusy(true);
    try {
      await api.post('/customer/bookings', { rooms: selected.map((roomId) => ({ roomId, startDate, endDate })) });
      navigate('/bookings');
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  const numberOfNights = startDate && endDate ? Math.max(0, nights(startDate, endDate)) : 0;
  const total = rooms.filter((room) => selected.includes(room.roomId)).reduce((sum, room) => sum + room.roomPricePerDay * numberOfNights, 0);

  return <section className="panel"><h1>Danh sách phòng</h1>
    <p>Chọn ngày lưu trú để tìm phòng trống. Có thể đặt nhiều phòng trong cùng một đơn.</p>
    <form className="filters" onSubmit={load}>
      <label>Số phòng<input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Ví dụ: 101" /></label>
      <label>Ngày nhận<input type="date" value={startDate} min={today()} onChange={(e) => { setStartDate(e.target.value); setSelected([]); }} /></label>
      <label>Ngày trả<input type="date" value={endDate} min={startDate || today()} onChange={(e) => { setEndDate(e.target.value); setSelected([]); }} /></label>
      <button disabled={loading}>Tìm phòng</button>
    </form>
    {message && <p className="message" role="alert">{message}</p>}
    {loading ? <p>Đang tải phòng...</p> : !rooms.length ? <p>Không có phòng phù hợp.</p> : <div className="table-wrap"><table>
      <thead><tr>{session?.role === 'CUSTOMER' && <th>Chọn</th>}<th>Phòng</th><th>Loại phòng</th><th>Số người tối đa</th><th>Mô tả</th><th>Giá / đêm</th></tr></thead>
      <tbody>{rooms.map((r) => <tr key={r.roomId}>
        {session?.role === 'CUSTOMER' && <td><input aria-label={`Chọn phòng ${r.roomNumber}`} type="checkbox" checked={selected.includes(r.roomId)} onChange={() => toggle(r.roomId)} /></td>}
        <td>{r.roomNumber}</td><td>{r.roomTypeName}</td><td>{r.roomMaxCapacity}</td><td>{r.roomDetailDescription}</td><td>{money(r.roomPricePerDay)}</td>
      </tr>)}</tbody>
    </table></div>}
    {session?.role === 'CUSTOMER' && <div className="booking-summary"><p>Đã chọn {selected.length} phòng · {numberOfNights} đêm · Tạm tính: <strong>{money(total)}</strong></p>
      <button disabled={busy || !selected.length} onClick={book}>{busy ? 'Đang đặt...' : 'Đặt các phòng đã chọn'}</button>
    </div>}
    {!session && <p><Link to="/login">Đăng nhập</Link> hoặc <Link to="/register">đăng ký</Link> để đặt phòng.</p>}
  </section>;
}
