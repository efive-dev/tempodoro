# Tempodoro

**_Tempodoro_** (from _tempo_, meaning "time" in Italian and _pomodoro_ of course!) is a productivity companion designed to help you manage your focus and break sessions using the Pomodoro Technique.  
It offers a clean REST API for integration and can be used to start, stop, complete, and track your Pomodoro sessions, all backed by a robust authentication system.

The application is built using:

- **Java** with **Spring Boot** for the backend and REST API.
- **Spring Security** for authentication & authorization with JWT.
- **JPA / Hibernate** for database interaction.
- A relational database (H2 in this case).

The API is secured, but you can easily test it with tools like `curl` or Postman.
It is also set up with swagger, so it can be tested through that ui as well.

---

## Features

- **Pomodoro session management**: start, stop, complete, and delete sessions.
- **Session history filtering** by date range.
- **Customizable durations** for focus and break times.
- **Secure authentication** with JWT-based login.
- **User registration & login** endpoints.
- Relational database persistence with JPA entities.

---

## How to run

1. **Clone the repository**

```bash
git clone https://github.com/efive-dev/tempodoro.git
cd tempodoro
```

2. **Build and run the application**

```bash
./mvnw spring-boot:run
```

3. **Access the API**  
   By default, the app runs at:

```
http://localhost:8080
```

Or use the tui that can be found at [tempodoro-tui](https://github.com/efive-dev/tempodoro-tui).

---

## Database Schema

### **auth_users**

Stores registered users.
| Column | Type | Constraints |
|------------|---------|----------------------------|
| `id` | BIGINT | Primary Key, Auto-increment |
| `username` | TEXT | Not Null, Unique |
| `password` | TEXT | Not Null |

### **pomodoro_sessions**

Tracks Pomodoro focus sessions.
| Column | Type | Constraints |
|-------------------|--------------|----------------------------------------------|
| `id` | BIGINT | Primary Key, Auto-increment |
| `user_id` | BIGINT | Foreign Key → `auth_users(id)` |
| `session_duration`| INTEGER | Default 25 |
| `break_duration` | INTEGER | Default 5 |
| `status` | ENUM | ACTIVE, STOPPED, COMPLETED, PAUSED |
| `started_at` | DATETIME | Auto-set on creation |
| `stopped_at` | DATETIME | Nullable |
| `completed` | BOOLEAN | Default false |
| `completed_at` | DATETIME | Nullable |

---

## Endpoints

The API is divided into **public** and **protected** routes.

### Authentication

| Method | Path             | Description              | Auth Required |
| ------ | ---------------- | ------------------------ | ------------- |
| POST   | `/auth/register` | Register a new user      | No            |
| POST   | `/auth/login`    | Log in and get JWT token | No            |

### Pomodoro Sessions

| Method | Path                        | Description                         | Auth Required |
| ------ | --------------------------- | ----------------------------------- | ------------- |
| POST   | `/api/pomodoro/start`       | Start a new Pomodoro session        | Yes           |
| PATCH  | `/api/pomodoro/stop`        | Stop the current session            | Yes           |
| PATCH  | `/api/pomodoro/complete`    | Mark the session as completed       | Yes           |
| GET    | `/api/pomodoro/history`     | View session history (with filters) | Yes           |
| DELETE | `/api/pomodoro/{sessionId}` | Delete a session                    | Yes           |

---

## Sample curl requests

1. **Register a user**

```bash
curl -X POST http://localhost:8080/auth/register   -H "Content-Type: application/json"   -d '{"username": "user1", "password": "pass123"}'
```

2. **Login and get JWT token**

```bash
curl -X POST http://localhost:8080/auth/login   -H "Content-Type: application/json"   -d '{"username": "user1", "password": "pass123"}'
```

3. **Start a Pomodoro session**

```bash
curl -X POST http://localhost:8080/api/pomodoro/start   -H "Content-Type: application/json"   -H "Authorization: Bearer YOUR_JWT_TOKEN"   -d '{"sessionDuration": 25, "breakDuration": 5}'
```

4. **Get session history (last week)**

```bash
curl -X GET "http://localhost:8080/api/pomodoro/history?from=2025-08-01T00:00:00&to=2025-08-08T23:59:59"   -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Session Status Enum

- `ACTIVE`: Session in progress.
- `STOPPED`: Session stopped before completion.
- `COMPLETED`: Session finished successfully.
- `PAUSED`: Session temporarily halted.

---

## Testing

If you have tests set up, run:

```bash
./mvnw test
```
