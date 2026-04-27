import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { verifyEmail, resendVerificationEmail } from '../../services/api';
import '../../App.css';

function VerifyEmail() {
  const [token, setToken] = useState('');
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [resendSent, setResendSent] = useState(false);
  const [showResend, setShowResend] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await verifyEmail(token);
      setSuccess(true);
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        'Error al verificar la cuenta. Comprueba el código e inténtalo de nuevo.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleResend = async (event) => {
    event.preventDefault();
    setError('');
    setResendSent(false);
    setIsLoading(true);

    try {
      await resendVerificationEmail(email);
      setResendSent(true);
      setShowResend(false);
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        'Error al reenviar el correo. Inténtalo de nuevo.';
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
          <p className="brand-tagline">Verifica tu cuenta de correo electrónico.</p>
        </header>

        {!success ? (
          <form className="login-form" onSubmit={handleSubmit}>
            {error && <p className="auth-error">{error}</p>}
            {resendSent && (
              <p className="auth-success">
                Si el correo está registrado y no verificado, recibirás un nuevo código.
              </p>
            )}

            <div className="input-group">
              <label htmlFor="verify-token">Código de verificación</label>
              <input
                type="text"
                id="verify-token"
                value={token}
                onChange={(e) => setToken(e.target.value)}
                placeholder="Pega aquí el código del email"
                required
                disabled={isLoading}
              />
            </div>

            <button type="submit" className="login-button" disabled={isLoading}>
              {isLoading ? 'Verificando...' : 'Verificar cuenta'}
            </button>

            {!showResend ? (
              <p style={{ color: '#a0a0a0', fontSize: '0.85rem', marginTop: '15px', textAlign: 'center' }}>
                ¿No recibiste el código?{' '}
                <a
                  href="#"
                  className="sign-up-link"
                  onClick={(e) => { e.preventDefault(); setShowResend(true); }}
                >
                  Reenviar código
                </a>
              </p>
            ) : (
              <div style={{ marginTop: '15px' }}>
                <div className="input-group">
                  <label htmlFor="resend-email">Tu correo electrónico</label>
                  <input
                    type="email"
                    id="resend-email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    disabled={isLoading}
                  />
                </div>
                <button
                  type="button"
                  className="login-button"
                  disabled={isLoading}
                  onClick={handleResend}
                  style={{ marginTop: '10px' }}
                >
                  {isLoading ? 'Enviando...' : 'Reenviar código'}
                </button>
              </div>
            )}
          </form>
        ) : (
          <div className="login-form">
            <p className="auth-success">
              ¡Cuenta verificada con éxito! Ya puedes iniciar sesión.
            </p>
            <button className="login-button" onClick={() => navigate('/login')} style={{ marginTop: '20px' }}>
              Ir al inicio de sesión
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

export default VerifyEmail;
