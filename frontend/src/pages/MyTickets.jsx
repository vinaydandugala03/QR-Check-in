import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, dateLong, timeShort } from '../api.js'
import { useAuth } from '../auth.jsx'
import { Loading } from '../toast.jsx'

export default function MyTickets() {
  const { user } = useAuth()
  const [tickets, setTickets] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { api(`/tickets/user/${user.id}`).then(setTickets).catch((e) => setError(e.message)) }, [])

  return (
    <>
      <h1>My tickets</h1>
      <p className="muted">Show the QR code at the gate. Each seat has its own code and works once.</p>
      {error && <p className="err">{error}</p>}
      {tickets === null && !error && <Loading text="Loading your tickets…" />}
      {tickets && tickets.length === 0 && <p className="muted">No tickets yet. <Link to="/events">Find an event</Link> and book seats.</p>}
      <div className="grid">
        {(tickets || []).map((t) => (
          <div key={t.ticketId} className="card ticket">
            <span className={`pill ${t.checkedIn ? 'completed' : 'confirmed'}`}>{t.checkedIn ? 'USED' : 'VALID'}</span>
            <h3>{t.eventName}</h3>
            <p>{dateLong(t.eventDate)}{t.startTime && ` · ${timeShort(t.startTime)}`}</p>
            <p className="muted">{t.venueName}</p>
            <p>Seat <strong>{t.seatNumber}</strong></p>
            <img src={t.qrImage} alt={`QR code for seat ${t.seatNumber}`} width="200" height="200" />
            <p className="muted small">{t.ticketCode}</p>
            {t.checkedIn
              ? <p className="ok">Checked in {new Date(t.checkedInAt).toLocaleString()}</p>
              : <a className="ghost btnlike" href={t.qrImage} download={`ticket-${t.eventName.replace(/\W+/g, '-')}-${t.seatNumber}.png`}>Download QR</a>}
          </div>
        ))}
      </div>
    </>
  )
}
