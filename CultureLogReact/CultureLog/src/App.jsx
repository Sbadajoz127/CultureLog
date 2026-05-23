import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { ProfilePicProvider } from './context/ProfilePicContext';
import { NotificationProvider } from './context/NotificationContext';
import { ConfirmProvider } from './context/ConfirmContext';
import { ErrorBoundary } from './components/ErrorBoundary';
import ProtectedRoute from './components/ProtectedRoute';
import ProtectedAdminRoute from './components/ProtectedAdminRoute';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ForgotPassword from './pages/auth/ForgotPassword';
import ResetPassword from './pages/auth/ResetPassword';
import VerifyEmail from './pages/auth/VerifyEmail';
import Home from './pages/Home';
import Profile from './pages/Profile';
import Library from './pages/Library';
import PublicProfile from './pages/PublicProfile';
import Notifications from './pages/Notifications';
import PostDetail from './pages/PostDetail';
import CreatePost from './pages/CreatePost';
import SearchPosts from './pages/SearchPosts';
import SearchUsers from './pages/SearchUsers';
import AdminDashboard from './pages/AdminDashboard';
import ThemedToaster from './components/ThemedToaster';
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
    <BrowserRouter basename="/CultureLog">
      <AuthProvider>
        <ThemeProvider>
          <ProfilePicProvider>
            <NotificationProvider>
              <ConfirmProvider>
                <AuthNavigationGuard />
                <ThemedToaster />
                <ErrorBoundary>
                  <Routes>
                    {/* Public routes */}
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/forgot-password" element={<ForgotPassword />} />
                    <Route path="/reset-password" element={<ResetPassword />} />
                    <Route path="/verify-email" element={<VerifyEmail />} />

                    {/* Protected routes */}
                    <Route element={<ProtectedRoute />}>
                      <Route path="/home" element={<Home />} />
                      <Route path="/library" element={<Library />} />
                      <Route path="/profile" element={<Profile />} />
                      <Route path="/user/:username" element={<PublicProfile />} />
                      <Route path="/notifications" element={<Notifications />} />
                      <Route path="/posts/create" element={<CreatePost />} />
                      <Route path="/posts/:postId" element={<PostDetail />} />
                      <Route path="/search" element={<SearchPosts />} />
                      <Route path="/search/users" element={<SearchUsers />} />
                    </Route>

                    {/* Admin routes */}
                    <Route element={<ProtectedAdminRoute />}>
                      <Route path="/admin" element={<AdminDashboard />} />
                    </Route>

                    {/* Fallback */}
                    <Route path="*" element={<SmartFallback />} />
                  </Routes>
                </ErrorBoundary>
              </ConfirmProvider>
            </NotificationProvider>
          </ProfilePicProvider>
        </ThemeProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
