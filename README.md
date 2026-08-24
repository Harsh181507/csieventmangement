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
- **Event lifecycle management** — create events, set a max team size, and lock scoring once judging begins
- **Team formation** — students create teams or join an existing one via a shareable join code
- **Judge assignment** — organizers assign judges to events or specific teams
- **Custom judging criteria** — define per-event scoring criteria with configurable max scores
- **Scoring & leaderboard** — judges submit scores per criterion; a live leaderboard ranks teams per event
- **Centralized error handling** — consistent JSON error responses via a global exception handler
- **API documentation** — Swagger / OpenAPI integration for interactive endpoint exploration

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4 (Web, Data JPA, Security, Validation) |
| Auth | JWT (`jjwt`) + BCrypt password hashing |
| Database | PostgreSQL (Hibernate / JPA) |
| API Docs | Swagger (springdoc-openapi) |
| Build Tool | Maven |
| Containerization | Docker (multi-stage build) |
| Deployment | Render |

---

## Project Structure

```
src/main/java/com/harsh/csieventmangement/
├── config/            # CORS and Swagger configuration
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
```

---

## Roles

| Role | Description |
|---|---|
| `ORGANIZER` | Creates events, assigns judges, locks scoring, manages users |
| `STUDENT` | Registers for events, creates/joins teams |
| `VOLUNTEER` | Assisting/operational role for an event |
| `JUDGE` | Scores teams against an event's judging criteria |

---

## API Overview

All endpoints are prefixed relative to the app root. `/auth/**` and `/` (health check) are public; every other endpoint requires a valid JWT in the `Authorization: Bearer <token>` header.

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Log in and receive a JWT |

### Events
| Method | Endpoint | Description |
|---|---|---|
| POST | `/events` | Create an event |
| GET | `/events` | List all events |
| POST | `/events/{eventId}/lock` | Lock scoring for an event |
| GET | `/events/judge` | List events assigned to the logged-in judge |
| POST | `/events/{eventId}/register` | Register the current user for an event |

### Teams
| Method | Endpoint | Description |
|---|---|---|
| POST | `/teams/{eventId}` | Create a team for an event |
| GET | `/teams/event/{eventId}` | List teams for an event |
| GET | `/teams/event/{eventId}/my` | Get the current user's team for an event |
| POST | `/teams/join-by-code` | Join a team using its join code |
| POST | `/teams/join/{teamId}` | Join a team by ID |
| DELETE | `/teams/{teamId}/leave` | Leave a team |

### Team Members
| Method | Endpoint | Description |
|---|---|---|
| POST | `/team-members/{teamId}/add/{userId}` | Add a member to a team |
| DELETE | `/team-members/{teamMemberId}` | Remove a team member |

### Judging Criteria
| Method | Endpoint | Description |
|---|---|---|
| POST | `/criteria` | Create judging criteria for an event |
| GET | `/criteria/{eventId}` | Get criteria for an event |

### Judge Assignment
| Method | Endpoint | Description |
|---|---|---|
| POST | `/assignments/event/{eventId}/judge/{judgeId}` | Assign a judge to an event |
| POST | `/assignments/team/{teamId}/judge/{judgeId}` | Assign a judge to a team |
| POST | `/judge-assignments` | Create a judge assignment |
| GET | `/judge/events` | Events assigned to the current judge |
| GET | `/judge/events/{eventId}/teams` | Teams the current judge can score for an event |

### Scoring & Leaderboard
| Method | Endpoint | Description |
|---|---|---|
| POST | `/scores` | Submit a score for a team against a criterion |
| GET | `/scores/judge` | Scores submitted by the current judge |
| GET | `/scores/event/{eventId}/summary` | Score summary for an event |
| GET | `/leaderboard/{eventId}` | Ranked leaderboard for an event |

### Users
| Method | Endpoint | Description |
|---|---|---|
| PUT | `/users/role` | Update a user's role |
| GET | `/users/judges` | List all judges |
| GET | `/users/all` | List all users |

> Interactive documentation is available at `/swagger-ui.html` once the app is running.

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

The app reads its database credentials and JWT secret from environment variables (with local fallbacks in `application.properties`). Set the following before running:

```bash
export DB_URL=jdbc:postgresql://<host>:5432/<database>
export DB_USERNAME=<your-db-username>
export DB_PASSWORD=<your-db-password>
```

> **Security note:** `application.properties` currently contains real fallback values for the database credentials and JWT secret. Before making this repository public (or continuing to use it as-is), rotate those credentials and move them out of version control — for example into environment variables only, with placeholder/blank defaults committed to git. A `.env` file (git-ignored) or your deployment platform's secret manager is a good place for the real values.

### 3. Run locally

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### 4. Run with Docker

```bash
docker build -t csieventmangement .
docker run -p 8080:8080 \
  -e DB_URL=<jdbc-url> \
  -e DB_USERNAME=<username> \
  -e DB_PASSWORD=<password> \
  csieventmangement
```

### 5. Run tests

```bash
./mvnw test
```

---

## Deployment

This project includes a `render.yaml` for one-click deployment on [Render](https://render.com/) as a Java web service, and a multi-stage `Dockerfile` for deploying anywhere that supports containers.

---

## Typical Flow

1. An **organizer** registers, logs in, and creates an event with a max team size.
2. **Students** register, log in, and either create a team (getting a join code) or join one using a code.
3. The organizer assigns **judges** to the event (or specific teams) and defines **judging criteria**.
4. **Judges** log in, view their assigned events/teams, and submit scores per criterion.
5. The organizer locks scoring once judging is complete, and everyone can view the **leaderboard**.

---

## Contributing

Issues and pull requests are welcome. If you're adding a new endpoint, please follow the existing layering convention: `controller → service → repository`, with request/response DTOs in `dto/`.
