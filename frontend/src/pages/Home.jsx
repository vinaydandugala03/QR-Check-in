import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, BRAND, dateLong, timeShort } from '../api.js'
import { Loading } from '../toast.jsx'
import { useAuth } from '../auth.jsx'

export default function Home() {
  const { user } = useAuth()
  const [events, setEvents] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { api('/events').then(setEvents).catch((e) => setError(e.message)) }, [])

  const featured = (events || [])
    .filter((e) => e.status === 'UPCOMING')
    .sort((a, b) => String(a.eventDate).localeCompare(String(b.eventDate)))
    .slice(0, 3)

  return (
    <>
      <section className="hero">
        <p className="tag">LIVE EVENTS · HYDERABAD</p>
        <h1>Book seats. Get a QR ticket. Walk right in.</h1>
        <p className="muted lead">{BRAND} lets you pick your exact seat, keeps every ticket as a unique QR code, and lets gate staff check you in with one scan.</p>
        <div className="row">
          <Link to="/events" className="btn">Browse events</Link>
          {!user && <Link to="/register" className="ghost btnlike">Create an account</Link>}
          {user && <Link to="/tickets" className="ghost btnlike">My tickets</Link>}
        </div>
      </section>

      <h2>Featured events</h2>
      {error && <p className="err">{error}</p>}
      {!error && events === null && <Loading text="Loading events…" />}
      {events && featured.length === 0 && <p className="muted">No upcoming events yet. Check back soon.</p>}
      <div className="grid">
        {featured.map((e) => (
          <Link key={e.id} to={`/events/${e.id}`} className="card event">
            <span className="tag">{e.category}</span>
            <h3>{e.name}</h3>
            {e.artist && <p className="muted">{e.artist}</p>}
            <p>{dateLong(e.eventDate)}{e.startTime && ` · ${timeShort(e.startTime)}`}</p>
            <p className="muted">{e.venue?.name}{e.venue?.location && `, ${e.venue.location}`}</p>
          </Link>
        ))}
      </div>
      {events && events.length > 0 && <p><Link to="/events">See all {events.length} events →</Link></p>}

      <h2>How it works</h2>
      <div className="grid three">
        {[['1. Pick your seats', 'Open an event and choose from the live seat map. Sold seats are blocked instantly.'],
          ['2. Get your QR ticket', 'Each seat gets its own unique QR code in My tickets. View it or download it.'],
          ['3. Scan at the gate', 'Gate staff scan the code. The server confirms it is real and never lets it be used twice.']].map(([t, d]) => (
          <div className="card" key={t}><h3>{t}</h3><p className="muted">{d}</p></div>
        ))}
      </div>
    </>
  )
}
