# EVENTTICKETINGSYSTEM

Spring Boot backend (port 4040, MySQL, Flyway) + our own React frontend (Vite, port 5173).
Events are read from MySQL through the REST API. Nothing is hardcoded in the frontend.

```
event-ticketing-system/
  backend/     Spring Boot app (controllers, services, repositories, entities, security, Flyway migrations)
  frontend/    React + Vite app
  database/    schema.sql (full schema, optional) and setup.sql (creates the empty database)
  README.md
```

## 1. Requirements
JDK 17 or newer, Maven 3.9+, Node 18+ (with npm), MySQL 8 running locally.

Check them in a terminal:
```cmd
java -version
mvn -version
node -v
npm -v
```
MySQL: it is running if MySQL Workbench can connect to your local instance. (`mysql --version` only works if MySQL's `bin` folder is on your PATH; you do not need it.)

## 2. Extract the ZIP
PowerShell, from the folder holding the zip:
```powershell
Expand-Archive event-ticketing-system.zip -DestinationPath .
cd event-ticketing-system
```
(or right-click the zip and choose Extract All). Windows may add one extra folder level; open folders until you see `backend` and `frontend` side by side.

## 3. Database
You do not need to create anything by hand. On first start the backend creates the `event_ticketing_db` database and every table (Flyway runs `V1__initial_schema.sql` and `V2__tickets.sql`).

Optional manual setup in MySQL Workbench: open `database\setup.sql` (just the empty database) or `database\schema.sql` (full schema) and run it.

## 4. Configure credentials
Settings live in `backend\.env` (git-ignored). It is already set for a local MySQL with user `root` and password `root`:
```
DB_URL=jdbc:mysql://localhost:3306/event_ticketing_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=root
```
If your MySQL password differs: `notepad backend\.env` and change `DB_PASSWORD`. A blank template is in `backend\.env.example`. No passwords are written in the source code.

## 5. Start the backend (terminal 1)
```cmd
cd backend
mvn spring-boot:run
```
Wait for `Started BackendApplication`. Leave the window open.

## 6. Start the frontend (terminal 2)
```cmd
cd frontend
npm install
npm run dev
```
The dev server forwards `/api` to the backend, so there are no CORS problems in development.

## 7. Open the app
http://localhost:5173

| Role | Email | Password |
|---|---|---|
| Admin | admin@demo.com | Admin@123 |
| Gate staff | gate@demo.com | Gate@123 |
| Customer | customer@demo.com | Customer@123 |

On first start the backend also creates sample venues, seats and 4 upcoming events (only when the database has no events).

## 8. Try it
1. **Events load from MySQL:** open Events. In MySQL Workbench, `SELECT * FROM events;` shows the same rows.
2. **Create an event:** log in as admin, open **Add event**, fill the form, save. It appears on the Events page and in the `events` table immediately.
3. **Book and get a QR ticket:** log in as the customer, open an event, pick seats, confirm. Open **My tickets** to view or download each seat's QR code.
4. **QR check-in:** log in as gate staff (or admin), open **QR check-in**, choose the event, press **Start camera** and point it at a ticket QR shown on another screen (phone or second monitor). You can also paste the ticket code under the QR. The result card says Checked in, Already checked in, Invalid code, Wrong event, or Not valid.
5. **Duplicate check-in:** scan the same ticket again; it is refused with "Already checked in".
6. **Dashboard:** log in as admin or gate staff and open **Dashboard** for totals, seats sold and attendance per event.

## 9. Features
- Event listing, search, category filter, event details with available seats
- Seat map booking (sold seats blocked), cancel booking
- Register, login (JWT), profile, change password
- Add / edit / delete events, venues, seats (bulk) from the admin area
- One unique QR ticket per booked seat, viewable and downloadable
- Camera QR scanner with manual fallback; the backend validates every scan and stores `checked_in`, `checked_in_at` and `checked_in_by` in MySQL
- Duplicate check-in prevention (row lock), wrong-event and cancelled-ticket detection
- Dashboard with attendance statistics, loading indicators, form validation, success and error notifications

Roles: `ADMIN` (everything), `GATE_STAFF` (QR check-in and dashboard), `USER` (browse, book, own tickets). Public sign-up always creates `USER`; gate staff accounts are seeded (`gate@demo.com`).

## 10. API endpoints
Full details: `backend\API_DOCUMENTATION.md`.
```
POST /api/auth/login                      POST /api/users (register)
GET  /api/events        GET /api/events/{id}        GET /api/events/{id}/summary        GET /api/events/{id}/booked-seats
POST/PUT/DELETE /api/events[/{id}]        (ADMIN)
GET  /api/venues  /api/seats              POST/PUT/DELETE, POST /api/seats/bulk (ADMIN)
POST /api/bookings    GET /api/bookings/user/{userId}    PUT /api/bookings/{id}/cancel
GET  /api/tickets/user/{userId}           QR tickets (owner or ADMIN)
POST /api/checkin                         ADMIN or GATE_STAFF
GET  /api/dashboard                       ADMIN or GATE_STAFF
GET/PUT /api/users/{id}   PUT /api/users/{id}/password
POST /api/chat                            optional AI assistant (needs GROQ_API_KEY)
```

## 11. Tests
```cmd
cd backend
mvn -Dtest=TicketServiceTest test
```
`TicketServiceTest` (6 tests, no database needed) covers: first scan succeeds, second scan is refused, unknown code, `ETS:` prefix, wrong event, cancelled booking/event, ticket ownership and QR image generation.
`mvn test` (all tests) also runs the original Eventra tests, which need MySQL running.

## 12. Troubleshooting
- **`Access denied for user 'root'`:** wrong `DB_PASSWORD` in `backend\.env`.
- **`Communications link failure` / `Connection refused`:** MySQL is not running. Start the MySQL80 service (Services app) or open Workbench to confirm.
- **`Port 4040 (or 5173) was already in use`:** close the other program, or change `PORT`/`server.port` for the backend, or run `npm run dev -- --port 5174` (then add that origin to `CORS_ALLOWED_ORIGINS` in `backend\.env`).
- **`'mvn' is not recognized` / `'npm' is not recognized`:** install it, then close and reopen the terminal.
- **Compile error about Lombok / TypeTag:** make sure you use the `pom.xml` in this project (it pins a Lombok version that supports new JDKs).
- **Frontend says "Cannot reach the server":** the backend is not running or crashed. Check terminal 1.
- **CORS error in the browser console:** open the app at `http://localhost:5173` exactly (not a different port or IP), or add your origin to `CORS_ALLOWED_ORIGINS` in `backend\.env` and restart the backend.
- **Camera does not start:** allow camera access (camera icon in the address bar). Browsers only allow cameras on `localhost` or HTTPS. Close other apps using the camera (Teams, Zoom). Use the manual ticket-code box as a fallback.
- **Flyway "checksum mismatch" error:** a migration file that was already applied to your database was edited. Never edit `V1__initial_schema.sql` or `V2__tickets.sql` after running the app; add a new `V3__...sql` instead. To start clean, drop the `event_ticketing_db` database in Workbench and start the backend again.
- **Flyway error mentioning a non-empty schema:** you pointed `DB_URL` at a database that already has tables from another project. Use a fresh database name.
- **AI chat returns an error:** it needs a Groq key in `GROQ_API_KEY`; the rest of the app does not.

## 13. Known limitations
- Not run end to end in the environment where this was packaged (no Maven or internet there). Build, run and test on your machine and report any error text.
- Booking is instant; there is no payment step.
- Camera scanning could not be tested here; use the manual code box if the camera is unavailable.
- Eventra has no organizer role; events are created by `ADMIN`.
