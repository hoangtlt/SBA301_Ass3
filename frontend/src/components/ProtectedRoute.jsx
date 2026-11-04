/**
 * Author: HoangTLT
 */
import { Navigate } from 'react-router-dom';

export default function ProtectedRoute({ session, role, children }) {
  if (!session) return <Navigate to="/login" replace />;
  if (session.role !== role) return <Navigate to="/" replace />;
  return children;
}
