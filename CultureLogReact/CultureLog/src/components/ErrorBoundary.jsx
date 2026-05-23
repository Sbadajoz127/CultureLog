import { Component } from 'react';

export class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught:', error, errorInfo);
  }

  handleReset = () => {
    this.setState({ hasError: false });
  };

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', padding: '2rem', textAlign: 'center' }}>
          <h2 style={{ marginBottom: '1rem' }}>Algo salió mal</h2>
          <p style={{ marginBottom: '1.5rem', color: 'var(--text-muted, #888)' }}>
            Ha ocurrido un error inesperado. Intenta recargar la página.
          </p>
          <button
            type="button"
            onClick={() => window.location.reload()}
            style={{ padding: '0.6rem 1.5rem', borderRadius: '8px', border: 'none', background: 'var(--accent, #448AFF)', color: '#fff', cursor: 'pointer', fontSize: '1rem' }}
          >
            Recargar página
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
