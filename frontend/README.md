# Task Management Web App - Frontend

A modern single-page application (SPA) built with **React 19** and **Vite** that interfaces with the Spring Boot Task Management REST API.

---

## 🛠 Tech Stack

- **React 19** (Functional components, Hooks, Context API)
- **Vite** (Fast dev server and optimized Rollup bundler)
- **React Router DOM v7** (Client-side routing and protected routes)
- **Axios** (API communication with authorization interceptors)
- **Custom CSS Design System** (Variables, responsive layout, reusable utility classes)
- **Oxlint** (High-speed JavaScript/React linter)

---

## 📁 Frontend Architecture

```
frontend/src/
├── assets/         # Static assets and icons
├── components/     # Reusable UI components
│   ├── FilterBar.jsx       # Status, priority, and assignee filter controls
│   ├── Modal.jsx           # Accessible dialog backdrop & modal container
│   ├── Navbar.jsx          # Top navigation bar with user profile & logout
│   ├── Pagination.jsx      # Page navigation buttons and page counter
│   ├── ProtectedRoute.jsx  # Route guard redirecting unauthenticated users
│   ├── TaskCard.jsx        # Individual task card with status/priority badges
│   └── TaskForm.jsx        # Create/Edit task form with RBAC validations
├── context/        # React context providers
│   ├── AuthContext.jsx     # Authentication state, login, register, token handling
│   └── TaskContext.jsx     # Task CRUD operations & shared state
├── hooks/          # Custom hooks
│   ├── useAuth.js          # Hook to access AuthContext
│   ├── useFetch.js         # Generic data fetch hook
│   └── useTasks.js         # Hook for task pagination, filtering, and auto-fetch
├── pages/          # Application views
│   ├── Dashboard.jsx       # Main task board with filters, grid, and pagination
│   ├── Login.jsx           # User login view
│   └── Register.jsx        # User registration view
├── services/       # Network layer
│   └── api.js              # Axios instance configured with base URL & JWT interceptor
├── styles/         # Global styles and design system
│   └── index.css           # CSS variables, typography, buttons, and utility classes
├── App.jsx         # App router and provider hierarchy
└── main.jsx        # DOM mount point
```

---

## ⚡ Features

- **JWT Authentication:** In-memory token management with automatic Bearer token injection on outgoing requests.
- **Protected Routing:** Route guards preventing unauthenticated access to the dashboard.
- **Role-Aware UI:** Form controls and action buttons (such as delete and reassign) automatically adapt based on whether the logged-in user is an `ADMIN`, the task creator, or the assignee.
- **Server-Side Filtering & Pagination:** Instant filtering by status (`TODO`, `IN_PROGRESS`, `DONE`), priority (`LOW`, `MEDIUM`, `HIGH`), and assignee ID with synchronized pagination.
- **Loading Skeletons:** Animated pulse placeholder cards during API data fetches.

---

## 🚀 Available Scripts

In the `frontend` directory, you can run:

### `npm install`
Installs all project dependencies.

### `npm run dev`
Runs the app in development mode with Hot Module Replacement (HMR).  
Open [http://localhost:5173](http://localhost:5173) in your browser.

### `npm run build`
Bundles the application for production to the `dist/` directory.

### `npm run preview`
Locally previews the production build.

### `npm run lint`
Runs Oxlint across the codebase to ensure code quality.

---

## ⚙️ Configuration

The frontend connects to the backend API at `http://localhost:8080/api` via `src/services/api.js`. Ensure the backend server is running before launching the frontend.
