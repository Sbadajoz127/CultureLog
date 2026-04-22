import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { useProfilePic } from '../context/ProfilePicContext';
import { AppHeader } from '../components/AppHeader';
import { UserAvatar } from '../components/UserAvatar';
import { Trash2 } from 'lucide-react';
import {
  uploadImage,
  updateProfilePicture,
  updateBanner,
  removeBanner,
  requestAccountDeletion,
  confirmAccountDeletion,
} from '../services/api';
import '../App.css';

const THEME_OPTIONS = [
  { value: 'DARK', label: 'Oscuro' },
  { value: 'LIGHT', label: 'Claro' },
  { value: 'SYSTEM', label: 'Sistema' },
];

const PRIVACY_OPTIONS = [
  { value: 'PUBLICO', label: 'Público' },
  { value: 'SOLO_AMIGOS', label: 'Solo amigos' },
  { value: 'PRIVADO', label: 'Privado' },
];

function Profile() {
  const { user, setUser, logout } = useAuth();
  const { theme, accentColor, settings, updateTheme, updateAccentColor, updateSettings } = useTheme();
  const { profilePic, setProfilePic } = useProfilePic();
  const navigate = useNavigate();

  const [previewPic, setPreviewPic] = useState(profilePic);
  const [pendingFile, setPendingFile] = useState(null);
  const [previewBanner, setPreviewBanner] = useState(user?.bannerUrl || null);
  const [pendingBannerFile, setPendingBannerFile] = useState(null);
  const [localTheme, setLocalTheme] = useState(theme);
  const [localAccent, setLocalAccent] = useState(accentColor);
  const [localPrivacy, setLocalPrivacy] = useState(settings?.profilePrivacy || 'PUBLICO');
  const [localAllowComments, setLocalAllowComments] = useState(settings?.allowComments ?? true);
  const [localShowFutureList, setLocalShowFutureList] = useState(settings?.showFutureList ?? true);
  const [localEmailNotifications, setLocalEmailNotifications] = useState(settings?.emailNotifications ?? true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteStep, setDeleteStep] = useState('confirm');
  const [deletionCode, setDeletionCode] = useState('');
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  useEffect(() => {
    if (!pendingFile) setPreviewPic(profilePic);
  }, [profilePic, pendingFile]);

  useEffect(() => {
    if (!pendingBannerFile) setPreviewBanner(user?.bannerUrl || null);
  }, [user?.bannerUrl, pendingBannerFile]);

  useEffect(() => {
    if (settings) {
      setLocalPrivacy(settings.profilePrivacy || 'PUBLICO');
      setLocalAllowComments(settings.allowComments ?? true);
      setLocalShowFutureList(settings.showFutureList ?? true);
      setLocalEmailNotifications(settings.emailNotifications ?? true);
    }
  }, [settings]);

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setPreviewPic(URL.createObjectURL(file));
      setPendingFile(file);
    }
  };

  const handleBannerChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setPreviewBanner(URL.createObjectURL(file));
      setPendingBannerFile(file);
    }
  };

  const handleRemoveBanner = async () => {
    if (!previewBanner) return;
    try {
      if (user?.bannerUrl) {
        await removeBanner();
      }
      setPreviewBanner(null);
      setPendingBannerFile(null);
      const updated = { ...user, bannerUrl: null };
      setUser(updated);
      localStorage.setItem('user', JSON.stringify(updated));
    } catch {
      setError('Error al eliminar el banner.');
    }
  };

  const handleRequestDeletion = async () => {
    setDeleteLoading(true);
    setDeleteError('');
    try {
      await requestAccountDeletion();
      setDeleteStep('code');
    } catch (err) {
      setDeleteError(
        err.response?.data?.message || err.response?.data?.error || 'Error al solicitar eliminación.'
      );
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleConfirmDeletion = async () => {
    if (!deletionCode.trim()) {
      setDeleteError('Introduce el código de confirmación.');
      return;
    }
    setDeleteLoading(true);
    setDeleteError('');
    try {
      await confirmAccountDeletion(deletionCode.trim());
      logout();
      navigate('/login');
    } catch (err) {
      setDeleteError(
        err.response?.data?.message || err.response?.data?.error || 'Código incorrecto o expirado.'
      );
    } finally {
      setDeleteLoading(false);
    }
  };

  const closeDeleteModal = () => {
    setShowDeleteModal(false);
    setDeleteStep('confirm');
    setDeletionCode('');
    setDeleteError('');
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);

    try {
      let updatedUser = { ...user };

      if (pendingFile) {
        const { data: cloudinaryUrl } = await uploadImage(pendingFile);
        await updateProfilePicture(cloudinaryUrl);
        setProfilePic(cloudinaryUrl);
        updatedUser.profilePictureUrl = cloudinaryUrl;
        setPendingFile(null);
      }

      if (pendingBannerFile) {
        const { data: bannerUrl } = await uploadImage(pendingBannerFile);
        await updateBanner(bannerUrl);
        updatedUser.bannerUrl = bannerUrl;
        setPendingBannerFile(null);
      }

      setUser(updatedUser);
      localStorage.setItem('user', JSON.stringify(updatedUser));

      if (localTheme !== theme) {
        await updateTheme(localTheme);
      }
      if (localAccent !== accentColor) {
        await updateAccentColor(localAccent);
      }

      await updateSettings({
        profilePrivacy: localPrivacy,
        allowComments: localAllowComments,
        showFutureList: localShowFutureList,
        emailNotifications: localEmailNotifications,
      });

      navigate('/home');
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data ||
        'Error al guardar los cambios. Inténtalo de nuevo.';
      setError(typeof msg === 'string' ? msg : 'Error al guardar los cambios.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="home-container">
      <AppHeader active="profile" userName={user.username} />

      <main className="feed profile-feed">
        <div className="create-post-card profile-card">
          <h3 className="profile-card-title">Ajustes de cuenta</h3>
          <button
            type="button"
            className="profile-view-public-link"
            onClick={() => navigate(`/user/${user.username}`)}
          >
            Ver mi perfil público
          </button>

          <form className="login-form" onSubmit={handleSave}>
            {error && <p className="auth-error">{error}</p>}

            <div className="profile-banner-section">
              <label className="profile-banner-label">Banner del perfil</label>
              {previewBanner ? (
                <div className="profile-banner-preview-wrap">
                  <img src={previewBanner} alt="Banner" className="profile-banner-preview" />
                  <button
                    type="button"
                    className="profile-banner-remove-btn"
                    onClick={handleRemoveBanner}
                    disabled={saving}
                    title="Eliminar banner"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              ) : (
                <div className="profile-banner-placeholder">Sin banner</div>
              )}
              <label htmlFor="banner-upload" className="upload-btn">
                {previewBanner ? 'Cambiar banner' : 'Subir banner'}
              </label>
              <input
                id="banner-upload"
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp"
                onChange={handleBannerChange}
                style={{ display: 'none' }}
                disabled={saving}
              />
            </div>

            <div className="profile-pic-section">
              {previewPic ? (
                <img src={previewPic} alt="Tu perfil" className="profile-avatar-large" />
              ) : (
                <UserAvatar name={user?.username} size="large" className="profile-avatar-large" />
              )}
              <label htmlFor="avatar-upload" className="upload-btn">
                Cambiar foto
              </label>
              <input
                id="avatar-upload"
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp"
                onChange={handleImageChange}
                style={{ display: 'none' }}
                disabled={saving}
              />
            </div>

            <div className="input-group">
              <label>Nombre de usuario</label>
              <div className="profile-info-readonly">{user?.username}</div>
            </div>

            <div className="input-group">
              <label>Correo electrónico</label>
              <div className="profile-info-readonly">{user?.email}</div>
            </div>

            <hr className="section-divider" />
            <h4 className="settings-section-title">Apariencia</h4>

            <div className="input-group">
              <label>Tema de la aplicación</label>
              <div className="theme-selector">
                {THEME_OPTIONS.map((opt) => (
                  <button
                    key={opt.value}
                    type="button"
                    className={`theme-option ${localTheme === opt.value ? 'active' : ''}`}
                    onClick={() => setLocalTheme(opt.value)}
                    disabled={saving}
                  >
                    {opt.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="input-group">
              <label>Color de acento</label>
              <div className="accent-color-group">
                <input
                  type="color"
                  className="accent-color-input"
                  value={localAccent}
                  onChange={(e) => setLocalAccent(e.target.value)}
                  disabled={saving}
                />
                <span className="accent-color-hex">{localAccent}</span>
              </div>
            </div>

            <hr className="section-divider" />
            <h4 className="settings-section-title">Privacidad y social</h4>

            <div className="input-group">
              <label>Privacidad del perfil</label>
              <div className="theme-selector">
                {PRIVACY_OPTIONS.map((opt) => (
                  <button
                    key={opt.value}
                    type="button"
                    className={`theme-option ${localPrivacy === opt.value ? 'active' : ''}`}
                    onClick={() => setLocalPrivacy(opt.value)}
                    disabled={saving}
                  >
                    {opt.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="input-group">
              <label>Permitir comentarios</label>
              <button
                type="button"
                className={`toggle-switch ${localAllowComments ? 'active' : ''}`}
                onClick={() => setLocalAllowComments((v) => !v)}
                disabled={saving}
                role="switch"
                aria-checked={localAllowComments}
              >
                <span className="toggle-knob" />
              </button>
            </div>

            <div className="input-group">
              <label>Mostrar lista &quot;Por ver&quot;</label>
              <button
                type="button"
                className={`toggle-switch ${localShowFutureList ? 'active' : ''}`}
                onClick={() => setLocalShowFutureList((v) => !v)}
                disabled={saving}
                role="switch"
                aria-checked={localShowFutureList}
              >
                <span className="toggle-knob" />
              </button>
            </div>

            <hr className="section-divider" />
            <h4 className="settings-section-title">Notificaciones</h4>

            <div className="input-group">
              <label>Notificaciones por email</label>
              <button
                type="button"
                className={`toggle-switch ${localEmailNotifications ? 'active' : ''}`}
                onClick={() => setLocalEmailNotifications((v) => !v)}
                disabled={saving}
                role="switch"
                aria-checked={localEmailNotifications}
              >
                <span className="toggle-knob" />
              </button>
            </div>

            <div className="profile-form-actions">
              <button
                type="button"
                className="logout-button profile-action-btn"
                onClick={() => navigate('/home')}
                disabled={saving}
              >
                Cancelar
              </button>
              <button type="submit" className="login-button profile-action-btn" disabled={saving}>
                {saving ? 'Guardando...' : 'Guardar cambios'}
              </button>
            </div>
          </form>

          <hr className="section-divider" />
          <h4 className="settings-section-title danger-section-title">Zona de peligro</h4>
          <div className="danger-zone">
            <p className="danger-zone-text">
              Eliminar tu cuenta es una acción <strong>irreversible</strong>. Se borrarán todos tus datos, publicaciones, comentarios y biblioteca.
            </p>
            <button
              type="button"
              className="danger-btn"
              onClick={() => setShowDeleteModal(true)}
            >
              <Trash2 size={16} /> Eliminar mi cuenta
            </button>
          </div>
        </div>
      </main>

      {showDeleteModal && (
        <div className="modal-overlay" onClick={closeDeleteModal}>
          <div className="modal-content delete-account-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Eliminar cuenta</h3>
              <button type="button" className="close-btn" onClick={closeDeleteModal}>×</button>
            </div>

            {deleteError && <p className="auth-error">{deleteError}</p>}

            {deleteStep === 'confirm' && (
              <div className="delete-modal-body">
                <p className="delete-warning">
                  ¿Estás seguro de que deseas eliminar tu cuenta? Esta acción es <strong>permanente</strong> y no se puede deshacer.
                </p>
                <p className="delete-info">
                  Se enviará un código de confirmación a tu correo electrónico: <strong>{user?.email}</strong>
                </p>
                <div className="delete-modal-actions">
                  <button
                    type="button"
                    className="logout-button"
                    onClick={closeDeleteModal}
                    disabled={deleteLoading}
                  >
                    Cancelar
                  </button>
                  <button
                    type="button"
                    className="danger-btn"
                    onClick={handleRequestDeletion}
                    disabled={deleteLoading}
                  >
                    {deleteLoading ? 'Enviando...' : 'Enviar código'}
                  </button>
                </div>
              </div>
            )}

            {deleteStep === 'code' && (
              <div className="delete-modal-body">
                <p className="delete-info">
                  Hemos enviado un código de 6 dígitos a <strong>{user?.email}</strong>. Introduce el código para confirmar la eliminación.
                </p>
                <div className="input-group">
                  <label>Código de confirmación</label>
                  <input
                    type="text"
                    className="portal-input deletion-code-input"
                    placeholder="123456"
                    value={deletionCode}
                    onChange={(e) => setDeletionCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                    maxLength={6}
                    disabled={deleteLoading}
                  />
                </div>
                <div className="delete-modal-actions">
                  <button
                    type="button"
                    className="logout-button"
                    onClick={closeDeleteModal}
                    disabled={deleteLoading}
                  >
                    Cancelar
                  </button>
                  <button
                    type="button"
                    className="danger-btn"
                    onClick={handleConfirmDeletion}
                    disabled={deleteLoading || deletionCode.length !== 6}
                  >
                    {deleteLoading ? 'Eliminando...' : 'Eliminar cuenta'}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default Profile;
