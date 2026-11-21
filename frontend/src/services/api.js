import axios from 'axios';

// Create a configured Axios instance using Vite environment variable
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// A variable to hold the in-memory token
let inMemoryToken = null;

// Function to update the token from AuthContext
export const setAuthToken = (token) => {
  inMemoryToken = token;
};

// Interceptor to attach the token to every request
api.interceptors.request.use((config) => {
  if (inMemoryToken) {
    config.headers.Authorization = `Bearer ${inMemoryToken}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

export default api;
