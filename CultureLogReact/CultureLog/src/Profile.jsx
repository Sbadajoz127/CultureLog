// src/Profile.jsx
import { useState } from 'react';
import './App.css'; 

function Profile({ userName, setUserName, userEmail, setUserEmail, profilePic, setProfilePic, onBack, onLogout }) {
  
  // Estados locales para editar antes de guardar
  const [editName, setEditName] = useState(userName);
  const [editEmail, setEditEmail] = useState(userEmail);
  const [newPassword, setNewPassword] = useState('');
  const [previewPic, setPreviewPic] = useState(profilePic);

  // Función mágica para previsualizar la imagen que subes
  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Crea una URL temporal en tu navegador para mostrar la foto al instante
      const imageUrl = URL.createObjectURL(file);
      setPreviewPic(imageUrl);
    }
  };

  const handleSave = (e) => {
    e.preventDefault();
    
    // 1. Actualizamos los estados globales en App.jsx
    if (editName.trim()) setUserName(editName);
    if (editEmail.trim()) setUserEmail(editEmail);
    setProfilePic(previewPic);
    
    // 2. Simulamos el cambio de contraseña
    if (newPassword.trim()) {
      console.log('Aquí se enviaría la nueva contraseña a la base de datos:', newPassword);
    }

    alert('¡Perfil actualizado con éxito!');
    onBack(); // Volvemos al Home
  };

  return (
    <div className="home-container">
      
      <header className="top-header">
        <div className="header-left">
          <h2 className="brand-logo" style={{ fontSize: '1.5rem', margin: 0, cursor: 'pointer' }} onClick={onBack}>
            Culture<span>Log</span>
          </h2>
        </div>
        <div className="header-right">
          <button className="logout-button" onClick={onLogout}>Salir</button>
        </div>
      </header>

      <main className="feed" style={{ alignItems: 'center' }}>
        <div className="create-post-card" style={{ width: '100%', padding: '40px', maxWidth: '500px' }}>
          
          <h3 style={{ marginBottom: '30px', textAlign: 'center', fontSize: '1.4rem' }}>Ajustes de Cuenta</h3>
          
          <form className="login-form" onSubmit={handleSave}>
            
            {/* SECCIÓN 1: FOTO DE PERFIL */}
            <div className="profile-pic-section">
              <img src={previewPic} alt="Tu perfil" className="profile-avatar-large" />
              
              {/* Truco: Ocultamos el input de archivo feo y usamos un label bonito */}
              <label htmlFor="avatar-upload" className="upload-btn">
                Cambiar foto
              </label>
              <input 
                id="avatar-upload" 
                type="file" 
                accept="image/*" 
                onChange={handleImageChange} 
                style={{ display: 'none' }} // Lo escondemos
              />
            </div>

            {/* SECCIÓN 2: DATOS DEL USUARIO */}
            <div className="input-group">
              <label>Nombre de usuario</label>
              <input type="text" value={editName} onChange={(e) => setEditName(e.target.value)} required />
            </div>

            <div className="input-group">
              <label>Correo Electrónico</label>
              <input type="email" value={editEmail} onChange={(e) => setEditEmail(e.target.value)} required />
            </div>

            <div className="input-group">
              <label>Nueva Contraseña (Opcional)</label>
              <input 
                type="password" 
                placeholder="Deja en blanco para no cambiarla"
                value={newPassword} 
                onChange={(e) => setNewPassword(e.target.value)} 
              />
            </div>

            <div style={{ display: 'flex', gap: '15px', marginTop: '20px' }}>
              <button type="button" className="login-button" style={{ backgroundColor: 'transparent', color: '#a0a0a0', border: '1px solid #444', flex: 1 }} onClick={onBack}>
                Cancelar
              </button>
              <button type="submit" className="login-button" style={{ flex: 1 }}>
                Guardar cambios
              </button>
            </div>

          </form>
        </div>
      </main>
    </div>
  );
}

export default Profile;