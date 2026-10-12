import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'

export default function Auth({ mode }) {
  const { login, register } = useAuth()
  const nav = useNavigate()
  const isLogin = mode === 'login'
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  async function submit(e) {
    e.preventDefault()
    setError(''); setBusy(true)
    try {
      const u = isLogin ? await login(form.email, form.password) : await register(form)
      nav(u.role === 'ADMIN' ? '/dashboard' : u.role === 'GATE_STAFF' ? '/checkin' : '/events')
    } catch (err) { setError(err.message) } finally { setBusy(false) }
  }

  return (
    <form className="card narrow" onSubmit={submit}>
      <h1>{isLogin ? 'Log in' : 'Create your account'}</h1>
      {!isLogin && <label>Name<input value={form.name} onChange={set('name')} required /></label>}
      <label>Email<input type="email" value={form.email} onChange={set('email')} required /></label>
      <label>Password<input type="password" value={form.password} onChange={set('password')} required minLength={isLogin ? 1 : 6} /></label>
      {error && <p className="err">{error}</p>}
      <button className="btn" disabled={busy}>{busy ? 'Please wait…' : isLogin ? 'Log in' : 'Sign up'}</button>
      <p className="muted">{isLogin ? <>New here? <Link to="/register">Create an account</Link></> : <>Have an account? <Link to="/login">Log in</Link></>}</p>
      {isLogin && <p className="muted small">Demo logins: customer@demo.com / Customer@123 · admin@demo.com / Admin@123 · gate@demo.com / Gate@123</p>}
    </form>
  )
}
