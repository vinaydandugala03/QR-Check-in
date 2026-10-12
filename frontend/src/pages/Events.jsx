import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, dateLong, timeShort } from '../api.js'
import { Loading } from '../toast.jsx'

export default function Events() {
  const [events, setEvents] = useState(null)
  const [error, setError] = useState('')
  const [q, setQ] = useState('')
  const [category, setCategory] = useState('')

  const load = () => { setError(''); api('/events').then(setEvents).catch((e) => setError(e.message)) }
  useEffect(load, [])

  const categories = useMemo(() => [...new Set((events || []).map((e) => e.category).filter(Boolean))], [events])
  const shown = (events || []).filter((e) =>
    (!category || e.category === category) &&
    `${e.name} ${e.artist || ''} ${e.description || ''} ${e.venue?.name || ''}`.toLowerCase().includes(q.toLowerCase()))

  return (
    <>
      <h1>All events</h1>
      <div className="row">
        <input placeholder="Search events, artists or venues" value={q} onChange={(e) => setQ(e.target.value)} />
        <select value={category} onChange={(e) => setCategory(e.target.value)}>
          <option value="">All categories</option>
          {categories.map((c) => <option key={c}>{c}</option>)}
        </select>
      </div>
      {error && <div className="card"><p className="err">{error}</p><button className="ghost" onClick={load}>Try again</button></div>}
      {!error && events === null && <Loading text="Loading events…" />}
      {!error && events && shown.length === 0 && <p className="muted">No events found.</p>}
      <div className="grid">
        {shown.map((e) => (
          <Link key={e.id} to={`/events/${e.id}`} className="card event">
            <span className="tag">{e.category}</span>
            <h3>{e.name}</h3>
            {e.artist && <p className="muted">{e.artist}</p>}
            <p>{dateLong(e.eventDate)}{e.startTime && ` · ${timeShort(e.startTime)}`}</p>
            <p className="muted">{e.venue?.name}{e.venue?.location && `, ${e.venue.location}`}</p>
            <p><span className={`pill ${String(e.status).toLowerCase()}`}>{e.status}</span></p>
          </Link>
        ))}
      </div>
    </>
  )
}
