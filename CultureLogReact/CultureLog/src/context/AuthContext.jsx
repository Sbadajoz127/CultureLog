import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { loginUser as apiLogin, registerUser as apiRegister } from '../services/api';

const AuthContext = createContext(null);

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

function parseJwtPayload(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  } catch {
    return null;
  }
}

function isTokenExpired(token) {
  const payload = parseJwtPayload(token);
  if (!payload?.exp) return true;
  return Date.now() >= payload.exp * 1000;
}

function readUserFromStorage() {
  const token = localStorage.getItem('token');
  const stored = localStorage.getItem('user');
  if (token && stored && !isTokenExpired(token)) {
    try {
      return JSON.parse(stored);
    } catch {
      /* fall through */
    }
  }
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  return null;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readUserFromStorage);

  const saveSession = useCallback((data) => {
    const userData = {
      id: data.id,
      username: data.username,
      email: data.email,
    };
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  }, []);

  const login = useCallback(async (username, password) => {
    const { data } = await apiLogin(username, password);
    saveSession(data);
    return data;
  }, [saveSession]);

  const register = useCallback(async (username, password, email) => {
    const { data } = await apiRegister(username, password, email);
    saveSession(data);
    return data;
  }, [saveSession]);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  useEffect(() => {
    const handleExpired = () => logout();
    window.addEventListener('auth:expired', handleExpired);
    return () => window.removeEventListener('auth:expired', handleExpired);
  }, [logout]);

  const value = {
    user,
    isAuthenticated: !!user,
    login,
    register,
    logout,
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
