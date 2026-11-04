/**
 * Author: HoangTLT
 */
import { useEffect, useState } from 'react';
import BookingTable from '../components/BookingTable';
import api, { errorMessage } from '../services/api';

export default function BookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    api.get('/customer/bookings').then(({ data }) => setBookings(data))
      .catch((error) => setMessage(errorMessage(error))).finally(() => setLoading(false));
  }, []);

  return <section className="panel"><h1>Lịch sử đặt phòng</h1>
    {message && <p className="message" role="alert">{message}</p>}
    {loading ? <p>Đang tải...</p> : <BookingTable bookings={bookings} />}
  </section>;
}
