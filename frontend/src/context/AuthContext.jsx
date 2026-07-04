import React, { createContext, useState, useCallback, useMemo } from 'react';
import api, { setAuthToken } from '../services/api';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);

  // useCallback is used to memoize these functions so they don't cause 
  // unnecessary re-renders of components consuming the AuthContext.
  
  const login = useCallback(async (email, password) => {
    // Make actual API call (we will wire this up fully later, for now we will assume it returns token/user)
    // Actually, let's wire it up since the backend is ready.
    // Wait, the backend login returns { token }. How do we get the user details?
    // We could decode the JWT, or the backend should return it. 
    // The instructions say: "POST /api/auth/login -> returns { token, user }".
    // Wait, the backend currently only returns { token } according to our implementation (AuthResponse).
    // Let's decode the JWT on the client side to get the email/role, or we can fetch a /me endpoint. 
    // For now, let's just use the api.
    const response = await api.post('/auth/login', { email, password });
    const receivedToken = response.data.token;
    
    // Decode JWT payload (simple base64 decode for the payload part)
    const payload = JSON.parse(atob(receivedToken.split('.')[1]));
    
    const loggedInUser = {
      email: payload.sub,
      role: payload.role || 'MEMBER', // Fallback if role isn't in token payload
      // Other details could be parsed here
    };

    setToken(receivedToken);
    setUser(loggedInUser);
    setAuthToken(receivedToken); // Update axios interceptor
  }, []);

  const register = useCallback(async (name, email, password) => {
    const response = await api.post('/auth/register', { name, email, password });
    const receivedToken = response.data.token;
    
    const payload = JSON.parse(atob(receivedToken.split('.')[1]));
    const registeredUser = {
      name,
      email: payload.sub,
      role: 'MEMBER'
    };

    setToken(receivedToken);
    setUser(registeredUser);
    setAuthToken(receivedToken);
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
    setAuthToken(null);
  }, []);

  // useMemo ensures the context value object is only recreated when user or token changes,
  // preventing consumers from re-rendering on every AuthProvider re-render.
  const value = useMemo(() => ({
    user,
    token,
    login,
    logout,
    register
  }), [user, token, login, logout, register]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
