# Full-Stack Task Management Web Application

A full-stack task management web application built with a **Spring Boot** REST API backend and a modern **React (Vite)** frontend. It features JWT-based authentication, Role-Based Access Control (RBAC), server-side pagination/filtering, and a responsive user dashboard.

---

## 🏗️ Project Architecture & Structure

```
jv-project/
├── backend/                  # Spring Boot REST API
│   ├── src/
│   │   ├── main/java/com/taskapp/
│   │   │   ├── controller/   # REST Controllers (Auth, Task)
│   │   │   ├── dto/          # Request & Response DTOs
│   │   │   ├── exception/    # Global Exception Handling
│   │   │   ├── model/        # JPA Entities (User, Task, Enums)
│   │   │   ├── repository/   # Spring Data JPA Repositories
│   │   │   ├── security/     # JWT Filter, UserDetailsService, SecurityConfig
│   │   │   └── service/      # Business Logic (AuthService, TaskService)
│   │   └── resources/        # application.properties & data.sql seed
│   ├── docker-compose.yml    # PostgreSQL container configuration
│   ├── pom.xml               # Maven dependencies and build setup
│   └── .env                  # Backend environment secrets (JWT secret)
│
├── frontend/                 # React 19 + Vite Single-Page Application
│   ├── src/
│   │   ├── components/       # Reusable UI (Navbar, TaskCard, TaskForm, FilterBar, Modal, Pagination)
│   │   ├── context/          # React Contexts (AuthContext, TaskContext)
│   │   ├── hooks/            # Custom Hooks (useAuth, useTasks, useFetch)
│   │   ├── pages/            # View Pages (Dashboard, Login, Register)
│   │   ├── services/         # Axios instance & interceptors (api.js)
│   │   └── styles/           # CSS design system & utility classes
│   ├── package.json          # Frontend scripts & npm dependencies
│   └── vite.config.js        # Vite configuration
│
└── README.md                 # Root project documentation
```

---

## 🛠 Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.x** (Spring Web, Spring Security, Spring Data JPA, Spring Validation)
- **PostgreSQL 15** (via Docker Compose)
- **JWT (JSON Web Tokens)** via `jjwt` (0.11.5)
- **Lombok** (Boilerplate reduction)
- **Docker & Docker Compose** (Containerized database)
- **JUnit 5 & Mockito** (Unit & Service testing)

### Frontend
- **React 19**
- **Vite** (Next-generation frontend tooling)
- **React Router DOM v7** (Client-side routing & route guards)
- **Axios** (HTTP client with JWT request interceptors)
- **Modern CSS** (Design tokens, CSS variables, utility classes, and responsive layout)
- **Oxlint** (High-performance code linting)

---

## ✨ Key Features

### 🔐 Authentication & Security
- **Stateless JWT Authentication:** Secure login and registration with token-based session management.
- **Password Hashing:** Passwords hashed securely using BCrypt.
- **Client-Side Token Handling:** In-memory token storage with automatic `Authorization: Bearer <token>` injection via Axios interceptors.
- **Protected Routing:** Unauthenticated users are redirected to `/login`, while authenticated sessions navigate to `/`.

### 👥 Role-Based Access Control (RBAC)
- **`ADMIN`:** Full privileges to view, create, edit, reassign, and delete any task across the workspace.
- **`MEMBER`:** Can view tasks, create tasks, and update tasks. If a task is assigned to a member, they can update its status and description; reassignment is restricted to admins and the task creator.

### 📋 Task Management & Workflow
- **Task Lifecycle:** Manage tasks with title, description, status (`TODO`, `IN_PROGRESS`, `DONE`), priority (`LOW`, `MEDIUM`, `HIGH`), due date, and assignee.
- **Filtering & Pagination:** Filter tasks dynamically by status, priority, or assignee ID with server-side pagination (`Pageable`).
- **Interactive UI:** Dynamic modal forms for creating and editing tasks, deletion confirmation, status badges, and loading skeleton states.

---

## 🚀 Getting Started

### Prerequisites
Make sure you have the following installed on your machine:
- **JDK 17+**
- **Maven 3.8+**
- **Node.js 18+** & **npm**
- **Docker & Docker Compose**

---

### Step 1: Backend Configuration

1. Check the `.env` file inside the `backend/` folder:
   ```properties
   JWT_SECRET=this-is-a-super-secret-key-that-should-be-at-least-256-bits-long-in-production-environments-for-jwt-signing
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
   *The backend REST API starts on `http://localhost:8080`.*

---

### Step 2: Database Seeding & Default Credentials

On startup, `backend/src/main/resources/data.sql` seeds the database with initial users and tasks for immediate testing:

| Role | Email | Password |
| :--- | :--- | :--- |
| **Admin** | `admin@test.com` | `password` |
| **Member** | `member@test.com` | `password` |

---

### Step 3: Frontend Setup

Open a new terminal window:

1. Navigate to the `frontend/` directory and install dependencies:
   ```bash
   cd frontend
   npm install
   ```

2. Start the Vite development server:
   ```bash
   npm run dev
   ```
   *The application will open on `http://localhost:5173` (or the port displayed in your terminal).*

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
| `GET` | `/api/tasks` | Get paginated tasks (supports `?page=0&size=10&status=TODO&priority=HIGH&assignedToId=1`) | Authenticated |
| `GET` | `/api/tasks/{id}` | Retrieve details for a specific task | Authenticated |
| `POST` | `/api/tasks` | Create a new task (creator can assign) | Authenticated |
| `PUT` | `/api/tasks/{id}` | Update task details (Admin / Creator can reassign; Assignee can update status/desc) | Authenticated |
| `DELETE` | `/api/tasks/{id}` | Delete a task | Admin or Creator |

---

## 🧪 Testing & Quality Checks

### Backend Unit Tests
Run the Spring Boot / Mockito unit test suite:
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
