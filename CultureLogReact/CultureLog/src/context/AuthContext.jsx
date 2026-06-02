import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { loginUser as apiLogin, getUserProfile } from '../services/api';

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
      profilePictureUrl: data.profilePictureUrl ?? null,
      bannerUrl: data.bannerUrl ?? null,
      role: data.role ?? 'USER',
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

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  useEffect(() => {
    const handleExpired = () => {
      logout();
      window.dispatchEvent(new Event('auth:navigate-login'));
    };
    window.addEventListener('auth:expired', handleExpired);
    return () => window.removeEventListener('auth:expired', handleExpired);
  }, [logout]);

  useEffect(() => {
    if (user?.username) {
      getUserProfile(user.username)
        .then((res) => {
          if (res.data?.bannerUrl && res.data.bannerUrl !== user.bannerUrl) {
            const updatedUser = { ...user, bannerUrl: res.data.bannerUrl };
            setUser(updatedUser);
            localStorage.setItem('user', JSON.stringify(updatedUser));
          }
        })
        .catch(() => {
          toast.error('Error al refrescar el perfil. Intenta refrescar la página.');
        });
    }
  }, []);

  const value = {
    user,
    isAuthenticated: !!user,
    isAdmin: user?.role === 'ADMIN',
    login,
    logout,
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
