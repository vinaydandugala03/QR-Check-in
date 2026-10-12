import { Link, NavLink, Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import { useAuth } from './auth.jsx'
import { BRAND } from './api.js'
import Home from './pages/Home.jsx'
import Events from './pages/Events.jsx'
import EventDetail from './pages/EventDetail.jsx'
import Auth from './pages/Auth.jsx'
import MyBookings from './pages/MyBookings.jsx'
import MyTickets from './pages/MyTickets.jsx'
import Profile from './pages/Profile.jsx'
import AddEvent from './pages/AddEvent.jsx'
import CheckIn from './pages/CheckIn.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Admin from './pages/Admin.jsx'

function Guard({ roles, children }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />
  return children
}

const STAFF = ['ADMIN', 'GATE_STAFF']

export default function App() {
  const { user, logout } = useAuth()
  const nav = useNavigate()
  const role = user?.role
  return (
    <>
      <header className="bar">
        <Link to="/" className="brand">{BRAND}</Link>
        <nav>
          <NavLink to="/events">Events</NavLink>
          {user && <NavLink to="/tickets">My tickets</NavLink>}
          {user && <NavLink to="/bookings">Bookings</NavLink>}
          {STAFF.includes(role) && <NavLink to="/checkin">QR check-in</NavLink>}
          {STAFF.includes(role) && <NavLink to="/dashboard">Dashboard</NavLink>}
          {role === 'ADMIN' && <NavLink to="/add-event">Add event</NavLink>}
          {role === 'ADMIN' && <NavLink to="/admin">Admin</NavLink>}
        </nav>
        <div className="who">
          {user ? (
            <>
              <Link to="/profile">{user.name}</Link>
              <span className="pill">{role.toLowerCase().replace('_', ' ')}</span>
              <button className="ghost" onClick={() => { logout(); nav('/') }}>Log out</button>
            </>
          ) : (
            <>
              <Link to="/login">Log in</Link>
              <Link to="/register" className="btn small">Sign up</Link>
            </>
          )}
        </div>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/events" element={<Events />} />
          <Route path="/events/:id" element={<EventDetail />} />
          <Route path="/login" element={<Auth mode="login" />} />
          <Route path="/register" element={<Auth mode="register" />} />
          <Route path="/tickets" element={<Guard><MyTickets /></Guard>} />
          <Route path="/bookings" element={<Guard><MyBookings /></Guard>} />
          <Route path="/profile" element={<Guard><Profile /></Guard>} />
          <Route path="/checkin" element={<Guard roles={STAFF}><CheckIn /></Guard>} />
          <Route path="/dashboard" element={<Guard roles={STAFF}><Dashboard /></Guard>} />
          <Route path="/add-event" element={<Guard roles={['ADMIN']}><AddEvent /></Guard>} />
          <Route path="/admin" element={<Guard roles={['ADMIN']}><Admin /></Guard>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <footer className="foot">{BRAND} · Hyderabad</footer>
    </>
  )
}
