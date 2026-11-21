# Full-Stack Task Management Web Application

A production-ready full-stack task management web application built with a **Spring Boot** REST API backend and a modern **React (Vite)** frontend. It features JWT-based authentication, Role-Based Access Control (RBAC), server-side pagination & filtering, Swagger/OpenAPI documentation, Flyway database migrations, integration testing with Testcontainers, and a responsive user dashboard.

---

## 🏗️ Project Architecture & Structure

```
jv-project/
├── backend/                  # Spring Boot REST API
│   ├── src/
│   │   ├── main/java/com/taskapp/
│   │   │   ├── config/       # OpenAPI & Swagger Configuration
│   │   │   ├── controller/   # REST Controllers (Auth, Task)
│   │   │   ├── dto/          # Request & Response DTOs (TaskRequest, TaskPatchRequest)
│   │   │   ├── exception/    # Global Exception Handling & Logging
│   │   │   ├── model/        # JPA Entities (User, Task, Enums)
│   │   │   ├── repository/   # Spring Data JPA Repositories
│   │   │   ├── security/     # JWT Filter, UserDetailsService, SecurityConfig
│   │   │   └── service/      # Business Logic (AuthService, TaskService)
│   │   ├── main/resources/
│   │   │   ├── db/migration/ # Flyway SQL Migration Scripts (V1, V2)
│   │   │   └── application.properties
│   │   └── test/             # JUnit 5, Mockito, JPA & Testcontainers test suite
│   ├── docker-compose.yml    # PostgreSQL container configuration
│   ├── pom.xml               # Maven dependencies and build setup
│   ├── .env                  # Local environment configuration
│   └── .env.example          # Template for environment configuration
│
├── frontend/                 # React 19 + Vite Single-Page Application
│   ├── src/
│   │   ├── components/       # Reusable UI (Navbar, TaskCard, TaskForm, FilterBar, Modal, Pagination)
│   │   ├── context/          # React Contexts (AuthContext, TaskContext)
│   │   ├── hooks/            # Custom Hooks (useAuth, useTasks, useFetch)
│   │   ├── pages/            # View Pages (Dashboard, Login, Register)
│   │   ├── services/         # Axios instance with VITE_API_BASE_URL & interceptors
│   │   └── styles/           # CSS design system & utility classes
│   ├── .env                  # Local frontend environment config
│   ├── .env.example          # Template for frontend environment config
│   ├── package.json          # Frontend scripts & npm dependencies
│   └── vite.config.js        # Vite configuration
│
├── .gitignore                # Root gitignore (protects secrets, builds, dependencies)
└── README.md                 # Full-stack project documentation
```

---

## 🛠 Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.x** (Spring Web, Spring Security, Spring Data JPA, Spring Validation)
- **PostgreSQL 15** (via Docker Compose)
- **Flyway** (Versioned database migrations)
- **SpringDoc OpenAPI 2.3.x / Swagger UI** (Interactive API documentation)
- **JWT (JSON Web Tokens)** via `jjwt` (0.11.5)
- **Lombok & SLF4J** (Boilerplate reduction & server-side logging)
- **JUnit 5, Mockito & H2** (Unit & JPA testing)
- **Testcontainers** (PostgreSQL container integration testing)

### Frontend
- **React 19**
- **Vite** (Next-generation frontend tooling with environment variables)
- **React Router DOM v7** (Client-side routing & route guards)
- **Axios** (Configured HTTP client with JWT request interceptors)
- **Modern CSS** (Design tokens, CSS variables, utility classes, and responsive layout)
- **Oxlint** (High-performance code linting)

---

## ✨ Key Features & Security Design

### 🔐 Authentication & Secret Management
- **Stateless JWT Authentication:** Secure login and registration with token-based session management.
- **Fail-Fast Secret Validation:** `JwtService` enforces that `JWT_SECRET` is at least 256 bits (32+ bytes) and blocks insecure fallback keys on startup.
- **Client-Side Token Handling:** In-memory token storage with automatic `Authorization: Bearer <token>` injection via Axios interceptors.
- **Protected Routing:** Unauthenticated users are redirected to `/login`, while authenticated sessions navigate to `/`.

### 👥 Role-Based Access Control (RBAC) & Permissions
- **`ADMIN`:** Full privileges to view, create, edit, reassign, and delete any task across the workspace.
- **`MEMBER` Visibility:** Members can view all tasks **assigned to them** OR **created by them**.
- **Assignee Permission Restrictions:** When a task is assigned to a member who is neither an admin nor the creator:
  - Allowed: Updating task `status` and `description`.
  - Restricted: Changing `title`, `priority`, `dueDate`, or `assignedTo` is forbidden and rejected with HTTP 403 Forbidden.
