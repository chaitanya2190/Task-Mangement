# Task Management Web App - Backend

A robust REST API for a task management system, built with Spring Boot. It features JWT-based authentication, role-based access control (RBAC), and a PostgreSQL database.

## 🛠 Tech Stack

- **Java 17**
- **Spring Boot 3.2.x** (Web, Data JPA, Security, Validation)
- **PostgreSQL** (Database)
- **JWT (JSON Web Tokens)** via `jjwt`
- **Lombok** (Boilerplate reduction)
- **Docker & Docker Compose** (Containerized DB)
- **JUnit 5 & Mockito** (Unit Testing)

## ✨ Features

- **Authentication & Security:**
  - Stateless JWT token-based authentication (`SessionCreationPolicy.STATELESS`).
  - Passwords hashed using BCrypt.
  - JWT secret loaded securely from environment variables (`.env`).
- **Role-Based Access Control (RBAC):**
  - **ADMIN:** Can view, manage, assign, and delete *all* tasks.
  - **MEMBER:** Can view, update status, and update the description of tasks assigned *only* to them.
- **Task Assignment Logic:**
  - On task creation (`POST`), the creator can assign the task to anyone.
  - On task update (`PUT`), only an `ADMIN` or the original task creator can reassign a task to someone else.
- **Robust Error Handling:**
  - Global `@RestControllerAdvice` handling 404 Not Found, 403 Forbidden, and 400 Bad Request.
  - Field-level validation messages for invalid payloads (`@NotBlank`, `@Email`, etc.).
- **Performance:**
  - Pagination and filtering (by status, priority, assignee) on the `GET /api/tasks` endpoint using Spring Data JPA `Pageable`.

## 🚀 Getting Started

### Prerequisites
- JDK 17
- Maven
- Docker (for spinning up the local PostgreSQL database)

### 1. Configure the Environment
A `.env` file is located inside the `backend/` directory to store sensitive environment variables (it is ignored by git). It contains the JWT Secret:
```properties
JWT_SECRET=this-is-a-super-secret-key-that-should-be-at-least-256-bits-long-in-production-environments-for-jwt-signing
```

### 2. Start the Database
Navigate to the `backend/` directory and spin up the local PostgreSQL instance using Docker Compose:
```bash
cd backend
docker-compose up -d
```
*This exposes PostgreSQL on `localhost:5432` with username `postgres` and password `postgrespassword`.*

### 3. Run the Application
Start the Spring Boot server using Maven:
```bash
cd backend
mvn spring-boot:run
```
*The API will start on `http://localhost:8080`.*

### 4. Database Seeding (Testing Data)
The application is configured to automatically run `data.sql` on startup, seeding the database with dummy users and tasks for immediate testing. 

**Pre-configured Users:**
- **Admin**: `admin@test.com` | password: `password`
- **Member**: `member@test.com` | password: `password`

### 5. Start the React Frontend
Open a new terminal window, navigate to the `frontend` directory, and start the Vite development server:
```bash
cd frontend
npm install
npm run dev
```
*The frontend expects the backend to be running on `http://localhost:8080`.*

## 📡 API Endpoints

### Auth
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new member |
| `POST` | `/api/auth/login` | Authenticate and retrieve JWT token |

### Tasks (Requires Bearer Token)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tasks` | Get paginated & filtered tasks `?page=0&size=10&status=TODO` |
| `GET` | `/api/tasks/{id}` | Get a specific task |
| `POST` | `/api/tasks` | Create a new task (Creator can assign) |
| `PUT` | `/api/tasks/{id}` | Update task (ADMIN/Creator can reassign) |
| `DELETE` | `/api/tasks/{id}` | Delete task (ADMIN/Creator only) |

## 🧪 Testing
The project includes a suite of unit tests for the `TaskService` validating the business logic and RBAC constraints. 
Navigate to the `backend/` directory and run the tests using:
```bash
cd backend
mvn test
```

## 📸 Screenshots & API Testing
*(Placeholder for UI screenshots and Postman collections once the frontend is built)*
