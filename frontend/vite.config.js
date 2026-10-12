import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The browser talks to /api on port 5173; Vite forwards it to the Spring Boot backend on 4040.
export default defineConfig({
  plugins: [react()],
  server: { port: 5173, proxy: { '/api': 'http://localhost:4040' } },
})
