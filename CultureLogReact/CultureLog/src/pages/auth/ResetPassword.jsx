import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import { resetPassword } from '../../services/api';
import './Auth.css';

function ResetPassword() {
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');

    if (newPassword !== confirmPassword) {
      setError('Las contraseñas no coinciden.');
      return;
    }

    setIsLoading(true);

    try {
      await resetPassword(token, newPassword);
      setSuccess(true);
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        'Error al restablecer la contraseña. Verifica el código e inténtalo de nuevo.';
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
          <p className="brand-tagline">Establece tu nueva contraseña.</p>
        </header>

        {!success ? (
          <form className="login-form" onSubmit={handleSubmit}>
            {error && <p className="auth-error">{error}</p>}

            <div className="input-group">
              <label htmlFor="reset-token">Código de seguridad</label>
              <input
                type="text"
                id="reset-token"
                value={token}
                onChange={(e) => setToken(e.target.value)}
                placeholder="Pega aquí el código del email"
                required
                disabled={isLoading}
              />
            </div>

            <div className="input-group password-group">
              <label htmlFor="new-password">Nueva contraseña</label>
              <div className="input-with-button">
                <input
                  type={showNewPassword ? 'text' : 'password'}
                  id="new-password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  disabled={isLoading}
                  minLength={6}
                />
                <button type="button" className="password-toggle-btn" onClick={() => setShowNewPassword(!showNewPassword)} tabIndex={-1} aria-label={showNewPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}>
                  {showNewPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <div className="input-group password-group">
              <label htmlFor="confirm-password">Confirmar contraseña</label>
              <div className="input-with-button">
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  id="confirm-password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  disabled={isLoading}
                  minLength={6}
                />
                <button type="button" className="password-toggle-btn" onClick={() => setShowConfirmPassword(!showConfirmPassword)} tabIndex={-1} aria-label={showConfirmPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}>
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <button type="submit" className="login-button" disabled={isLoading}>
              {isLoading ? 'Restableciendo...' : 'Restablecer contraseña'}
            </button>
          </form>
        ) : (
          <div className="login-form">
            <p className="auth-success">
              ¡Contraseña actualizada con éxito! Ya puedes iniciar sesión con tu nueva contraseña.
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

export default ResetPassword;
