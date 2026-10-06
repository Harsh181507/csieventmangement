# CSI Event Management — Backend

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square)
![License](https://img.shields.io/badge/License-Unspecified-lightgrey?style=flat-square)

A Spring Boot REST API for organizing and running **hackathon-style CSI (Computer Society of India) events** — from event creation and team registration through judge assignment, scoring, and a live leaderboard.

Built with Java 21, Spring Boot 3.4, Spring Security (JWT), and PostgreSQL. Containerized with Docker and deployable to [Render](https://render.com/).

---

## Features

- **Role-based access control** — four roles (`ORGANIZER`, `STUDENT`, `VOLUNTEER`, `JUDGE`) with JWT-secured, stateless authentication
- **Event lifecycle management** — create and delete events, set a max team size, lock (and unlock) scoring
- **Team formation** — students create teams or join an existing one via a shareable join code (only visible to the team's members)
- **Judge assignment** — organizers assign judges to an event to score all teams, or limit them to specific teams
- **Custom judging criteria** — define per-event scoring criteria with configurable max scores
- **Scoring & leaderboard** — judges submit all of a team's scores in one request; the leaderboard ranks teams by average score per judge
- **Account deletion** — users can delete their own account (required by Google Play)
- **Centralized error handling** — consistent JSON error responses via a global exception handler
- **Built for bursts** — cached user lookups, batched queries and row locks so 200 users can hit it at once

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (virtual threads) |
| Framework | Spring Boot 3.4 (Web, Data JPA, Security, Validation) |
| Auth | JWT (`jjwt`) + BCrypt password hashing |
| Database | PostgreSQL (Hibernate / JPA), hosted on Supabase |
| Caching | Caffeine (in-memory) |
| Build Tool | Maven |
| Containerization | Docker (multi-stage build) |
| Deployment | Render |

---

## Project Structure

```
src/main/java/com/harsh/csieventmangement/
├── config/            # CORS configuration
├── controller/         # REST endpoints
├── dto/
│   ├── request/         # Incoming request payloads
│   └── response/        # Outgoing response payloads
├── entity/            # JPA entities (Event, Team, Score, User, ...)
├── exception/          # Custom exceptions + global exception handler
├── repository/         # Spring Data JPA repositories
├── security/            # JWT filter, entry point, user details, security config
├── service/            # Business logic
└── util/               # Enums and shared constants
src/main/resources/static/  # Privacy policy + account deletion pages
db/                         # SQL scripts to run once in Supabase
```

---

## Roles

| Role | Description |
|---|---|
| `ORGANIZER` | Creates events, assigns judges, locks scoring, manages users |
| `STUDENT` | Registers for events, creates/joins teams |
| `VOLUNTEER` | Assisting/operational role for an event (no app screens yet) |
| `JUDGE` | Scores teams against an event's judging criteria |

---

## API Overview

All endpoints are prefixed relative to the app root. `/auth/**`, `/`, `/health` and the public pages are open; every other endpoint requires a valid JWT in the `Authorization: Bearer <token>` header. Errors are JSON: `{"success": false, "message": "...", "data": null}`.

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user (always `STUDENT`) |
| POST | `/auth/login` | Log in and receive a JWT (email is case-insensitive) |

### Events
| Method | Endpoint | Description |
|---|---|---|
| POST | `/events` | Create an event (organizer) |
| GET | `/events` | List all events, newest first |
| POST | `/events/{eventId}/lock` | Lock scoring for an event (organizer) |
| POST | `/events/{eventId}/unlock` | Reopen scoring (organizer) |
| DELETE | `/events/{eventId}` | Delete an event with its teams, criteria and scores (organizer) |
| GET | `/events/judge` | List events assigned to the logged-in judge |
| POST | `/events/{eventId}/register` | Register the current user for an event |

### Teams
| Method | Endpoint | Description |
|---|---|---|
| POST | `/teams/{eventId}?teamName=` | Create a team for an event |
| GET | `/teams/event/{eventId}` | List teams for an event |
| GET | `/teams/event/{eventId}/my` | Get the current user's team for an event |
| POST | `/teams/join-by-code?code=` | Join a team using its join code |
| POST | `/teams/join/{teamId}` | Join a team by ID |
| DELETE | `/teams/{teamId}/leave` | Leave a team |

### Team Members
| Method | Endpoint | Description |
|---|---|---|
| POST | `/team-members/{teamId}/add/{userId}` | Add a member to a team (team leader only) |
| DELETE | `/team-members/{teamMemberId}` | Remove a team member (team leader only) |

### Judging Criteria
| Method | Endpoint | Description |
|---|---|---|
| POST | `/criteria` | Create judging criteria for an event (organizer) |
| GET | `/criteria/{eventId}` | Get criteria for an event |
| DELETE | `/criteria/{criteriaId}` | Delete a criterion and its scores (organizer) |

### Judge Assignment
| Method | Endpoint | Description |
|---|---|---|
| POST | `/judge-assignments` | `{eventId, judgeId, teamIds}` — set the teams a judge scores (empty = all teams) |
| GET | `/judge-assignments/event/{eventId}` | Judges on an event and their teams |
| DELETE | `/judge-assignments/event/{eventId}/judge/{judgeId}` | Remove a judge from an event |
| POST | `/assignments/event/{eventId}/judge/{judgeId}` | Assign a judge to an event |
| POST | `/assignments/team/{teamId}/judge/{judgeId}` | Assign a judge to a team |
| GET | `/judge/events` | Events assigned to the current judge |
| GET | `/judge/events/{eventId}/teams` | Teams the current judge can score for an event |

### Scoring & Leaderboard
| Method | Endpoint | Description |
|---|---|---|
| POST | `/scores/batch` | `{teamId, scores: [{criteriaId, scoreValue}]}` — save a team's scores (all or nothing) |
| POST | `/scores` | Submit a single score for a team against a criterion |
| GET | `/scores/judge?eventId=` | Scores submitted by the current judge |
| GET | `/scores/event/{eventId}/summary` | Live standings (organizer, judge) |
| GET | `/leaderboard/{eventId}` | Final top 10 for everyone, once scoring is locked |

Leaderboard score = average total per judge, so teams scored by different numbers of judges are ranked fairly. Tied teams share a rank.

### Users
| Method | Endpoint | Description |
|---|---|---|
| GET | `/users/me` | Current user's profile |
| POST | `/users/me/delete` | `{password}` — permanently delete own account |
| PUT | `/users/role` | Update a user's role (organizer) |
| GET | `/users/judges` | List all judges (organizer) |
| GET | `/users/all` | List all non-organizer users (organizer) |

### Public
| Method | Endpoint | Description |
|---|---|---|
| GET | `/health` | `{"status":"UP"}` for Render / uptime monitors |
| GET | `/privacy-policy` | Privacy policy page (for Google Play) |
| GET | `/delete-account` | Account deletion instructions (for Google Play) |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+ (or use the included `mvnw` wrapper)
- A PostgreSQL database (local or hosted, e.g. Supabase)

### 1. Clone the repository

```bash
git clone https://github.com/Harsh181507/csieventmangement.git
cd csieventmangement
```

### 2. Configure environment variables

All secrets come from environment variables only; nothing secret is committed.

| Variable | Required | Notes |
|---|---|---|
| `DB_URL` | yes | e.g. `jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:5432/postgres` |
| `DB_USERNAME` | yes | `postgres.<project-ref>` on Supabase |
| `DB_PASSWORD` | yes | Database password |
| `JWT_SECRET` | yes | Random string, at least 32 characters |
| `JWT_EXPIRATION_MS` | no | Token lifetime, default 7 days (`604800000`) |
| `DB_POOL_SIZE` | no | Max DB connections, default `10` |
| `PORT` | no | Set by Render automatically |

> **Security note:** older commits of `application.properties` contained real database credentials and a JWT secret. Rotate the Supabase database password; the JWT secret is replaced by whatever you set in `JWT_SECRET` (existing app logins will need to sign in again).

### 3. Run locally

Either export the variables above, or create `src/main/resources/application-local.properties` (git-ignored):

```properties
spring.datasource.url=jdbc:postgresql://<host>:5432/<database>
spring.datasource.username=<your-db-username>
spring.datasource.password=<your-db-password>
jwt.secret=any-local-secret-of-at-least-32-characters
```

and run with the `local` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The API will be available at `http://localhost:8080`. Point a debug build of the Android app at it with `./gradlew installDebug -PapiUrl=http://10.0.2.2:8080/` (emulator) or your PC's LAN IP (phone).

### 4. Run with Docker

```bash
docker build -t csieventmangement .
docker run -p 8080:8080 \
  -e DB_URL=<jdbc-url> \
  -e DB_USERNAME=<username> \
  -e DB_PASSWORD=<password> \
  -e JWT_SECRET=<random-32+-chars> \
  csieventmangement
```

### 5. Run tests

```bash
./mvnw test
```

(The context test connects to the database, so the environment variables above must be set.)

---

## Deployment (Render)

1. Push this repo to GitHub.
2. In Render: **New → Blueprint** and pick the repo (uses `render.yaml`, Docker runtime), or for an existing
   service set **Runtime: Docker** in Settings.
3. In the service's **Environment** tab set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
   (and `JWT_SECRET` if Render didn't generate one).
4. Deploy, then check `https://<service>.onrender.com/health` returns `{"status":"UP"}`.

### Choosing a plan (measured)

Load test: 200 users opening the app at the same instant (profile + events + teams + my team),
backend in Docker limited to each plan's CPU and 512 MB RAM:

| Plan | Startup | Median response | 95th percentile | Errors |
|---|---|---|---|---|
| Free (0.1 CPU) | ~150 s | ~20 s | ~40 s | some "server busy" |
| Starter (0.5 CPU) | ~30 s | 2–5 s | ~9 s | none |
| Standard (1 CPU) | ~20 s | 1–2 s | ~3 s | none |

200 *simultaneous logins* (password hashing is deliberately slow): ~25 s p95 on Starter,
~11 s on Standard, no errors. Tokens last 7 days, so ask participants to log in before the event.

- **Free** sleeps after 15 minutes without traffic; the next request waits for a ~2–3 minute
  cold start. Fine for development, not for event day.
- **Starter** is fine for normal use. For the event itself, upgrade to **Standard** for the day
  (billing is prorated) and downgrade afterwards.
- Keep the service in **Singapore**: the database is in Tokyo, and every query crosses that link.

### Database scripts

Run these once in Supabase (**SQL Editor → New query → Run**). Both are safe to re-run.

- `db/001_indexes.sql` — indexes on foreign keys used by every screen.
- `db/002_team_members_per_event.sql` — checks for an old unique constraint that stops a student
  joining teams in more than one event, with instructions to drop it.

### Google Play pages

- Privacy policy: `https://<service>.onrender.com/privacy-policy`
- Account deletion: `https://<service>.onrender.com/delete-account`

Use these in Play Console (App content → Privacy policy / Data safety → Account deletion).

---

## Typical Flow

1. An **organizer** registers, logs in, and creates an event with a max team size.
2. **Students** register, log in, and either create a team (getting a join code) or join one using a code.
3. The organizer assigns **judges** to the event (all teams, or specific teams) and defines **judging criteria**.
4. **Judges** log in, view their assigned events/teams, and submit scores per criterion.
5. The organizer locks scoring once judging is complete, and everyone can view the **leaderboard**.

---

## Contributing

Issues and pull requests are welcome. If you're adding a new endpoint, please follow the existing layering convention: `controller → service → repository`, with request/response DTOs in `dto/`.
