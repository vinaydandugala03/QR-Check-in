import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, dateLong, money } from '../api.js'
import { Loading } from '../toast.jsx'

const pct = (a, b) => (b ? Math.round((100 * a) / b) : 0)

export default function Dashboard() {
  const [d, setD] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => {
    const load = () => api('/dashboard').then(setD).catch((e) => setError(e.message))
    load()
    const t = setInterval(load, 10000)
    return () => clearInterval(t)
  }, [])

  if (error) return <p className="err">{error}</p>
  if (!d) return <Loading text="Loading dashboard…" />
  return (
    <>
      <h1>Dashboard</h1>
      <div className="grid four">
        {[['Events', d.totalEvents], ['Users', d.totalUsers], ['Bookings', d.totalBookings], ['Revenue', money(d.revenue)], ['Tickets issued', d.ticketsIssued], ['Checked in', `${d.checkedIn} (${pct(d.checkedIn, d.ticketsIssued)}%)`]].map(([k, v]) => (
          <div className="card" key={k}><p className="count">{v}</p><p className="muted">{k}</p></div>
        ))}
      </div>
      <h2>Attendance by event</h2>
      <div className="tablewrap">
        <table>
          <thead><tr><th>Event</th><th>Date</th><th>Status</th><th>Seats sold</th><th>Checked in</th></tr></thead>
          <tbody>
            {d.events.map((e) => (
              <tr key={e.eventId}>
                <td><Link to={`/events/${e.eventId}`}>{e.eventName}</Link></td>
                <td>{dateLong(e.eventDate)}</td>
                <td>{e.status}</td>
                <td>{e.bookedSeats} / {e.totalSeats}<div className="bar-track"><div className="bar-fill" style={{ width: `${pct(e.bookedSeats, e.totalSeats)}%` }} /></div></td>
                <td>{e.checkedIn} / {e.ticketsIssued}<div className="bar-track"><div className="bar-fill ok" style={{ width: `${pct(e.checkedIn, e.ticketsIssued)}%` }} /></div></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}
