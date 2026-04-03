import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import '../App.css';

function Register({ onSwitchToLogin, onRegisterSuccess }) {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const { register } = useAuth();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await register(username, password, email);
      onRegisterSuccess();
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Error al crear la cuenta. Inténtalo de nuevo.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-card">
      <header className="brand-section">
        <h1 className="brand-logo">Culture<span>Log</span></h1>
        <p className="brand-tagline">Únete a nuestra comunidad cultural.</p>
      </header>

      <form className="login-form" onSubmit={handleSubmit}>
        {error && <p className="auth-error">{error}</p>}

        <div className="input-group">
          <label htmlFor="name">Nombre de usuario</label>
          <input type="text" id="name" value={username} onChange={(e) => setUsername(e.target.value)} required disabled={isLoading} minLength={3} maxLength={30} />
        </div>

        <div className="input-group">
          <label htmlFor="email">Correo electrónico</label>
          <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)} required disabled={isLoading} />
        </div>

        <div className="input-group">
          <label htmlFor="password">Crea una contraseña</label>
          <input type="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)} required disabled={isLoading} minLength={6} />
        </div>

        <button type="submit" className="login-button" disabled={isLoading}>
          {isLoading ? 'Creando cuenta...' : 'Registrarse'}
        </button>
      </form>

      <footer className="footer-section">
        <p>
          ¿Ya tienes una cuenta?{' '}
          <a href="#" className="sign-up-link" onClick={(e) => { e.preventDefault(); onSwitchToLogin(); }}>
            Inicia sesión aquí
          </a>
        </p>
      </footer>
    </div>
  );
}

export default Register;
