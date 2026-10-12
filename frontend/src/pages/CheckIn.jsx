import { useEffect, useRef, useState } from 'react'
import { Html5Qrcode } from 'html5-qrcode'
import { api, dateLong, timeShort } from '../api.js'

const LABEL = { SUCCESS: 'Checked in', ALREADY_CHECKED_IN: 'Already checked in', INVALID: 'Invalid code', WRONG_EVENT: 'Wrong event', NOT_VALID: 'Not valid' }

export default function CheckIn() {
  const [events, setEvents] = useState([])
  const [eventId, setEventId] = useState('')
  const [summary, setSummary] = useState(null)
  const [result, setResult] = useState(null)
  const [recent, setRecent] = useState([])
  const [manual, setManual] = useState('')
  const [scanning, setScanning] = useState(false)
  const [camError, setCamError] = useState('')
  const scanner = useRef(null)
  const last = useRef({ text: '', at: 0 })
  const eventRef = useRef('')
  eventRef.current = eventId

  useEffect(() => { api('/events').then(setEvents).catch(() => {}) }, [])

  const loadSummary = () => eventRef.current && api(`/events/${eventRef.current}/summary`).then(setSummary).catch(() => {})
  useEffect(() => {
    setSummary(null)
    loadSummary()
    const t = setInterval(loadSummary, 5000)
    return () => clearInterval(t)
  }, [eventId])

  async function verify(text) {
    const code = text.trim()
    if (!code) return
    try {
      const r = await api('/checkin', { method: 'POST', body: { ticketCode: code, eventId: eventRef.current ? Number(eventRef.current) : null } })
      setResult(r)
      setRecent((l) => [{ ...r, at: new Date() }, ...l].slice(0, 8))
      loadSummary()
    } catch (e) {
      setResult({ status: 'INVALID', message: e.message })
    }
  }

  async function startCamera() {
    setCamError('')
    try {
      const cams = await Html5Qrcode.getCameras()
      if (!cams.length) throw new Error('No camera found on this device.')
      const cam = cams.find((c) => /back|rear|environment/i.test(c.label)) || cams[0]
      const s = new Html5Qrcode('qr-reader')
      scanner.current = s
      await s.start(cam.id, { fps: 10, qrbox: { width: 240, height: 240 } }, (text) => {
        const now = Date.now()
        if (text === last.current.text && now - last.current.at < 4000) return
        last.current = { text, at: now }
        verify(text)
      }, () => {})
      setScanning(true)
    } catch (e) {
      scanner.current = null
      setCamError(String(e?.message || e).includes('Permission') || String(e).includes('NotAllowed')
        ? 'Camera access was blocked. Click the camera icon in the address bar, allow it, then press Start camera again. You can also type the code below.'
        : `Could not start the camera: ${e?.message || e}. You can type the code below instead.`)
    }
  }

  async function stopCamera() {
    try { await scanner.current?.stop(); scanner.current?.clear() } catch { /* already stopped */ }
    scanner.current = null
    setScanning(false)
  }
  useEffect(() => () => { scanner.current?.stop().catch(() => {}) }, [])

  const t = result?.ticket
  return (
    <>
      <h1>QR check-in</h1>
      <div className="grid two">
        <div className="card">
          <label>Event being scanned
            <select value={eventId} onChange={(e) => setEventId(e.target.value)}>
              <option value="">Any event</option>
              {events.map((e) => <option key={e.id} value={e.id}>{e.name} · {dateLong(e.eventDate)}</option>)}
            </select>
          </label>
          <div id="qr-reader" className={scanning ? 'reader on' : 'reader'} />
          <div className="row">
            {!scanning ? <button className="btn" onClick={startCamera}>Start camera</button> : <button className="ghost" onClick={stopCamera}>Stop camera</button>}
          </div>
          {camError && <p className="err">{camError}</p>}
          <form className="row" onSubmit={(e) => { e.preventDefault(); verify(manual); setManual('') }}>
            <input placeholder="Or type / paste the ticket code" value={manual} onChange={(e) => setManual(e.target.value)} />
            <button className="ghost">Check in</button>
          </form>
        </div>

        <div>
          {summary && (
            <div className="card">
              <p className="count">{summary.checkedIn} <span className="muted small">of {summary.ticketsIssued} tickets checked in</span></p>
              <div className="bar-track"><div className="bar-fill" style={{ width: `${summary.ticketsIssued ? (100 * summary.checkedIn) / summary.ticketsIssued : 0}%` }} /></div>
              <p className="muted small">{summary.eventName} · {summary.availableSeats} seats unsold</p>
            </div>
          )}
          {result && (
            <div className={`card result ${result.status}`}>
              <h2>{LABEL[result.status] || result.status}</h2>
              <p>{result.message}</p>
              {t && <p className="small">{t.attendeeName} · {t.eventName} · seat <strong>{t.seatNumber}</strong>{t.startTime && ` · ${timeShort(t.startTime)}`}</p>}
            </div>
          )}
          {!result && <div className="card"><p className="muted">Scan a ticket or type its code. The result appears here.</p></div>}
          {recent.length > 0 && (
            <div className="card">
              <h3>Recent scans</h3>
              {recent.map((r, i) => (
                <p key={i} className="small"><span className={`dot ${r.status}`} /> {r.at.toLocaleTimeString()} · {LABEL[r.status]} {r.ticket && `· seat ${r.ticket.seatNumber}`}</p>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  )
}
