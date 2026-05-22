import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import { registerUser } from '../../services/api';
import '../../App.css';

function Register() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [registered, setRegistered] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await registerUser(username, password, email);
      setRegistered(true);
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
    <div className="culturelog-login-container">
      <div className="login-card">
        <header className="brand-section">
          <h1 className="brand-logo">Culture<span>Log</span></h1>
          <p className="brand-tagline">Únete a nuestra comunidad cultural.</p>
        </header>

        {!registered ? (
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

            <div className="input-group password-group">
              <label htmlFor="password">Crea una contraseña</label>
              <div className="input-with-button">
                <input type={showPassword ? 'text' : 'password'} id="password" value={password} onChange={(e) => setPassword(e.target.value)} required disabled={isLoading} minLength={6} />
                <button type="button" className="password-toggle-btn" onClick={() => setShowPassword(!showPassword)} tabIndex={-1} aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}>
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <button type="submit" className="login-button" disabled={isLoading}>
              {isLoading ? 'Creando cuenta...' : 'Registrarse'}
            </button>
          </form>
        ) : (
          <div className="login-form">
            <p className="auth-success">
              ¡Registro exitoso! Hemos enviado un código de verificación a tu correo electrónico.
            </p>
            <p style={{ color: '#a0a0a0', fontSize: '0.9rem', marginTop: '10px' }}>
              Revisa tu bandeja de entrada y copia el código en la siguiente pantalla.
            </p>
            <button className="login-button" onClick={() => navigate('/verify-email')} style={{ marginTop: '20px' }}>
              Ya tengo el código
            </button>
          </div>
        )}

        <footer className="footer-section">
          <p>
            ¿Ya tienes una cuenta?{' '}
            <a href="#" className="sign-up-link" onClick={(e) => { e.preventDefault(); navigate('/login'); }}>
              Inicia sesión aquí
            </a>
          </p>
        </footer>
      </div>
    </div>
  );
}

export default Register;
