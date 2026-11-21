# Task Management Web App - Backend

A robust REST API for a task management system, built with **Spring Boot 3.2.x** and **Java 17**. It features JWT-based authentication, Role-Based Access Control (RBAC), Flyway database migrations, Swagger/OpenAPI documentation, Testcontainers integration testing, and a containerized **PostgreSQL** database.

---

## 🛠 Tech Stack

- **Java 17**
- **Spring Boot 3.2.x** (Web, Data JPA, Security, Validation)
- **PostgreSQL 15** (Relational database)
- **Flyway** (Database migrations)
- **SpringDoc OpenAPI 2.3.x / Swagger UI** (API documentation)
- **JWT (JSON Web Tokens)** via `jjwt` (0.11.5)
- **Lombok & SLF4J** (Boilerplate reduction & server logging)
- **Docker & Docker Compose** (Containerized database)
- **JUnit 5, Mockito & H2** (Unit & JPA testing)
- **Testcontainers** (Real PostgreSQL container integration testing)

---

## ✨ Features & Security Constraints

- **Authentication & Security:**
  - Stateless JWT token-based authentication (`SessionCreationPolicy.STATELESS`).
  - Passwords hashed using BCrypt.
  - Startup secret validation: `JWT_SECRET` must be at least 256 bits (32+ bytes) and blocks insecure fallbacks.
- **Role-Based Access Control (RBAC):**
  - **ADMIN:** Can view, manage, reassign, and delete *all* tasks.
  - **MEMBER Visibility:** Members can view tasks assigned to them or created by them.
  - **Assignee Limitations:** Assignees who are not admins or creators can only update task `status` and `description`. Changes to `title`, `priority`, `dueDate`, or `assignedTo` are denied.
- **Task API Design (`PUT` & `PATCH`):**
  - `PUT /api/tasks/{id}` for full task updates.
  - `PATCH /api/tasks/{id}` for partial updates (e.g. updating only status or description without sending unchanged required fields).
- **Database Migrations (Flyway):**
  - `V1__init_schema.sql` establishes tables and indexes.
  - `V2__seed_initial_data.sql` executes safe idempotent seed inserts.
- **Robust Error Handling & Input Validation:**
  - `GlobalExceptionHandler` masks internal exceptions (returns generic 500 error to clients) and logs stack traces on the server.
  - Pagination validation rejects negative pages (`page < 0`) and unreasonable sizes (`size < 1` or `size > 100`) with HTTP 400 Bad Request.

---

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- Maven 3.8+
- Docker & Docker Compose

### 1. Configure the Environment
Create or edit `.env` inside `backend/` (see `.env.example`):
```properties
JWT_SECRET=this-is-a-super-secret-key-that-should-be-at-least-256-bits-long-in-production-environments-for-jwt-signing
DB_URL=jdbc:postgresql://localhost:5432/taskdb
DB_USERNAME=postgres
DB_PASSWORD=postgrespassword
DB_NAME=taskdb
DB_PORT=5432
```

### 2. Start the Database
Spin up the local PostgreSQL container using Docker Compose:
```bash
docker-compose up -d
```
*Exposes PostgreSQL on `localhost:5432` with database `taskdb`.*

### 3. Run the Application
Start the Spring Boot server using Maven:
```bash
mvn spring-boot:run
```
*The API will start on `http://localhost:8080`.*

### 4. Interactive Swagger Documentation
Open in your browser:
- [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### 5. Database Seeding (Testing Data)
Seeded with:
- **Admin**: `admin@test.com` | password: `password`
- **Member**: `member@test.com` | password: `password`

---

## 📡 API Endpoints

### Auth (`/api/auth`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Authenticate and retrieve JWT token |

### Tasks (`/api/tasks`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tasks` | Get paginated & filtered tasks (`?page=0&size=10&status=TODO&priority=HIGH`) |
| `GET` | `/api/tasks/{id}` | Get task details by ID |
| `POST` | `/api/tasks` | Create a new task (Creator can assign) |
| `PUT` | `/api/tasks/{id}` | Full update of existing task |
| `PATCH` | `/api/tasks/{id}` | Partial update of task (status / description) |
| `DELETE` | `/api/tasks/{id}` | Delete task (ADMIN/Creator only) |

---

## 🧪 Testing
Run the backend test suite:
```bash
mvn test
```
