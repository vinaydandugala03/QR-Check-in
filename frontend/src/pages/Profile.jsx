import { useState } from 'react'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import { useToast } from '../toast.jsx'

export default function Profile() {
  const { user, updateUser } = useAuth()
  const toast = useToast()
  const [p, setP] = useState({ name: user.name, email: user.email })
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '' })
  const [m1, setM1] = useState(null)
  const [m2, setM2] = useState(null)

  async function saveProfile(e) {
    e.preventDefault(); setM1(null)
    try { updateUser(await api(`/users/${user.id}`, { method: 'PUT', body: p })); setM1({ ok: true, t: 'Profile updated' }); toast('Profile updated') }
    catch (err) { setM1({ t: err.message }) }
  }
  async function savePassword(e) {
    e.preventDefault(); setM2(null)
    try { await api(`/users/${user.id}/password`, { method: 'PUT', body: pw }); setPw({ currentPassword: '', newPassword: '' }); setM2({ ok: true, t: 'Password changed' }); toast('Password changed') }
    catch (err) { setM2({ t: err.message }) }
  }

  return (
    <>
      <h1>Profile</h1>
      <div className="grid two">
        <form className="card" onSubmit={saveProfile}>
          <h3>Your details</h3>
          <label>Name<input value={p.name} onChange={(e) => setP({ ...p, name: e.target.value })} required /></label>
          <label>Email<input type="email" value={p.email} onChange={(e) => setP({ ...p, email: e.target.value })} required /></label>
          {m1 && <p className={m1.ok ? 'ok' : 'err'}>{m1.t}</p>}
          <button className="btn">Save</button>
        </form>
        <form className="card" onSubmit={savePassword}>
          <h3>Change password</h3>
          <label>Current password<input type="password" value={pw.currentPassword} onChange={(e) => setPw({ ...pw, currentPassword: e.target.value })} required /></label>
          <label>New password (6+ characters)<input type="password" minLength={6} value={pw.newPassword} onChange={(e) => setPw({ ...pw, newPassword: e.target.value })} required /></label>
          {m2 && <p className={m2.ok ? 'ok' : 'err'}>{m2.t}</p>}
          <button className="btn">Change password</button>
        </form>
      </div>
    </>
  )
}
