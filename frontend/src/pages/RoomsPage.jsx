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
    if (!startDate || !endDate || nights(startDate, endDate) <= 0) { setMessage('Chá»n ngÃ y nháº­n vÃ  ngÃ y tráº£ há»£p lá»‡ trÆ°á»›c khi Ä‘áº·t.'); return; }
    setBusy(true);
    try {
      await api.post('/customer/bookings', { rooms: selected.map((roomId) => ({ roomId, startDate, endDate })) });
      navigate('/bookings');
    } catch (error) { setMessage(errorMessage(error)); }
    finally { setBusy(false); }
  }

  const numberOfNights = startDate && endDate ? Math.max(0, nights(startDate, endDate)) : 0;
  const total = rooms.filter((room) => selected.includes(room.roomId)).reduce((sum, room) => sum + room.roomPricePerDay * numberOfNights, 0);

  return <section className="panel"><h1>Danh sÃ¡ch phÃ²ng</h1>
    <p>Chá»n ngÃ y lÆ°u trÃº Ä‘á»ƒ tÃ¬m phÃ²ng trá»‘ng. CÃ³ thá»ƒ Ä‘áº·t nhiá»u phÃ²ng trong cÃ¹ng má»™t Ä‘Æ¡n.</p>
    <form className="filters" onSubmit={load}>
      <label>Sá»‘ phÃ²ng<input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="VÃ­ dá»¥: 101" /></label>
      <label>NgÃ y nháº­n<input type="date" value={startDate} min={today()} onChange={(e) => { setStartDate(e.target.value); setSelected([]); }} /></label>
      <label>NgÃ y tráº£<input type="date" value={endDate} min={startDate || today()} onChange={(e) => { setEndDate(e.target.value); setSelected([]); }} /></label>
      <button disabled={loading}>TÃ¬m phÃ²ng</button>
    </form>
    {message && <p className="message" role="alert">{message}</p>}
    {loading ? <p>Äang táº£i phÃ²ng...</p> : !rooms.length ? <p>KhÃ´ng cÃ³ phÃ²ng phÃ¹ há»£p.</p> : <div className="table-wrap"><table>
      <thead><tr>{session?.role === 'CUSTOMER' && <th>Chá»n</th>}<th>PhÃ²ng</th><th>Loáº¡i phÃ²ng</th><th>Sá»‘ ngÆ°á»i tá»‘i Ä‘a</th><th>MÃ´ táº£</th><th>GiÃ¡ / Ä‘Ãªm</th></tr></thead>
      <tbody>{rooms.map((r) => <tr key={r.roomId}>
        {session?.role === 'CUSTOMER' && <td><input aria-label={`Chá»n phÃ²ng ${r.roomNumber}`} type="checkbox" checked={selected.includes(r.roomId)} onChange={() => toggle(r.roomId)} /></td>}
        <td>{r.roomNumber}</td><td>{r.roomTypeName}</td><td>{r.roomMaxCapacity}</td><td>{r.roomDetailDescription}</td><td>{money(r.roomPricePerDay)}</td>
      </tr>)}</tbody>
    </table></div>}
    {session?.role === 'CUSTOMER' && <div className="booking-summary"><p>ÄÃ£ chá»n {selected.length} phÃ²ng Â· {numberOfNights} Ä‘Ãªm Â· Táº¡m tÃ­nh: <strong>{money(total)}</strong></p>
      <button disabled={busy || !selected.length} onClick={book}>{busy ? 'Äang Ä‘áº·t...' : 'Äáº·t cÃ¡c phÃ²ng Ä‘Ã£ chá»n'}</button>
    </div>}
    {!session && <p><Link to="/login">ÄÄƒng nháº­p</Link> hoáº·c <Link to="/register">Ä‘Äƒng kÃ½</Link> Ä‘á»ƒ Ä‘áº·t phÃ²ng.</p>}
  </section>;
}
