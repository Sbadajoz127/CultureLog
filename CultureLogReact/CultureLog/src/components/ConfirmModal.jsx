import { useEffect, useRef } from 'react';
import { AlertTriangle } from 'lucide-react';
import '../App.css';

export function ConfirmModal({
  isOpen,
  title = '¿Estás seguro?',
  message,
  confirmText = 'Confirmar',
  cancelText = 'Cancelar',
  onConfirm,
  onCancel,
  danger = false,
}) {
  const confirmBtnRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      confirmBtnRef.current?.focus();
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (!isOpen) return;
      if (e.key === 'Escape') {
        onCancel();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onCancel]);

  if (!isOpen) return null;

  return (
    <div className="confirm-modal-overlay" onClick={onCancel}>
      <div className="confirm-modal" onClick={(e) => e.stopPropagation()}>
        <div className={`confirm-modal-icon ${danger ? 'danger' : ''}`}>
          <AlertTriangle size={32} />
        </div>
        <h3 className="confirm-modal-title">{title}</h3>
        {message && <p className="confirm-modal-message">{message}</p>}
        <div className="confirm-modal-actions">
          <button
            type="button"
            className="logout-button"
            onClick={onCancel}
          >
            {cancelText}
          </button>
          <button
            ref={confirmBtnRef}
            type="button"
            className={danger ? 'danger-btn' : 'login-button'}
            onClick={onConfirm}
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}
