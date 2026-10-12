import { useEffect, useState } from 'react'
import { api, money } from '../api.js'
import { useAuth } from '../auth.jsx'
import { Loading, useToast } from '../toast.jsx'

export default function MyBookings() {
  const { user } = useAuth()
  const toast = useToast()
  const [list, setList] = useState(null)
  const [error, setError] = useState('')
  const load = () => api(`/bookings/user/${user.id}`).then((l) => setList([...l].reverse())).catch((e) => setError(e.message))
  useEffect(() => { load() }, [])

  async function cancel(id) {
    if (!confirm('Cancel this booking? The seats will be released.')) return
    try { await api(`/bookings/${id}/cancel`, { method: 'PUT' }); toast('Booking cancelled'); load() } catch (e) { toast(e.message, 'error') }
  }

  return (
    <>
      <h1>My bookings</h1>
      {error && <p className="err">{error}</p>}
      {list === null && !error && <Loading text="Loading your bookings…" />}
      {list && list.length === 0 && <p className="muted">No bookings yet. Pick an event and choose your seats.</p>}
      <div className="grid">
        {(list || []).map((b) => (
          <div key={b.bookingId} className="card">
            <span className={`pill ${b.status.toLowerCase()}`}>{b.status}</span>
            <h3>{b.eventName}</h3>
            <p>Seats: <strong>{(b.seatNumbers || []).join(', ')}</strong></p>
            <p>{money(b.totalAmount)}</p>
            <p className="muted small">Booking #{b.bookingId} · {new Date(b.bookingDate).toLocaleString()}</p>
            {b.status === 'CONFIRMED' && <button className="ghost" onClick={() => cancel(b.bookingId)}>Cancel booking</button>}
          </div>
        ))}
      </div>
    </>
  )
}
