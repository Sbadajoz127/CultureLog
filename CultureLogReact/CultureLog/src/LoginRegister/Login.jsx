import { useState } from 'react';
import '../App.css';

function Login({ onSwitchToRegister, onLoginSuccess }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = (event) => {
    event.preventDefault();
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      onLoginSuccess(email)
    }, 2000);
  };

  return (
    <div className="login-card">
      <header className="brand-section">
        <h1 className="brand-logo">Culture<span>Log</span></h1>
        <p className="brand-tagline">Conectando historias, compartiendo cultura.</p>
      </header>

      <form className="login-form" onSubmit={handleSubmit}>
        <div className="input-group">
          <label htmlFor="email">Correo electrónico</label>
          <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)} required disabled={isLoading} />
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
          ¿Eres nuevo en CultureLog?{' '}
          <a href="#" className="sign-up-link" onClick={(e) => { e.preventDefault(); onSwitchToRegister(); }}>
            Regístrate gratis
          </a>
        </p>
      </footer>
    </div>
  );
}

export default Login;