import { useState } from 'react';
import './App.css';

function Home({ userName, profilePic, onLogout, onGoToProfile }) {
  const [activeCategory, setActiveCategory] = useState('General');
  const [newPostContent, setNewPostContent] = useState('');

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

  const handlePostSubmit = (e) => {
    e.preventDefault();
    if (!newPostContent.trim()) return;

    const newPost = {
      id: Date.now(),
      author: userName || 'Usuario',
      authorPic: profilePic,
      content: newPostContent,
      category: activeCategory === 'General' ? 'General' : activeCategory,
      likes: 0,
      date: 'Ahora mismo'
    };

    setPosts([newPost, ...posts]);
    setNewPostContent('');
  };

  const handleLike = (postId) => {
    setPosts(posts.map((post) =>
      post.id === postId ? { ...post, likes: post.likes + 1 } : post
    ));
  };

  const filteredPosts = activeCategory === 'General'
    ? posts
    : posts.filter(post => post.category === activeCategory);

  return (
    <div className="home-container">
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
          <button className="header-user-btn" onClick={onGoToProfile}>
            @{userName}
          </button>
          <button className="logout-button" onClick={onLogout}>
            Cerrar sesión
          </button>
        </div>
      </header>

      <main className="feed">
        <div className="create-post-card">
          <form onSubmit={handlePostSubmit}>
            <textarea
              placeholder={`¿Qué quieres compartir en ${activeCategory}?`}
              value={newPostContent}
              onChange={(e) => setNewPostContent(e.target.value)}
              maxLength={280}
            />
            <div className="post-actions">
              <span className="text-dim" style={{ fontSize: '0.8rem', marginRight: '15px' }}>
                {newPostContent.length}/280
              </span>
              <button type="submit" className="login-button" style={{ padding: '8px 25px', fontSize: '0.9rem', width: 'auto' }}>
                Publicar en {activeCategory}
              </button>
            </div>
          </form>
        </div>

        <div className="posts-list">
          {filteredPosts.length > 0 ? (
            filteredPosts.map((post) => (
              <div key={post.id} className="post-card">
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px', alignItems: 'flex-start' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <img src={post.authorPic} alt={post.author} className="post-avatar-small" />
                    <div>
                      <h4 className="post-author" style={{ marginBottom: '2px' }}>{post.author}</h4>
                      <span className="post-category-tag">{post.category}</span>
                    </div>
                  </div>
                  <span className="text-dim" style={{ fontSize: '0.8rem' }}>{post.date}</span>
                </div>

                <p className="post-content">{post.content}</p>

                <div className="post-footer">
                  <button
                    style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0, fontSize: '0.95rem', fontWeight: '600' }}
                    className="text-faint"
                    onClick={() => handleLike(post.id)}
                  >
                    🤍 {post.likes} Me gusta
                  </button>
                </div>
              </div>
            ))
          ) : (
            <div className="bg-card" style={{ textAlign: 'center', marginTop: '40px', padding: '40px', borderRadius: '12px' }}>
              <p className="text-faint" style={{ fontSize: '1.2rem', marginBottom: '10px' }}>Aún no hay publicaciones de {activeCategory}.</p>
              <p style={{ fontSize: '0.9rem' }}>¡Sé el primero en compartir algo interesante!</p>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

export default Home;
