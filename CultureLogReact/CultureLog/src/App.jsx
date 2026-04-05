import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { ProfilePicProvider } from './context/ProfilePicContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './LoginRegister/Login';
import Register from './LoginRegister/Register';
import ForgotPassword from './LoginRegister/ForgotPassword';
import ResetPassword from './LoginRegister/ResetPassword';
import Home from './Home';
import Profile from './Profile';
import Library from './Library';
import PublicProfile from './PublicProfile';
import './App.css';

function AuthNavigationGuard() {
  const navigate = useNavigate();
  useEffect(() => {
    const handler = () => navigate('/login', { replace: true });
    window.addEventListener('auth:navigate-login', handler);
    return () => window.removeEventListener('auth:navigate-login', handler);
  }, [navigate]);
  return null;
}

function SmartFallback() {
  const { isAuthenticated } = useAuth();
  return <Navigate to={isAuthenticated ? '/home' : '/login'} replace />;
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ThemeProvider>
          <ProfilePicProvider>
            <AuthNavigationGuard />
            <Routes>
              {/* Public routes */}
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />
              <Route path="/reset-password" element={<ResetPassword />} />

              {/* Protected routes */}
              <Route element={<ProtectedRoute />}>
                <Route path="/home" element={<Home />} />
                <Route path="/library" element={<Library />} />
                <Route path="/profile" element={<Profile />} />
                <Route path="/user/:userId" element={<PublicProfile />} />
              </Route>

              {/* Fallback */}
              <Route path="*" element={<SmartFallback />} />
            </Routes>
          </ProfilePicProvider>
        </ThemeProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
