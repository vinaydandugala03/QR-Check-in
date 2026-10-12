import { useEffect, useState } from 'react'
import { api, dateLong, money } from '../api.js'

const TABS = ['Events', 'Venues', 'Seats', 'Users', 'Bookings']
const t8 = (t) => (t && t.length === 5 ? t + ':00' : t || null)
const blankEvent = { name: '', artist: '', category: 'Music', description: '', eventDate: '', startTime: '19:00', endTime: '22:00', status: 'UPCOMING', venueId: '' }

export default function Admin() {
  const [tab, setTab] = useState('Events')
  const [d, setD] = useState({ events: [], venues: [], seats: [], users: [], bookings: [] })
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')

  const load = () => Promise.all([api('/events'), api('/venues'), api('/seats'), api('/users'), api('/bookings')])
    .then(([events, venues, seats, users, bookings]) => setD({ events, venues, seats, users, bookings }))
    .catch((e) => setError(e.message))
  useEffect(() => { load() }, [])

  async function run(fn, success) {
    setError(''); setOk('')
    try { await fn(); setOk(success); await load() } catch (e) { setError(e.message) }
  }

  const revenue = d.bookings.filter((b) => b.status === 'CONFIRMED').reduce((a, b) => a + Number(b.totalAmount), 0)

  return (
    <>
      <h1>Admin</h1>
      <div className="grid four">
        {[['Events', d.events.length], ['Venues', d.venues.length], ['Users', d.users.length], ['Bookings', d.bookings.length], ['Revenue', money(revenue)]].map(([k, v]) => (
          <div className="card" key={k}><p className="count">{v}</p><p className="muted">{k}</p></div>
        ))}
      </div>
      <div className="tabs">{TABS.map((t) => <button key={t} className={t === tab ? 'btn small' : 'ghost'} onClick={() => { setTab(t); setError(''); setOk('') }}>{t}</button>)}</div>
      {error && <p className="err">{error}</p>}
      {ok && <p className="ok">{ok}</p>}

      {tab === 'Events' && <EventsTab d={d} run={run} />}
      {tab === 'Venues' && <VenuesTab d={d} run={run} />}
      {tab === 'Seats' && <SeatsTab d={d} run={run} />}
      {tab === 'Users' && (
        <table><thead><tr><th>Name</th><th>Email</th><th>Role</th><th /></tr></thead><tbody>
          {d.users.map((u) => (
            <tr key={u.id}><td>{u.name}</td><td>{u.email}</td><td>{u.role}</td>
              <td><button className="ghost" onClick={() => confirm(`Delete ${u.email}?`) && run(() => api(`/users/${u.id}`, { method: 'DELETE' }), 'User deleted')}>Delete</button></td></tr>
          ))}
        </tbody></table>
      )}
      {tab === 'Bookings' && (
        <table><thead><tr><th>#</th><th>User</th><th>Event</th><th>Seats</th><th>Total</th><th>Status</th><th /></tr></thead><tbody>
          {[...d.bookings].reverse().map((b) => (
            <tr key={b.bookingId}><td>{b.bookingId}</td><td>{b.userName}</td><td>{b.eventName}</td><td>{(b.seatNumbers || []).join(', ')}</td><td>{money(b.totalAmount)}</td><td>{b.status}</td>
              <td>{b.status === 'CONFIRMED' && <button className="ghost" onClick={() => run(() => api(`/bookings/${b.bookingId}/cancel`, { method: 'PUT' }), 'Booking cancelled')}>Cancel</button>}</td></tr>
          ))}
        </tbody></table>
      )}
    </>
  )
}

