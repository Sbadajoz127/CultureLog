import { useState } from 'react';
import './App.css'; // Asegúrate de que la ruta sea correcta según tus carpetas

function Home({ userName, profilePic, onLogout, onGoToProfile }) {
  // 1. ESTADOS
  const [activeCategory, setActiveCategory] = useState('General');
  const [newPostContent, setNewPostContent] = useState('');
  
  // Lista inicial de publicaciones
  const [posts, setPosts] = useState([
    {
      id: 1,
      author: 'Equipo CultureLog',
      authorPic: 'https://www.pngarts.com/files/3/Monkey-Transparent-Background-PNG.png',
      content: '¡Bienvenido a tu nuevo espacio! Aquí puedes llevar el registro de todas tus aventuras culturales.',
      category: 'General',
      likes: 10,
      date: 'Hace un momento'
    }
  ]);

  // 2. FUNCIÓN PARA CREAR UNA PUBLICACIÓN
  const handlePostSubmit = (e) => {
    e.preventDefault();
    
    if (!newPostContent.trim()) return;

    const newPost = {
      id: Date.now(),
      author: userName || 'Usuario', // Usa el nombre real del login
      authorPic: profilePic,         // Usa la foto que subiste en Ajustes
      content: newPostContent,
      category: activeCategory === 'General' ? 'General' : activeCategory, 
      likes: 0,
      date: 'Ahora mismo'
    };

    setPosts([newPost, ...posts]);
    setNewPostContent('');
  };

  // 3. FUNCIÓN PARA DAR "ME GUSTA"
  const handleLike = (postId) => {
    const updatedPosts = posts.map((post) => {
      // Si el ID del post coincide con el que hicimos clic, le sumamos 1
      if (post.id === postId) {
        return { ...post, likes: post.likes + 1 };
      }
      return post;
    });
    setPosts(updatedPosts);
  };

  // 4. FILTRO DE CATEGORÍAS
  const filteredPosts = activeCategory === 'General' 
    ? posts 
    : posts.filter(post => post.category === activeCategory);

  return (
    <div className="home-container">
      
      {/* --- HEADER SUPERIOR --- */}
      <header className="top-header">
        
        <div className="header-left">
          <h2 className="brand-logo" style={{ fontSize: '1.5rem', margin: 0 }}>
            Culture<span>Log</span>
          </h2>
        </div>

        <nav className="header-center">
          {['General', 'Libros', 'Películas', 'Música'].map((category) => (
            <button 
              key={category}
              className={`category-btn ${activeCategory === category ? 'active' : ''}`}
              onClick={() => setActiveCategory(category)}
            >
              {category}
            </button>
          ))}
        </nav>

        <div className="header-right">
            <button 
                style={{ background: 'none', border: 'none', color: '#a0a0a0', fontSize: '0.95rem', fontWeight: '600', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '5px' }}
                onClick={onGoToProfile}
            >
             @{userName} ⚙️
            </button>
          <button className="logout-button" onClick={onLogout}>
            Salir
          </button>
        </div>
      </header>

      {/* --- MURO CENTRAL --- */}
      <main className="feed">
        
        {/* Caja para escribir */}
        <div className="create-post-card">
          <form onSubmit={handlePostSubmit}>
            <textarea 
              placeholder={`¿Qué quieres compartir en ${activeCategory}?`}
              value={newPostContent}
              onChange={(e) => setNewPostContent(e.target.value)}
              maxLength={280}
            />
            <div className="post-actions">
              <span style={{ color: '#555', fontSize: '0.8rem', marginRight: '15px' }}>
                {newPostContent.length}/280
              </span>
              <button type="submit" className="login-button" style={{ padding: '8px 25px', fontSize: '0.9rem', width: 'auto' }}>
                Publicar en {activeCategory}
              </button>
            </div>
          </form>
        </div>

        {/* Lista de posts */}
        <div className="posts-list">
          {filteredPosts.length > 0 ? (
            filteredPosts.map((post) => (
              <div key={post.id} className="post-card">
                
                {/* Cabecera del post */}
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px', alignItems: 'flex-start' }}>  
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <img src={post.authorPic} alt={post.author} className="post-avatar-small" />
                    <div>
                      <h4 className="post-author" style={{ marginBottom: '2px' }}>{post.author}</h4>
                      <span className="post-category-tag">{post.category}</span>
                    </div>
                  </div>
                  <span style={{ color: '#555', fontSize: '0.8rem' }}>{post.date}</span>
                </div>
                
                {/* Contenido */}
                <p className="post-content">{post.content}</p>
                
                {/* Pie del post con el botón funcional */}
                <div className="post-footer">
                  <button 
                    style={{ background: 'none', border: 'none', color: '#888', cursor: 'pointer', padding: 0, fontSize: '0.95rem', fontWeight: '600' }}
                    onClick={() => handleLike(post.id)}
                  >
                    🤍 {post.likes} Me gusta
                  </button>
                </div>

              </div>
            ))
          ) : (
            <div style={{ textAlign: 'center', color: '#888', marginTop: '40px', padding: '40px', backgroundColor: '#1e1e1e', borderRadius: '12px' }}>
              <p style={{ fontSize: '1.2rem', marginBottom: '10px' }}>Aún no hay publicaciones de {activeCategory}.</p>
              <p style={{ fontSize: '0.9rem' }}>¡Sé el primero en compartir algo interesante!</p>
            </div>
          )}
        </div>
      </main>

    </div>
  );
}

export default Home;