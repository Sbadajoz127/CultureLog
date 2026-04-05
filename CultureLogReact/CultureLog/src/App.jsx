import { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import Login from './LoginRegister/Login';
import Register from './LoginRegister/Register';
import ForgotPassword from './LoginRegister/ForgotPassword';
import ResetPassword from './LoginRegister/ResetPassword';
import Home from './Home';
import Profile from './Profile';
import Library from './Library';
import './App.css';

const AUTH_VIEWS = ['login', 'register', 'forgot-password', 'reset-password'];

function AppContent() {
  const { user, isAuthenticated, logout } = useAuth();
  const [currentView, setCurrentView] = useState('login');
  const [profilePic, setProfilePic] = useState(null);

  const routedView =
    isAuthenticated && AUTH_VIEWS.includes(currentView)
      ? 'home'
      : currentView;

  const handleLogout = () => {
    logout();
    setCurrentView('login');
  };

  const goHome = () => setCurrentView('home');
  const goLibrary = () => setCurrentView('library');
  const goProfile = () => setCurrentView('profile');

  return (
    <>
      {AUTH_VIEWS.includes(routedView) && (
        <div className="culturelog-login-container">
          {routedView === 'login' && (
            <Login
              onSwitchToRegister={() => setCurrentView('register')}
              onLoginSuccess={() => setCurrentView('home')}
              onForgotPassword={() => setCurrentView('forgot-password')}
            />
          )}

          {routedView === 'register' && (
            <Register
              onSwitchToLogin={() => setCurrentView('login')}
              onRegisterSuccess={() => setCurrentView('home')}
            />
          )}

          {routedView === 'forgot-password' && (
            <ForgotPassword
              onSwitchToLogin={() => setCurrentView('login')}
              onSwitchToReset={() => setCurrentView('reset-password')}
            />
          )}

          {routedView === 'reset-password' && (
            <ResetPassword
              onSwitchToLogin={() => setCurrentView('login')}
            />
          )}
        </div>
      )}

      {routedView === 'home' && isAuthenticated && (
        <Home
          userName={user.username}
          profilePic={profilePic}
          onLogout={handleLogout}
          onGoHome={goHome}
          onGoLibrary={goLibrary}
          onGoProfile={goProfile}
        />
      )}

      {routedView === 'profile' && isAuthenticated && (
        <Profile
          userName={user.username}
          profilePic={profilePic}
          setProfilePic={setProfilePic}
          onGoHome={goHome}
          onGoLibrary={goLibrary}
          onGoProfile={goProfile}
          onLogout={handleLogout}
        />
      )}

      {routedView === 'library' && isAuthenticated && (
        <Library
          userName={user.username}
          profilePic={profilePic}
          onGoHome={goHome}
          onGoLibrary={goLibrary}
          onGoProfile={goProfile}
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