function EventsTab({ d, run }) {
  const [f, setF] = useState(blankEvent)
  const [editing, setEditing] = useState(null)
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })

  const edit = (e) => { setEditing(e.id); setF({ name: e.name, artist: e.artist || '', category: e.category || '', description: e.description || '', eventDate: e.eventDate, startTime: (e.startTime || '').slice(0, 5), endTime: (e.endTime || '').slice(0, 5), status: e.status || 'UPCOMING', venueId: String(e.venue?.id || '') }) }
  const submit = (ev) => {
    ev.preventDefault()
    const body = { name: f.name, artist: f.artist, category: f.category, description: f.description, eventDate: f.eventDate, startTime: t8(f.startTime), endTime: t8(f.endTime), status: f.status, venue: { id: Number(f.venueId) } }
    run(() => (editing ? api(`/events/${editing}`, { method: 'PUT', body }) : api('/events', { method: 'POST', body })), editing ? 'Event updated' : 'Event created')
    setEditing(null); setF(blankEvent)
  }

  return (
    <div className="grid two">
      <table><thead><tr><th>Event</th><th>Date</th><th>Venue</th><th>Status</th><th /></tr></thead><tbody>
        {d.events.map((e) => (
          <tr key={e.id}><td>{e.name}</td><td>{dateLong(e.eventDate)}</td><td>{e.venue?.name}</td><td>{e.status}</td>
            <td className="nowrap"><button className="ghost" onClick={() => edit(e)}>Edit</button>{' '}
              <button className="ghost" onClick={() => confirm(`Delete ${e.name}?`) && run(() => api(`/events/${e.id}`, { method: 'DELETE' }), 'Event deleted')}>Delete</button></td></tr>
        ))}
      </tbody></table>
      <form className="card" onSubmit={submit}>
        <h3>{editing ? 'Edit event' : 'New event'}</h3>
        <label>Name<input value={f.name} onChange={set('name')} required /></label>
        <label>Artist / host<input value={f.artist} onChange={set('artist')} /></label>
        <label>Category<input value={f.category} onChange={set('category')} placeholder="Music, Comedy, Theatre…" /></label>
        <label>Description<textarea value={f.description} onChange={set('description')} /></label>
        <label>Date<input type="date" value={f.eventDate} onChange={set('eventDate')} required /></label>
        <div className="row"><label>Starts<input type="time" value={f.startTime} onChange={set('startTime')} /></label><label>Ends<input type="time" value={f.endTime} onChange={set('endTime')} /></label></div>
        <label>Status<select value={f.status} onChange={set('status')}>{['UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED'].map((s) => <option key={s}>{s}</option>)}</select></label>
        <label>Venue<select value={f.venueId} onChange={set('venueId')} required><option value="">Choose a venue</option>{d.venues.map((v) => <option key={v.id} value={v.id}>{v.name}</option>)}</select></label>
        <div className="row"><button className="btn">{editing ? 'Save changes' : 'Create event'}</button>{editing && <button type="button" className="ghost" onClick={() => { setEditing(null); setF(blankEvent) }}>Cancel</button>}</div>
      </form>
    </div>
  )
}

function VenuesTab({ d, run }) {
  const [f, setF] = useState({ name: '', location: '', capacity: 100 })
  return (
    <div className="grid two">
      <table><thead><tr><th>Venue</th><th>Location</th><th>Capacity</th><th>Seats</th><th /></tr></thead><tbody>
        {d.venues.map((v) => (
          <tr key={v.id}><td>{v.name}</td><td>{v.location}</td><td>{v.capacity}</td><td>{d.seats.filter((s) => s.venue?.id === v.id).length}</td>
            <td><button className="ghost" onClick={() => confirm(`Delete ${v.name}?`) && run(() => api(`/venues/${v.id}`, { method: 'DELETE' }), 'Venue deleted')}>Delete</button></td></tr>
        ))}
      </tbody></table>
      <form className="card" onSubmit={(e) => { e.preventDefault(); run(() => api('/venues', { method: 'POST', body: { ...f, capacity: Number(f.capacity) } }), 'Venue created'); setF({ name: '', location: '', capacity: 100 }) }}>
        <h3>New venue</h3>
        <label>Name<input value={f.name} onChange={(e) => setF({ ...f, name: e.target.value })} required /></label>
        <label>Location<input value={f.location} onChange={(e) => setF({ ...f, location: e.target.value })} required /></label>
        <label>Capacity<input type="number" min="1" value={f.capacity} onChange={(e) => setF({ ...f, capacity: e.target.value })} required /></label>
        <button className="btn">Add venue</button>
      </form>
    </div>
  )
}

function SeatsTab({ d, run }) {
  const [f, setF] = useState({ venueId: '', category: 'Regular', rowFrom: 'A', rowTo: 'E', seatsPerRow: 10, price: 500 })
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })
  return (
    <div className="grid two">
      <table><thead><tr><th>Venue</th><th>Seats</th><th>Premium</th><th>Regular</th></tr></thead><tbody>
        {d.venues.map((v) => {
          const list = d.seats.filter((s) => s.venue?.id === v.id)
          const prem = list.filter((s) => ['PREMIUM', 'VIP'].includes(String(s.seatType).toUpperCase())).length
          return <tr key={v.id}><td>{v.name}</td><td>{list.length}</td><td>{prem}</td><td>{list.length - prem}</td></tr>
        })}
      </tbody></table>
      <form className="card" onSubmit={(e) => { e.preventDefault(); run(() => api('/seats/bulk', { method: 'POST', body: { venueId: Number(f.venueId), category: f.category, rowFrom: f.rowFrom, rowTo: f.rowTo, seatsPerRow: Number(f.seatsPerRow), price: Number(f.price) } }), 'Seats created') }}>
        <h3>Add seats in bulk</h3>
        <label>Venue<select value={f.venueId} onChange={set('venueId')} required><option value="">Choose a venue</option>{d.venues.map((v) => <option key={v.id} value={v.id}>{v.name}</option>)}</select></label>
        <label>Category (use Premium for premium seats)<input value={f.category} onChange={set('category')} required /></label>
        <div className="row"><label>Row from<input maxLength={1} value={f.rowFrom} onChange={set('rowFrom')} required /></label><label>Row to<input maxLength={1} value={f.rowTo} onChange={set('rowTo')} required /></label></div>
        <label>Seats per row<input type="number" min="1" value={f.seatsPerRow} onChange={set('seatsPerRow')} required /></label>
        <label>Price per seat (₹)<input type="number" min="1" value={f.price} onChange={set('price')} required /></label>
        <button className="btn">Create seats</button>
      </form>
    </div>
  )
}
