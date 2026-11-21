# Task Management Web App - Backend

A robust REST API for a task management system, built with **Spring Boot 3.2.x** and **Java 17**. It features JWT-based authentication, Role-Based Access Control (RBAC), and a containerized **PostgreSQL** database.

---

## 🛠 Tech Stack

- **Java 17**
- **Spring Boot 3.2.x** (Web, Data JPA, Security, Validation)
- **PostgreSQL 15** (Relational database)
- **JWT (JSON Web Tokens)** via `jjwt` (0.11.5)
- **Lombok** (Boilerplate reduction)
- **Docker & Docker Compose** (Containerized database)
- **JUnit 5 & Mockito** (Unit & Service testing)

---

## ✨ Features

- **Authentication & Security:**
  - Stateless JWT token-based authentication (`SessionCreationPolicy.STATELESS`).
  - Passwords hashed using BCrypt.
  - JWT secret loaded securely from environment variables (`.env`).
- **Role-Based Access Control (RBAC):**
  - **ADMIN:** Can view, manage, assign, and delete *all* tasks.
  - **MEMBER:** Can view tasks, create tasks, and update description/status of assigned tasks.
- **Task Assignment Logic:**
  - On task creation (`POST`), the creator can assign the task to any user.
  - On task update (`PUT`), only an `ADMIN` or the original task creator can reassign a task to someone else.
- **Robust Error Handling:**
  - Global `@RestControllerAdvice` handling 404 Not Found, 403 Forbidden, and 400 Bad Request.
  - Field-level validation messages for invalid payloads (`@NotBlank`, `@Email`, etc.).
- **Performance:**
  - Pagination and filtering (by status, priority, assignee) on the `GET /api/tasks` endpoint using Spring Data JPA `Pageable`.

---

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- Maven 3.8+
- Docker & Docker Compose

### 1. Configure the Environment
A `.env` file is located inside the `backend/` directory to store sensitive environment variables:
```properties
JWT_SECRET=this-is-a-super-secret-key-that-should-be-at-least-256-bits-long-in-production-environments-for-jwt-signing
```

### 2. Start the Database
Spin up the local PostgreSQL container using Docker Compose:
```bash
docker-compose up -d
```
*This exposes PostgreSQL on `localhost:5432` with database `taskdb`, username `postgres`, and password `postgrespassword`.*

### 3. Run the Application
Start the Spring Boot server using Maven:
```bash
mvn spring-boot:run
```
*The API will start on `http://localhost:8080`.*

### 4. Database Seeding (Testing Data)
The application automatically executes `data.sql` on startup, seeding the database with dummy users and tasks for immediate testing.

**Pre-configured Users:**
- **Admin**: `admin@test.com` | password: `password`
- **Member**: `member@test.com` | password: `password`

---

## 📡 API Endpoints

### Auth
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Authenticate and retrieve JWT token |

### Tasks (Requires Bearer Token)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tasks` | Get paginated & filtered tasks (`?page=0&size=10&status=TODO`) |
| `GET` | `/api/tasks/{id}` | Get task details by ID |
| `POST` | `/api/tasks` | Create a new task (Creator can assign) |
| `PUT` | `/api/tasks/{id}` | Update task (ADMIN/Creator can reassign) |
| `DELETE` | `/api/tasks/{id}` | Delete task (ADMIN/Creator only) |

---

## 🧪 Testing
Run the backend test suite:
```bash
mvn test
```
