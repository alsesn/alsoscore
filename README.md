# AlsoScore

A backend service for creating tests, running test-taking sessions, and scoring results. Spring Boot 4 (Java 21) + PostgreSQL, with a REST API and a Thymeleaf UI (admin panel and user flow).

## Stack

- Java 21, Spring Boot 4.0.0
- Spring Web MVC, Spring Data JPA, Spring Security (HTTP Basic, stateless)
- PostgreSQL
- Thymeleaf
- MapStruct, Lombok, Bean Validation

## Getting Started

### 1. Requirements
- JDK 21
- PostgreSQL (local or via Docker)

### 2. Environment setup

Create a `.env` file in the project root (picked up via `spring.config.import`):

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/alsoscore
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
```

The DB schema is created/updated automatically (`ddl-auto: update`); there are no separate migrations or a `schema.sql` in the project.

### 3. Build and run

```bash
./mvnw spring-boot:run
```

or

```bash
./mvnw clean package
java -jar target/alsoscore-0.0.1-SNAPSHOT.jar
```

The app starts on `http://localhost:8080`.

### 4. Accounts (in-memory, hardcoded in `SecurityConfig`)

| Login     | Password     | Role    |
|-----------|--------------|---------|
| `admin`   | `adminpass`  | ADMIN   |
| `creator` | `creatorpass`| CREATOR |
| `user`    | `userpass`   | USER    |

Authentication is HTTP Basic, sessions are not stored (`STATELESS`), CSRF is disabled.

## Roles and access

- **ADMIN, CREATOR** — `/api/v1/tests/**` (create/edit tests and questions, publish)
- **USER** — `/api/v1/sessions/**`, `/api/v1/reports/**` (take a test, get a report)
- All other requests require any authenticated user

## REST API

Base prefix: `/api/v1`

### Test management (ADMIN, CREATOR)

| Method | Path | Description |
|---|---|---|
| `POST` | `/tests` | Create a new test |
| `GET` | `/tests/{testId}` | Get a test by ID |
| `PUT` | `/tests/{testId}/questions` | Add a question to a test |
| `POST` | `/tests/{testId}/publish` | Publish a test (requires ≥1 question) |

### Taking a test (USER)

| Method | Path | Description |
|---|---|---|
| `POST` | `/sessions/start/{testId}` | Start a session for a published test |
| `GET` | `/sessions/{sessionId}/next-question` | Get the next unanswered question (correct answer omitted from the response). `204` if no questions remain |
| `POST` | `/sessions/{sessionId}/answer` | Submit an answer: `{"questionId": "...", "submittedAnswer": "...", "solvingLogic": "..."}` |
| `POST` | `/sessions/{sessionId}/finish` | Finish the session (triggers async result processing) |
| `GET` | `/reports/{sessionId}` | Get the detailed session report (only available after processing, status `REPORT_READY`) |

### Web UI (Thymeleaf, no `/api` prefix)

| Method | Path | Description |
|---|---|---|
| `GET` | `/admin/tests` | List tests, creation form |
| `POST` | `/admin/tests/create` | Create a test from the form |
| `GET` | `/admin/tests/{testId}/edit` | Edit a test / add questions |
| `POST` | `/admin/tests/{testId}/publish` | Publish a test |
| `GET` | `/user/start` | List published tests |
| `POST` | `/user/session/start/{testId}` | Start taking a test |
| `GET` | `/user/session/{sessionId}/question` | Show the current question |
| `POST` | `/user/session/{sessionId}/answer/{questionId}` | Submit an answer from the form |
| `GET` | `/user/session/{sessionId}/finish` | Finish the session |
| `GET` | `/user/report/{sessionId}` | Report page |

## Database schema

```
Test
├── id (PK, identity)
├── title
├── description
└── status            [DRAFT | PUBLISHED]

Question
├── id (PK, identity)
├── test_id (FK → Test.id)
├── text
├── type              [CHOICE | INPUT]
├── answer_options
├── correct_answer
└── score_points

TestSession
├── session_id (PK, UUID)
├── test_id (FK → Test.id)
├── user_id
├── start_time
├── finish_time
├── status                          [IN_PROGRESS | FINISHED | REPORT_READY]
├── total_score
├── total_time_millis
├── correct_answers_percentage
└── average_time_per_question_millis

UserAnswer
├── id (PK, identity)
├── session_id (FK → TestSession.session_id)
├── question_id (FK → Question.id)
├── submitted_answer
├── is_correct
├── time_to_solve_millis
├── question_start_time
├── answer_submit_time
└── solving_logic_data (TEXT)
```

Relationships: `Test 1—N Question`, `TestSession N—1 Test`, `UserAnswer N—1 TestSession`, `UserAnswer N—1 Question`.

## Test-taking lifecycle

1. A test is created in `DRAFT` status, filled with questions, then published (`DRAFT → PUBLISHED`).
2. A user starts a session for a published test → a `TestSession` is created with status `IN_PROGRESS`.
3. Questions are served one at a time (`getNextQuestion`), answers are stored as `UserAnswer` records.
4. On finish, the session moves to `FINISHED`, and `ReportingService` asynchronously (with a ~2s delay) computes the metrics and moves the session to `REPORT_READY`.
5. The report (`/reports/{sessionId}`) is only available once the session reaches `REPORT_READY`.

## Known limitations (as-is)

- `userId` on a session is just the `hashCode()` of the username, not a real link to a user entity.
- In `submitAnswer` (REST), the time to solve a question is hardcoded to 15 seconds instead of being derived from actual user interaction — real time tracking only exists in the web flow (`submitAnswerWithTimeMetrics`).
- In `TestSessionService.finishSession`, the status check (`status != FINISHED || status == REPORT_READY`) prevents a session from ever being finished while `IN_PROGRESS` — likely a bug worth reviewing.
- Users are hardcoded in memory (`InMemoryUserDetailsManager`); there's no proper User entity.
