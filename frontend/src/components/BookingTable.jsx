/**
 * Author: HoangTLT
 */
import { bookingStatus, money } from '../utils/format';

export default function BookingTable({ bookings, staff = false, onStatus, onDelete, onEdit, busy = false }) {
  if (!bookings.length) return <p>ChÆ°a cÃ³ Ä‘Æ¡n Ä‘áº·t phÃ²ng.</p>;
  return <div className="table-wrap"><table>
    <thead><tr><th>MÃ£ Ä‘Æ¡n</th>{staff && <th>KhÃ¡ch hÃ ng</th>}<th>NgÃ y Ä‘áº·t</th><th>Chi tiáº¿t phÃ²ng</th><th>Tá»•ng tiá»n</th><th>Tráº¡ng thÃ¡i</th>{staff && <th>Thao tÃ¡c</th>}</tr></thead>
    <tbody>{bookings.map((b) => <tr key={b.bookingReservationId}>
      <td>#{b.bookingReservationId}</td>{staff && <td>{b.customerFullName}</td>}<td>{b.bookingDate}</td>
      <td>{b.bookingDetails.map((d) => <div key={d.roomId} className="booking-detail">
        <strong>PhÃ²ng {d.roomNumber}</strong><br />{d.startDate} â†’ {d.endDate}<br />{money(d.actualPrice)}/Ä‘Ãªm
      </div>)}</td>
      <td>{money(b.totalPrice)}</td><td>{bookingStatus[b.bookingStatus]}</td>
      {staff && <td><div className="actions">
        {b.bookingStatus === 'CONFIRMED' && <>
          <button className="secondary" disabled={busy} onClick={() => onEdit(b)}>Sá»­a phÃ²ng / ngÃ y</button>
          <button disabled={busy} onClick={() => onStatus(b.bookingReservationId, 'COMPLETED')}>HoÃ n táº¥t</button>
          <button className="danger" disabled={busy} onClick={() => onStatus(b.bookingReservationId, 'CANCELLED')}>Há»§y Ä‘Æ¡n</button>
        </>}
        {b.bookingStatus === 'CANCELLED' && <button className="danger" disabled={busy} onClick={() => onDelete(b.bookingReservationId)}>XÃ³a</button>}
      </div></td>}
    </tr>)}</tbody>
  </table></div>;
}