- **Reassignment & Deletion:** Restricted strictly to `ADMIN` and the original task creator.

### 📝 Partial Updates (`PATCH` API Design)
- Dedicated `PATCH /api/tasks/{id}` endpoint accepting `TaskPatchRequest`.
- Assignees can conveniently update only `status` or `description` without being forced to send existing unchanged required fields like `title` and `priority`.

### 🗄️ Database Migrations with Flyway
- Schema changes and seed records are managed via Flyway migration scripts in `backend/src/main/resources/db/migration/`:
  - `V1__init_schema.sql`: Sets up tables (`users`, `tasks`), foreign keys, and indexes.
  - `V2__seed_initial_data.sql`: Idempotent seeding using `ON CONFLICT (email) DO NOTHING` and `NOT EXISTS`.
- Production-grade schema validation (`spring.jpa.hibernate.ddl-auto=validate`).

### 🛡️ Defensive Error Handling & Input Validation
- **Exception Masking:** Unhandled internal server exceptions return generic error responses (`500 Internal Server Error`) to clients while logging full stack traces on the server.
- **Pagination Validation:** Endpoint parameters are strictly validated:
  - `page` must be `>= 0` (negative page index returns HTTP 400 Bad Request).
  - `size` must be between `1` and `100` (prevents memory exhaustion and returns HTTP 400 Bad Request).

---

## 🚀 Getting Started

### Prerequisites
Make sure you have the following installed on your machine:
- **JDK 17+**
- **Maven 3.8+**
- **Node.js 18+** & **npm**
- **Docker & Docker Compose** (Optional: local PostgreSQL instance can also be used)

---

### Step 1: Backend Configuration

1. Set up your `.env` file in `backend/` (refer to `backend/.env.example`):
   ```properties
   # JWT Configuration (minimum 256 bits / 32 characters)
   JWT_SECRET=this-is-a-super-secret-key-that-should-be-at-least-256-bits-long-in-production-environments-for-jwt-signing

   # PostgreSQL Database Configuration
   DB_URL=jdbc:postgresql://localhost:5432/taskdb
   DB_USERNAME=postgres
   DB_PASSWORD=postgrespassword
   DB_NAME=taskdb
   DB_PORT=5432
   ```

2. Start the PostgreSQL database container:
   ```bash
   cd backend
   docker-compose up -d
   ```
   *PostgreSQL will be running on `localhost:5432` with database `taskdb`.*

3. Run the Spring Boot backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   *Flyway will automatically run migrations and start the backend on `http://localhost:8080`.*

---

### Step 2: Database Seeding & Default Credentials

Flyway automatically seeds the initial test users and tasks:

| Role | Email | Password |
| :--- | :--- | :--- |
| **Admin** | `admin@test.com` | `password` |
| **Member** | `member@test.com` | `password` |

---

### Step 3: Frontend Setup

Open a new terminal window:

1. Configure `.env` in `frontend/` (refer to `frontend/.env.example`):
   ```properties
   VITE_API_BASE_URL=http://localhost:8080/api
   ```

2. Install dependencies:
   ```bash
   cd frontend
   npm install
   ```

3. Start the Vite development server:
   ```bash
   npm run dev
   ```
   *The application will open on `http://localhost:5173`.*

---

## 📖 Swagger / OpenAPI Documentation

Interactive Swagger documentation is available once the backend is started:
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON Docs:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 📡 REST API Endpoints

### Authentication (`/api/auth`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user account | Public |
| `POST` | `/api/auth/login` | Log in and receive a JWT Bearer token | Public |

### Tasks (`/api/tasks`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/tasks` | Get paginated tasks (`?page=0&size=10&status=TODO&priority=HIGH&assignedToId=1`) | Authenticated |
| `GET` | `/api/tasks/{id}` | Retrieve details for a specific task | Assignee, Creator, or Admin |
| `POST` | `/api/tasks` | Create a new task (creator can assign) | Authenticated |
| `PUT` | `/api/tasks/{id}` | Full update of existing task | Assignee (status/desc), Creator/Admin (all fields) |
| `PATCH` | `/api/tasks/{id}` | Partial update of task (e.g. status or description only) | Assignee (status/desc), Creator/Admin (all fields) |
| `DELETE` | `/api/tasks/{id}` | Delete a task | Admin or Creator |

---

## 🧪 Testing & Quality Checks

### Backend Unit & Integration Tests
Run the complete test suite (unit, JPA integration, and Testcontainers):
```bash
cd backend
mvn test
```

### Frontend Lint & Build
Run the frontend linter and production build:
```bash
cd frontend
npm run lint
npm run build
```
