import { useState, useEffect } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import Login from './LoginRegister/Login';
import Register from './LoginRegister/Register';
import ForgotPassword from './LoginRegister/ForgotPassword';
import ResetPassword from './LoginRegister/ResetPassword';
import Home from './Home';
import Profile from './Profile';
import Portal from './Portal';
import './App.css';

const AUTH_VIEWS = ['login', 'register', 'forgot-password', 'reset-password'];

function AppContent() {
  const { user, isAuthenticated, loading, logout } = useAuth();
  const [currentView, setCurrentView] = useState('login');
  const [profilePic, setProfilePic] = useState(
    'https://www.pngarts.com/files/3/Monkey-Transparent-Background-PNG.png'
  );

  useEffect(() => {
    if (!loading && isAuthenticated && AUTH_VIEWS.includes(currentView)) {
      setCurrentView('home');
    }
  }, [loading, isAuthenticated, currentView]);

  if (loading) return null;

  const handleLogout = () => {
    logout();
    setCurrentView('login');
  };

  return (
    <>
      {AUTH_VIEWS.includes(currentView) && (
        <div className="culturelog-login-container">
          {currentView === 'login' && (
            <Login
              onSwitchToRegister={() => setCurrentView('register')}
              onLoginSuccess={() => setCurrentView('home')}
              onForgotPassword={() => setCurrentView('forgot-password')}
            />
          )}

          {currentView === 'register' && (
            <Register
              onSwitchToLogin={() => setCurrentView('login')}
              onRegisterSuccess={() => setCurrentView('home')}
            />
          )}

          {currentView === 'forgot-password' && (
            <ForgotPassword
              onSwitchToLogin={() => setCurrentView('login')}
              onSwitchToReset={() => setCurrentView('reset-password')}
            />
          )}

          {currentView === 'reset-password' && (
            <ResetPassword
              onSwitchToLogin={() => setCurrentView('login')}
            />
          )}
        </div>
      )}

      {currentView === 'home' && isAuthenticated && (
        <Home
          userName={user.username}
          profilePic={profilePic}
          onLogout={handleLogout}
          onGoToProfile={() => setCurrentView('profile')}
          onGoToPortal={() => setCurrentView('portal')}
        />
      )}

      {currentView === 'profile' && isAuthenticated && (
        <Profile
          profilePic={profilePic}
          setProfilePic={setProfilePic}
          onBack={() => setCurrentView('home')}
          onLogout={handleLogout}
        />
      )}

      {currentView === 'portal' && isAuthenticated && (
        <Portal
          userName={user.username}
          onBack={() => setCurrentView('home')}
          onLogout={handleLogout}
        />
      )}
    </>
  );
}

function App() {
  return (
    <AuthProvider>
      <ThemeProvider>
        <AppContent />
      </ThemeProvider>
    </AuthProvider>
  );
}

export default App;
