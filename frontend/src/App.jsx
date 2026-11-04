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
    function expire() { setSession(null); navigate('/login', { state: { message: 'PhiÃªn Ä‘Äƒng nháº­p háº¿t háº¡n hoáº·c tÃ i khoáº£n Ä‘Ã£ ngá»«ng hoáº¡t Ä‘á»™ng.' } }); }
    window.addEventListener('session-expired', expire);
    return () => window.removeEventListener('session-expired', expire);
  }, [navigate]);

  const customer = (page) => <ProtectedRoute session={session} role="CUSTOMER">{page}</ProtectedRoute>;
  const staff = (page) => <ProtectedRoute session={session} role="STAFF">{page}</ProtectedRoute>;

  return <>
    <header><div className="header-content">
      <Link to="/" className="brand">FU Mini Hotel</Link>
      <nav aria-label="Äiá»u hÆ°á»›ng chÃ­nh">
        <NavLink to="/" end>Danh sÃ¡ch phÃ²ng</NavLink>
        {session?.role === 'CUSTOMER' && <><NavLink to="/bookings">Lá»‹ch sá»­ Ä‘áº·t phÃ²ng</NavLink><NavLink to="/profile">Há»“ sÆ¡</NavLink></>}
        {session?.role === 'STAFF' && <><NavLink to="/staff/customers">KhÃ¡ch hÃ ng</NavLink><NavLink to="/staff/rooms">Quáº£n lÃ½ phÃ²ng</NavLink><NavLink to="/staff/bookings">ÄÆ¡n Ä‘áº·t phÃ²ng</NavLink></>}
        {session ? <button className="secondary" onClick={logout}>ÄÄƒng xuáº¥t</button>
          : <><NavLink to="/login">ÄÄƒng nháº­p</NavLink><NavLink to="/register">ÄÄƒng kÃ½</NavLink></>}
      </nav>
    </div></header>
    <main>
      {session && <p className="user-line">Xin chÃ o, {session.fullName} Â· {session.role === 'STAFF' ? 'NhÃ¢n viÃªn' : 'KhÃ¡ch hÃ ng'}</p>}
      <Routes>
        <Route path="/" element={<RoomsPage session={session} />} />
        <Route path="/login" element={<LoginPage onLogin={setSession} />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/profile" element={customer(<ProfilePage />)} />
        <Route path="/bookings" element={customer(<BookingsPage />)} />
        <Route path="/staff/customers" element={staff(<StaffCustomersPage />)} />
        <Route path="/staff/rooms" element={staff(<StaffRoomsPage />)} />
        <Route path="/staff/bookings" element={staff(<StaffBookingsPage />)} />
        <Route path="*" element={<><h1>KhÃ´ng tÃ¬m tháº¥y trang</h1><Link to="/">Vá» danh sÃ¡ch phÃ²ng</Link></>} />
      </Routes>
    </main>
    <footer>FU Mini Hotel Â· Há»‡ thá»‘ng quáº£n lÃ½ vÃ  Ä‘áº·t phÃ²ng</footer>
  </>;
}
