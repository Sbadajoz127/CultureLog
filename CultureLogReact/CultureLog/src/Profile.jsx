import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import { useTheme } from './context/ThemeContext';
import { useProfilePic } from './context/ProfilePicContext';
import { AppHeader } from './components/AppHeader';
import { UserAvatar } from './components/UserAvatar';
import { uploadImage, updateProfilePicture } from './services/api';
import './App.css';

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
  const { user } = useAuth();
  const { theme, accentColor, settings, updateTheme, updateAccentColor, updateSettings } = useTheme();
  const { profilePic, setProfilePic } = useProfilePic();
  const navigate = useNavigate();

  const [previewPic, setPreviewPic] = useState(profilePic);
  const [pendingFile, setPendingFile] = useState(null);
  const [localTheme, setLocalTheme] = useState(theme);
  const [localAccent, setLocalAccent] = useState(accentColor);
  const [localPrivacy, setLocalPrivacy] = useState(settings?.profilePrivacy || 'PUBLICO');
  const [localAllowComments, setLocalAllowComments] = useState(settings?.allowComments ?? true);
  const [localShowFutureList, setLocalShowFutureList] = useState(settings?.showFutureList ?? true);
  const [localEmailNotifications, setLocalEmailNotifications] = useState(settings?.emailNotifications ?? true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!pendingFile) setPreviewPic(profilePic);
  }, [profilePic, pendingFile]);

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

  const handleSave = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);

    try {
      if (pendingFile) {
        const { data: cloudinaryUrl } = await uploadImage(pendingFile);
        await updateProfilePicture(cloudinaryUrl);
        setProfilePic(cloudinaryUrl);
        setPendingFile(null);
      }

      if (localTheme !== theme) {
        updateTheme(localTheme);
      }
      if (localAccent !== accentColor) {
        updateAccentColor(localAccent);
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
            onClick={() => navigate(`/user/${user.id}`)}
          >
            Ver mi perfil público
          </button>

          <form className="login-form" onSubmit={handleSave}>
            {error && <p className="auth-error">{error}</p>}

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
        </div>
      </main>
    </div>
  );
}

export default Profile;
