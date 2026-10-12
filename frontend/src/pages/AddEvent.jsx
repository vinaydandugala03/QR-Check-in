import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api.js'
import { useToast } from '../toast.jsx'

const CATEGORIES = ['Music', 'Comedy', 'Theatre', 'Conference', 'Sports', 'Other']
const today = () => new Date().toISOString().slice(0, 10)
const sec = (t) => (t && t.length === 5 ? t + ':00' : t || null)

export default function AddEvent() {
  const toast = useToast()
  const nav = useNavigate()
  const [venues, setVenues] = useState([])
  const [f, setF] = useState({ name: '', artist: '', category: 'Music', description: '', eventDate: '', startTime: '19:00', endTime: '22:00', venueId: '' })
  const [errors, setErrors] = useState({})
  const [busy, setBusy] = useState(false)
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })

  useEffect(() => { api('/venues').then(setVenues).catch((e) => toast(e.message, 'error')) }, [])

  function validate() {
    const e = {}
    if (f.name.trim().length < 3) e.name = 'Enter an event name (at least 3 characters)'
    if (!f.eventDate) e.eventDate = 'Choose a date'
    else if (f.eventDate < today()) e.eventDate = 'The date cannot be in the past'
    if (f.startTime && f.endTime && f.endTime <= f.startTime) e.endTime = 'End time must be after the start time'
    if (!f.venueId) e.venueId = 'Choose a venue'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  async function submit(ev) {
    ev.preventDefault()
    if (!validate()) return
    setBusy(true)
    try {
      const created = await api('/events', {
        method: 'POST',
        body: { name: f.name.trim(), artist: f.artist.trim(), category: f.category, description: f.description.trim(), eventDate: f.eventDate, startTime: sec(f.startTime), endTime: sec(f.endTime), status: 'UPCOMING', venue: { id: Number(f.venueId) } },
      })
      toast(`"${created.name}" was created and is now on the events page`)
      nav(`/events/${created.id}`)
    } catch (err) { toast(err.message, 'error') } finally { setBusy(false) }
  }

  const Err = ({ k }) => (errors[k] ? <span className="field-err">{errors[k]}</span> : null)

  return (
    <>
      <h1>Add event</h1>
      <form className="card narrow wide" onSubmit={submit} noValidate>
        <label>Event name<input value={f.name} onChange={set('name')} /><Err k="name" /></label>
        <label>Artist / host<input value={f.artist} onChange={set('artist')} /></label>
        <label>Category<select value={f.category} onChange={set('category')}>{CATEGORIES.map((c) => <option key={c}>{c}</option>)}</select></label>
        <label>Description<textarea rows={3} value={f.description} onChange={set('description')} /></label>
        <label>Date<input type="date" min={today()} value={f.eventDate} onChange={set('eventDate')} /><Err k="eventDate" /></label>
        <div className="row">
          <label>Starts<input type="time" value={f.startTime} onChange={set('startTime')} /></label>
          <label>Ends<input type="time" value={f.endTime} onChange={set('endTime')} /><Err k="endTime" /></label>
        </div>
        <label>Venue
          <select value={f.venueId} onChange={set('venueId')}>
            <option value="">Choose a venue</option>
            {venues.map((v) => <option key={v.id} value={v.id}>{v.name}, {v.location}</option>)}
          </select>
          <Err k="venueId" />
        </label>
        <p className="muted small">Seats come from the venue. Add or change seats in Admin → Seats.</p>
        <button className="btn" disabled={busy}>{busy ? 'Creating…' : 'Create event'}</button>
      </form>
    </>
  )
}
