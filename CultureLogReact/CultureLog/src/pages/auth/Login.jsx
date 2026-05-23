import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import './Auth.css';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [emailNotVerified, setEmailNotVerified] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setEmailNotVerified(false);
    setIsLoading(true);

    try {
      await login(username, password);
      navigate('/home');
    } catch (err) {
      const data = err.response?.data;
      if (data?.emailNotVerified) {
        setEmailNotVerified(true);
      }
      const msg =
        data?.message ||
        data?.error ||
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
          {emailNotVerified && (
            <p style={{ textAlign: 'center', marginBottom: '10px' }}>
              <a href="#" className="sign-up-link" onClick={(e) => { e.preventDefault(); navigate('/verify-email'); }}>
                Verificar mi cuenta
              </a>
            </p>
          )}

          <div className="input-group">
            <label htmlFor="username">Nombre de usuario</label>
            <input type="text" id="username" value={username} onChange={(e) => setUsername(e.target.value)} required disabled={isLoading} />
          </div>

          <div className="input-group password-group">
            <label htmlFor="password">Contraseña</label>
            <div className="input-with-button">
              <input type={showPassword ? 'text' : 'password'} id="password" value={password} onChange={(e) => setPassword(e.target.value)} required disabled={isLoading} />
              <button type="button" className="password-toggle-btn" onClick={() => setShowPassword(!showPassword)} tabIndex={-1} aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}>
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
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
