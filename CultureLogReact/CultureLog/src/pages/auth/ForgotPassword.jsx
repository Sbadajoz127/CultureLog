import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { requestPasswordReset } from '../../services/api';
import '../../App.css';

function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await requestPasswordReset(email);
      setSent(true);
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        'Error al enviar el correo. Inténtalo de nuevo.';
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
          <p className="brand-tagline">Recupera el acceso a tu cuenta.</p>
        </header>

        {!sent ? (
          <form className="login-form" onSubmit={handleSubmit}>
            {error && <p className="auth-error">{error}</p>}

            <p style={{ color: '#a0a0a0', fontSize: '0.9rem', marginBottom: '20px' }}>
              Introduce tu correo electrónico y te enviaremos un código para restablecer tu contraseña.
            </p>

            <div className="input-group">
              <label htmlFor="reset-email">Correo electrónico</label>
              <input type="email" id="reset-email" value={email} onChange={(e) => setEmail(e.target.value)} required disabled={isLoading} />
            </div>

            <button type="submit" className="login-button" disabled={isLoading}>
              {isLoading ? 'Enviando...' : 'Enviar código'}
            </button>
          </form>
        ) : (
          <div className="login-form">
            <p className="auth-success">
              Si el correo está registrado, recibirás un código de seguridad en tu bandeja de entrada.
            </p>
            <button className="login-button" onClick={() => navigate('/reset-password')} style={{ marginTop: '20px' }}>
              Ya tengo el código
            </button>
          </div>
        )}

        <footer className="footer-section">
          <p>
            <a href="#" className="sign-up-link" onClick={(e) => { e.preventDefault(); navigate('/login'); }}>
              Volver al inicio de sesión
            </a>
          </p>
        </footer>
      </div>
    </div>
  );
}

export default ForgotPassword;
