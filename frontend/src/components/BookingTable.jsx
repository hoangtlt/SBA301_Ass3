/**
 * Author: HoangTLT
 */
import { bookingStatus, money } from '../utils/format';

export default function BookingTable({ bookings, staff = false, onStatus, onDelete, onEdit, busy = false }) {
  if (!bookings.length) return <p>Chưa có đơn đặt phòng.</p>;
  return <div className="table-wrap"><table>
    <thead><tr><th>Mã đơn</th>{staff && <th>Khách hàng</th>}<th>Ngày đặt</th><th>Chi tiết phòng</th><th>Tổng tiền</th><th>Trạng thái</th>{staff && <th>Thao tác</th>}</tr></thead>
    <tbody>{bookings.map((b) => <tr key={b.bookingReservationId}>
      <td>#{b.bookingReservationId}</td>{staff && <td>{b.customerFullName}</td>}<td>{b.bookingDate}</td>
      <td>{b.bookingDetails.map((d) => <div key={d.roomId} className="booking-detail">
        <strong>Phòng {d.roomNumber}</strong><br />{d.startDate} → {d.endDate}<br />{money(d.actualPrice)}/đêm
      </div>)}</td>
      <td>{money(b.totalPrice)}</td><td>{bookingStatus[b.bookingStatus]}</td>
      {staff && <td><div className="actions">
        {b.bookingStatus === 'CONFIRMED' && <>
          <button className="secondary" disabled={busy} onClick={() => onEdit(b)}>Sửa phòng / ngày</button>
          <button disabled={busy} onClick={() => onStatus(b.bookingReservationId, 'COMPLETED')}>Hoàn tất</button>
          <button className="danger" disabled={busy} onClick={() => onStatus(b.bookingReservationId, 'CANCELLED')}>Hủy đơn</button>
        </>}
        {b.bookingStatus === 'CANCELLED' && <button className="danger" disabled={busy} onClick={() => onDelete(b.bookingReservationId)}>Xóa</button>}
      </div></td>}
    </tr>)}</tbody>
  </table></div>;
}
