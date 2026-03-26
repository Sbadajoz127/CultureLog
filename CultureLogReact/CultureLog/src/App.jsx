// src/App.jsx
import { useState } from 'react';
import Login from './LoginRegister/Login';
import Register from './LoginRegister/Register';
import Home from './Home';
import Profile from './Profile'; // <-- IMPORTAMOS EL PERFIL
import './App.css';
import Portal from './Portal';

function App() {
  const [currentView, setCurrentView] = useState('login');
  const [userName, setUserName] = useState('');

  const [userEmail, setUserEmail] = useState(''); // Guardamos el correo
  // Ponemos una imagen por defecto (un círculo gris)
  const [profilePic, setProfilePic] = useState('https://www.pngarts.com/files/3/Monkey-Transparent-Background-PNG.png');
  return (
    <>
      {(currentView === 'login' || currentView === 'register') && (
        <div className="culturelog-login-container">
          {currentView === 'login' && (
            <Login 
              onSwitchToRegister={() => setCurrentView('register')} 
              onLoginSuccess={(email) => {
                setUserName(email.split('@')[0]); 
                setCurrentView('home'); 
              }} 
            />
          )}
          
          {currentView === 'register' && (
            <Register onSwitchToLogin={() => setCurrentView('login')} />
          )}
        </div>
      )}

      {currentView === 'home' && (
        <Home 
          userName={userName} 
          profilePic={profilePic}
          onLogout={() => setCurrentView('login')} 
          onGoToProfile={() => setCurrentView('profile')} // <-- NUEVO CABLE PARA IR AL PERFIL
          onGoToPortal={() => setCurrentView('portal')}
        />
      )}

      {/* NUEVA PANTALLA DE PERFIL */}
      {currentView === 'profile' && (
        <Profile 
          userName={userName}
          setUserName={setUserName}
          userEmail={userEmail}            // Pasamos el correo
          setUserEmail={setUserEmail}      // Pasamos el poder de cambiar el correo
          profilePic={profilePic}          // Pasamos la foto actual
          setProfilePic={setProfilePic}    // Pasamos el poder de cambiar la foto
          onBack={() => setCurrentView('home')} 
          onLogout={() => setCurrentView('login')}
        />
      )}

      {/* NUEVO: PANTALLA DEL PORTAL */}
      {currentView === 'portal' && (
        <Portal 
          userName={userName}
          onBack={() => setCurrentView('home')} 
          onLogout={() => setCurrentView('login')}
        />
      )}

    </>
  );
}

export default App;