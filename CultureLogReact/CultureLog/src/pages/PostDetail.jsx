import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Heart, MessageCircle, Trash2 } from 'lucide-react';
import { AppHeader } from '../components/AppHeader';
import { UserAvatar } from '../components/UserAvatar';
import { useAuth } from '../context/AuthContext';
import { MEDIA_TYPE_LABELS } from '../constants/media';
import {
  addPostComment,
  getPostById,
  getPostComments,
  togglePostLike,
  deletePostComment,
  deletePost,
} from '../services/api';
import '../App.css';

const MAX_COMMENT_LENGTH = 1000;

function formatDate(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
  } catch {
    return iso;
  }
}

export default function PostDetail() {
  const { user } = useAuth();
  const { postId } = useParams();
  const navigate = useNavigate();

  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [commentText, setCommentText] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);
  const [replyOpenForId, setReplyOpenForId] = useState(null);
  const [replyDrafts, setReplyDrafts] = useState({});
  const [submittingReplyId, setSubmittingReplyId] = useState(null);
  const [liking, setLiking] = useState(false);

  const linkedItem = useMemo(() => ({
    id: post?.linkedItemId,
    title: post?.linkedItemTitle,
    type: post?.linkedItemType,
    rating: post?.linkedItemRating,
    imageUrl: post?.linkedItemImageUrl,
    creator: post?.linkedItemCreator,
    releaseDate: post?.linkedItemReleaseDate,
    genre: post?.linkedItemGenre,
    description: post?.linkedItemDescription,
  }), [post]);

  const linkedItemDescriptionText = (linkedItem?.description || '').trim()
    || 'Esta obra no tiene descripción disponible todavía.';

  const load = useCallback(async () => {
    if (!postId) return;
    setLoading(true);
    setError('');
    try {
      const [{ data: postData }, { data: commentsData }] = await Promise.all([
        getPostById(postId),
        getPostComments(postId),
      ]);
      setPost(postData);
      setComments(Array.isArray(commentsData) ? commentsData : []);
    } catch (e) {
      setError(e.response?.data?.message || e.response?.data?.error || 'No se pudo cargar la publicación.');
    } finally {
      setLoading(false);
    }
  }, [postId]);

  useEffect(() => {
    load();
  }, [load]);

  const handleLike = async () => {
    if (!post || liking) return;
    setLiking(true);
    try {
      const { data } = await togglePostLike(post.id);
      setPost((prev) => prev ? { ...prev, likedByCurrentUser: data.liked, likeCount: data.likeCount } : prev);
    } catch {
      /* ignore */
    } finally {
      setLiking(false);
    }
  };

  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    const text = commentText.trim();
    if (!text || !post) return;
    setSubmittingComment(true);
    try {
      const { data: created } = await addPostComment(post.id, text);
      setComments((prev) => [...prev, created]);
      setPost((prev) => prev ? { ...prev, commentCount: (prev.commentCount || 0) + 1 } : prev);
      setCommentText('');
    } catch (e) {
      setError(e.response?.data?.message || e.response?.data?.error || 'No se pudo enviar el comentario.');
    } finally {
      setSubmittingComment(false);
    }
  };

  const commentTree = useMemo(() => {
    const list = Array.isArray(comments) ? comments : [];
    const byId = new Map(list.map((c) => [c.id, { ...c, children: [] }]));
    const roots = [];
    for (const c of byId.values()) {
      if (c.parentCommentId && byId.has(c.parentCommentId)) {
        byId.get(c.parentCommentId).children.push(c);
      } else {
        roots.push(c);
      }
    }
    const sortByCreatedAt = (a, b) => new Date(a.createdAt) - new Date(b.createdAt);
    const sortRec = (arr) => {
      arr.sort(sortByCreatedAt);
      arr.forEach((x) => sortRec(x.children));
    };
    sortRec(roots);
    return roots;
  }, [comments]);

  const submitReply = async (parentCommentId) => {
    const text = (replyDrafts[parentCommentId] || '').trim();
    if (!text || !post) return;
    setSubmittingReplyId(parentCommentId);
    try {
      const { data: created } = await addPostComment(post.id, text, parentCommentId);
      setComments((prev) => [...prev, created]);
      setPost((prev) => prev ? { ...prev, commentCount: (prev.commentCount || 0) + 1 } : prev);
      setReplyDrafts((prev) => ({ ...prev, [parentCommentId]: '' }));
      setReplyOpenForId(null);
    } catch (e) {
      setError(e.response?.data?.message || e.response?.data?.error || 'No se pudo enviar la respuesta.');
    } finally {
      setSubmittingReplyId(null);
    }
  };

  const handleDeleteComment = async (commentId) => {
    try {
      await deletePostComment(commentId);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
      setPost((prev) => prev ? { ...prev, commentCount: Math.max(0, (prev.commentCount || 0) - 1) } : prev);
    } catch {
      /* ignore */
    }
  };

  const handleDeletePost = async () => {
    if (!post) return;
    if (!window.confirm('¿Seguro que quieres eliminar esta publicación?')) return;
    try {
      await deletePost(post.id);
      navigate('/home');
    } catch {
      /* ignore */
    }
  };

  const renderCommentNode = (c, depth = 0) => (
    <div key={c.id} className={`post-detail-comment-node ${depth > 0 ? 'is-reply' : ''}`}>
      <article className="post-detail-comment-item">
        <div className="post-detail-comment-head">
          <UserAvatar src={c.authorProfilePictureUrl} name={c.authorName} size="small" />
          <div className="post-detail-comment-header">
            <span
              className="post-author post-author-link"
              role="button"
              tabIndex={0}
              onClick={() => navigate(`/user/${c.authorName}`)}
              onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${c.authorName}`); }}
            >
              {c.authorName}
            </span>
            <span className="text-dim">{formatDate(c.createdAt)}</span>
          </div>
        </div>
        <p>{c.text}</p>

        <div className="post-detail-comment-actions-row">
          <button
            type="button"
            className="post-like-btn"
            onClick={() => setReplyOpenForId((prev) => (prev === c.id ? null : c.id))}
          >
            Responder
          </button>
          {(c.authorId === user?.id || post?.authorId === user?.id) && (
            <button
              type="button"
              className="post-like-btn post-comment-delete-btn"
              onClick={() => handleDeleteComment(c.id)}
            >
              <Trash2 size={14} /> Eliminar
            </button>
          )}
        </div>

        {replyOpenForId === c.id && (
          <div className="post-detail-reply-form">
            <textarea
              value={replyDrafts[c.id] || ''}
              onChange={(e) => setReplyDrafts((prev) => ({ ...prev, [c.id]: e.target.value }))}
              placeholder={`Responder a @${c.authorName}...`}
              maxLength={MAX_COMMENT_LENGTH}
            />
            <div className="post-detail-comment-actions">
              <span className="text-dim">{(replyDrafts[c.id] || '').length}/{MAX_COMMENT_LENGTH}</span>
              <button
                type="button"
                className="login-button"
                disabled={submittingReplyId === c.id || !(replyDrafts[c.id] || '').trim()}
                onClick={() => submitReply(c.id)}
              >
                {submittingReplyId === c.id ? 'Enviando...' : 'Responder'}
              </button>
            </div>
          </div>
        )}
      </article>

      {Array.isArray(c.children) && c.children.length > 0 && (
        <div className="post-detail-comment-children">
          {c.children.map((child) => renderCommentNode(child, depth + 1))}
        </div>
      )}
    </div>
  );

  return (
    <div className="home-container">
      <AppHeader active="home" userName={user?.username} />
      <main className="feed post-detail-page">
        <button type="button" className="logout-button post-detail-back" onClick={() => navigate(-1)}>
          Volver
        </button>

        {loading && <p className="text-muted">Cargando publicación...</p>}
        {error && <p className="auth-error">{error}</p>}

        {post && !loading && (
          <article className="post-card post-detail-card">
            <div className="post-card-header">
              <div className="post-card-author">
                <UserAvatar src={post.authorProfilePictureUrl} name={post.authorName} size="small" />
                <div>
                  <h4
                    className="post-author post-author-link"
                    role="button"
                    tabIndex={0}
                    onClick={() => navigate(`/user/${post.authorName}`)}
                    onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${post.authorName}`); }}
                  >
                    {post.authorName}
                  </h4>
                  {post.linkedItemType && (
                    <span className="post-category-tag">{MEDIA_TYPE_LABELS[post.linkedItemType] || post.linkedItemType}</span>
                  )}
                </div>
              </div>
              <span className="text-dim post-date">{formatDate(post.createdAt)}</span>
              {post.authorId === user?.id && (
                <button type="button" className="post-like-btn post-delete-btn" onClick={handleDeletePost}>
                  <Trash2 size={16} /> Eliminar
                </button>
              )}
            </div>

            {linkedItem?.id && (
              <section className="post-detail-linked-item">
                {linkedItem.imageUrl ? (
                  <img src={linkedItem.imageUrl} alt={linkedItem.title} className="post-detail-linked-image" />
                ) : (
                  <div className="post-detail-linked-image post-detail-placeholder">
                    {MEDIA_TYPE_LABELS[linkedItem.type] || linkedItem.type}
                  </div>
                )}
                <div className="post-detail-linked-body">
                  <h3 className="post-detail-linked-title">{linkedItem.title}</h3>
                  <p className="text-muted">
                    {MEDIA_TYPE_LABELS[linkedItem.type] || linkedItem.type}
                    {linkedItem.creator ? ` · ${linkedItem.creator}` : ''}
                    {linkedItem.releaseDate ? ` · ${linkedItem.releaseDate}` : ''}
                    {linkedItem.rating != null ? ` · ${linkedItem.rating}/10` : ''}
                  </p>
                  {linkedItem.genre && <p className="text-muted">Género: {linkedItem.genre}</p>}
                  <p className="post-detail-linked-description">
                    <strong>Descripción: </strong>
                    {linkedItemDescriptionText}
                  </p>
                </div>
              </section>
            )}

            <p className="post-content">{post.content}</p>

            <div className="post-footer">
              <button
                type="button"
                className={`post-like-btn ${post.likedByCurrentUser ? 'liked' : ''}`}
                onClick={handleLike}
                disabled={liking}
              >
                <Heart size={16} fill={post.likedByCurrentUser ? 'currentColor' : 'none'} /> {post.likeCount} Me gusta
              </button>
              <span className="text-muted post-comment-count">
                <MessageCircle size={14} /> {post.commentCount || comments.length} comentarios
              </span>
            </div>

            <section className="post-detail-comments">
              <h3 className="post-detail-comments-title">Comentarios</h3>
              <form onSubmit={handleCommentSubmit} className="post-detail-comment-form">
                <textarea
                  value={commentText}
                  onChange={(e) => setCommentText(e.target.value)}
                  placeholder="Escribe un comentario..."
                  maxLength={MAX_COMMENT_LENGTH}
                />
                <div className="post-detail-comment-actions">
                  <span className="text-dim">{commentText.length}/{MAX_COMMENT_LENGTH}</span>
                  <button type="submit" className="login-button" disabled={submittingComment || !commentText.trim()}>
                    {submittingComment ? 'Enviando...' : 'Comentar'}
                  </button>
                </div>
              </form>

              <div className="post-detail-comment-list">
                {comments.length === 0 ? (
                  <p className="text-muted">Sé el primero en comentar esta publicación.</p>
                ) : (
                  commentTree.map((c) => renderCommentNode(c, 0))
                )}
              </div>
            </section>
          </article>
        )}
      </main>
    </div>
  );
}
