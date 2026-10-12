import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api, dateLong, isPremium, money, timeShort } from '../api.js'
import { useAuth } from '../auth.jsx'
import { Loading, useToast } from '../toast.jsx'

const split = (s) => { const m = /^([A-Za-z]+)(\d+)$/.exec(s.seatNumber); return m ? [m[1].toUpperCase(), +m[2]] : [s.seatNumber, 0] }

export default function EventDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const nav = useNavigate()
  const toast = useToast()
  const [summary, setSummary] = useState(null)
  const [event, setEvent] = useState(null)
  const [seats, setSeats] = useState([])
  const [booked, setBooked] = useState([])
  const [picked, setPicked] = useState([])
  const [msg, setMsg] = useState({ text: '', bad: false })
  const [receipt, setReceipt] = useState(null)
  const [busy, setBusy] = useState(false)

  async function load() {
    try {
      const ev = await api(`/events/${id}`)
      const [all, taken] = await Promise.all([api('/seats'), api(`/events/${id}/booked-seats`)])
      setEvent(ev)
      setSeats(all.filter((s) => s.venue?.id === ev.venue?.id))
      setBooked(taken)
      setSummary(await api(`/events/${id}/summary`))
      setPicked((p) => p.filter((sid) => !taken.includes(sid)))
    } catch (e) { setMsg({ text: e.message, bad: true }) }
  }
  useEffect(() => { load() }, [id])

  const rows = useMemo(() => {
    const m = {}
    seats.forEach((s) => { const [r] = split(s); (m[r] ||= []).push(s) })
    Object.values(m).forEach((l) => l.sort((a, b) => split(a)[1] - split(b)[1]))
    return Object.entries(m).sort(([a], [b]) => a.localeCompare(b))
  }, [seats])
  const total = seats.filter((s) => picked.includes(s.id)).reduce((a, s) => a + Number(s.price), 0)
  const labels = seats.filter((s) => picked.includes(s.id)).map((s) => s.seatNumber)

  const toggle = (s) => {
    if (booked.includes(s.id) || receipt) return
    setPicked((p) => (p.includes(s.id) ? p.filter((x) => x !== s.id) : [...p, s.id]))
  }

  async function book() {
    if (!user) return nav('/login')
    setBusy(true); setMsg({ text: '', bad: false })
    try {
      setReceipt(await api('/bookings', { method: 'POST', body: { eventId: Number(id), seatIds: picked } }))
      toast('Booking confirmed. Your QR tickets are ready.')
      setPicked([])
    } catch (e) { setMsg({ text: e.message, bad: true }); toast(e.message, 'error'); load() } finally { setBusy(false) }
  }

  if (!event) return msg.bad ? <p className="err">{msg.text}</p> : <Loading text="Loading event…" />
  const closed = event.status === 'CANCELLED' || event.status === 'COMPLETED'

  return (
    <>
      <h1>{event.name}</h1>
      <p>{dateLong(event.eventDate)}{event.startTime && ` · ${timeShort(event.startTime)}`}{event.endTime && ` to ${timeShort(event.endTime)}`}</p>
      <p>{event.venue?.name}{event.venue?.location && `, ${event.venue.location}`} {event.artist && <span className="muted"> · {event.artist}</span>}</p>
      <p className="muted">{event.description}</p>
      {summary && <p><strong>{summary.availableSeats}</strong> of {summary.totalSeats} seats available</p>}
      {closed && <p className="err">This event is {event.status.toLowerCase()}, so booking is closed.</p>}

      {receipt ? (
        <div className="card narrow">
          <h2 className="ok">Booking confirmed</h2>
          <p>Booking #{receipt.bookingId} · seats <strong>{receipt.seatNumbers.join(', ')}</strong></p>
          <p>Total paid: <strong>{money(receipt.totalAmount)}</strong></p>
          <div className="row">
            <button className="btn" onClick={() => nav('/tickets')}>View my QR tickets</button>
            <button className="ghost" onClick={() => { setReceipt(null); load() }}>Book more seats</button>
          </div>
        </div>
      ) : (
        <>
          <div className="stage">Stage</div>
          {rows.length === 0 && <p className="muted">No seats have been set up for this venue yet.</p>}
          <div className="seatmap">
            {rows.map(([label, list]) => (
              <div className="seatrow" key={label}>
                <b>{label}</b>
                {list.map((s) => {
                  const taken = booked.includes(s.id)
                  return (
                    <button key={s.id} disabled={taken || closed} onClick={() => toggle(s)}
                      className={`seat ${isPremium(s) ? 'vip' : ''} ${taken ? 'booked' : ''} ${picked.includes(s.id) ? 'picked' : ''}`}
                      title={`${s.seatNumber} · ${s.section || ''} · ${money(s.price)}${taken ? ' · sold' : ''}`}>
                      {split(s)[1] || s.seatNumber}
                    </button>
                  )
                })}
              </div>
            ))}
          </div>
          <p className="legend"><span className="seat" /> Available <span className="seat vip" /> Premium <span className="seat picked" /> Selected <span className="seat booked" /> Sold</p>
          {msg.text && <p className={msg.bad ? 'err' : 'ok'}>{msg.text}</p>}
          {picked.length > 0 && (
            <div className="card row between">
              <span>{labels.join(', ')} · <strong>{money(total)}</strong></span>
              <button className="btn" onClick={book} disabled={busy}>{busy ? 'Booking…' : user ? 'Confirm booking' : 'Log in to book'}</button>
            </div>
          )}
        </>
      )}
    </>
  )
}
