import { useState } from 'react';
import '../App.css';

function Register({ onSwitchToLogin }) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = (event) => {
    event.preventDefault();
    setIsLoading(true);
    setTimeout(() => {
      alert(`¡Cuenta creada con éxito para ${name}!`);
      setIsLoading(false);
    }, 2000);
  };

  return (
    <div className="login-card">
      <header className="brand-section">
        <h1 className="brand-logo">Culture<span>Log</span></h1>
        <p className="brand-tagline">Únete a nuestra comunidad cultural.</p>
      </header>

      <form className="login-form" onSubmit={handleSubmit}>
        <div className="input-group">
          <label htmlFor="name">Nombre completo</label>
          <input type="text" id="name" value={name} onChange={(e) => setName(e.target.value)} required disabled={isLoading} />
        </div>

        <div className="input-group">
          <label htmlFor="email">Correo electrónico</label>
          <input type="email" id="email" value={email} onChange={(e) => setEmail(e.target.value)} required disabled={isLoading} />
        </div>

        <div className="input-group">
          <label htmlFor="password">Crea una contraseña</label>
          <input type="password" id="password" value={password} onChange={(e) => setPassword(e.target.value)} required disabled={isLoading} />
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