/**
 * Author: HoangTLT
 */
import { useEffect, useState } from 'react';
import { Link, NavLink, Route, Routes, useNavigate } from 'react-router-dom';
import { clearSession, getSession } from './services/api';
import ProtectedRoute from './components/ProtectedRoute';
import RoomsPage from './pages/RoomsPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import BookingsPage from './pages/BookingsPage';
import StaffCustomersPage from './pages/StaffCustomersPage';
import StaffRoomsPage from './pages/StaffRoomsPage';
import StaffBookingsPage from './pages/StaffBookingsPage';

export default function App() {
  const [session, setSession] = useState(getSession);
  const navigate = useNavigate();

  function logout() { clearSession(); setSession(null); navigate('/login'); }

  useEffect(() => {
    function expire() { setSession(null); navigate('/login', { state: { message: 'Phiên đăng nhập hết hạn hoặc tài khoản đã ngừng hoạt động.' } }); }
    window.addEventListener('session-expired', expire);
    return () => window.removeEventListener('session-expired', expire);
  }, [navigate]);

  const customer = (page) => <ProtectedRoute session={session} role="CUSTOMER">{page}</ProtectedRoute>;
  const staff = (page) => <ProtectedRoute session={session} role="STAFF">{page}</ProtectedRoute>;

  return <>
    <header><div className="header-content">
      <Link to="/" className="brand">FU Mini Hotel</Link>
      <nav aria-label="Điều hướng chính">
        <NavLink to="/" end>Danh sách phòng</NavLink>
        {session?.role === 'CUSTOMER' && <><NavLink to="/bookings">Lịch sử đặt phòng</NavLink><NavLink to="/profile">Hồ sơ</NavLink></>}
        {session?.role === 'STAFF' && <><NavLink to="/staff/customers">Khách hàng</NavLink><NavLink to="/staff/rooms">Quản lý phòng</NavLink><NavLink to="/staff/bookings">Đơn đặt phòng</NavLink></>}
        {session ? <button className="secondary" onClick={logout}>Đăng xuất</button>
          : <><NavLink to="/login">Đăng nhập</NavLink><NavLink to="/register">Đăng ký</NavLink></>}
      </nav>
    </div></header>
    <main>
      {session && <p className="user-line">Xin chào, {session.fullName} · {session.role === 'STAFF' ? 'Nhân viên' : 'Khách hàng'}</p>}
      <Routes>
        <Route path="/" element={<RoomsPage session={session} />} />
        <Route path="/login" element={<LoginPage onLogin={setSession} />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/profile" element={customer(<ProfilePage />)} />
        <Route path="/bookings" element={customer(<BookingsPage />)} />
        <Route path="/staff/customers" element={staff(<StaffCustomersPage />)} />
        <Route path="/staff/rooms" element={staff(<StaffRoomsPage />)} />
        <Route path="/staff/bookings" element={staff(<StaffBookingsPage />)} />
        <Route path="*" element={<><h1>Không tìm thấy trang</h1><Link to="/">Về danh sách phòng</Link></>} />
      </Routes>
    </main>
    <footer>FU Mini Hotel · Hệ thống quản lý và đặt phòng</footer>
  </>;
}
