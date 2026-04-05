import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../App.css';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await login(username, password);
      navigate('/home');
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Error al iniciar sesión. Inténtalo de nuevo.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="culturelog-login-container">
      <div className="login-card">
        <header className="brand-section">
          <h1 className="brand-logo">Culture<span>Log</span></h1>
          <p className="brand-tagline">Conectando historias, compartiendo cultura.</p>
        </header>

        <form className="login-form" onSubmit={handleSubmit}>
          {error && <p className="auth-error">{error}</p>}

          <div className="input-group">
            <label htmlFor="username">Nombre de usuario</label>
            <input type="text" id="username" value={username} onChange={(e) => setUsername(e.target.value)} required disabled={isLoading} />
          </div>

          <div className="input-group">
            <label htmlFor="password">Contraseña</label>
            <input type="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)} required disabled={isLoading} />
          </div>

          <button type="submit" className="login-button" disabled={isLoading}>
            {isLoading ? 'Cargando...' : 'Iniciar sesión'}
          </button>
        </form>

        <footer className="footer-section">
          <p>
            <a href="#" className="sign-up-link" onClick={(e) => { e.preventDefault(); navigate('/forgot-password'); }}>
              ¿Olvidaste tu contraseña?
            </a>
          </p>
          <p>
            ¿Eres nuevo en CultureLog?{' '}
            <a href="#" className="sign-up-link" onClick={(e) => { e.preventDefault(); navigate('/register'); }}>
              Regístrate gratis
            </a>
          </p>
        </footer>
      </div>
    </div>
  );
}

export default Login;
