import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
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

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ThemeProvider>
          <ProfilePicProvider>
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
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </ProfilePicProvider>
        </ThemeProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
