import { createContext, useContext, useState } from 'react'
import { api } from './api.js'

const Ctx = createContext(null)
export const useAuth = () => useContext(Ctx)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem('user') || 'null'))

  const save = (res) => {
    localStorage.setItem('token', res.token)
    localStorage.setItem('user', JSON.stringify(res.user))
    setUser(res.user)
    return res.user
  }
  const login = async (email, password) => save(await api('/auth/login', { method: 'POST', body: { email, password } }))
  const register = async ({ name, email, password }) => {
    await api('/users', { method: 'POST', body: { name, email, password } })
    return login(email, password)
  }
  const updateUser = (u) => { localStorage.setItem('user', JSON.stringify(u)); setUser(u) }
  const logout = () => { localStorage.removeItem('token'); localStorage.removeItem('user'); setUser(null) }

  return <Ctx.Provider value={{ user, login, register, logout, updateUser }}>{children}</Ctx.Provider>
}
