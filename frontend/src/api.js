export const BRAND = 'EVENTTICKETINGSYSTEM'
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/+$/, '')

export async function api(path, { method = 'GET', body } = {}) {
  const token = localStorage.getItem('token')
  let res
  try {
    res = await fetch(API_BASE_URL + path, {
      method,
      headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new Error('Cannot reach the server. Is the backend running on port 4040?')
  }
  const text = await res.text()
  let data = text
  try { data = text ? JSON.parse(text) : null } catch { /* plain text reply */ }
  if (!res.ok) {
    const msg = (data && data.message) || (typeof data === 'string' && data) || res.statusText || 'Request failed'
    throw new Error(res.status === 401 && !path.startsWith('/auth') ? 'Please log in to continue' : msg)
  }
  return data
}

export const money = (n) => '₹' + Number(n || 0).toLocaleString('en-IN')
export const timeShort = (t) => (t ? String(t).slice(0, 5) : '')
export const dateLong = (d) => (d ? new Date(d + 'T00:00:00').toLocaleDateString([], { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' }) : '')
export const isPremium = (s) => ['PREMIUM', 'VIP'].includes(String(s.seatType || s.section || '').toUpperCase())
