import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Heart, MessageCircle, ArrowLeft } from 'lucide-react';
import { AppHeader } from '../components/AppHeader';
import { UserAvatar } from '../components/UserAvatar';
import { CommentSection } from '../components/CommentSection';
import { useAuth } from '../context/AuthContext';
import { MEDIA_TYPE_LABELS } from '../constants/media';
import { getPostById, getPostComments, togglePostLike } from '../services/api';
import '../App.css';

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

  const handleCommentCountChange = (delta) => {
    setPost((prev) => prev ? { ...prev, commentCount: (prev.commentCount || 0) + delta } : prev);
  };

  return (
    <div className="home-container">
      <AppHeader active="home" userName={user?.username} />
      <main className="feed post-detail-page">
        <button
          type="button"
          className="post-detail-back-btn"
          onClick={() => navigate(-1)}
          aria-label="Volver"
        >
          <ArrowLeft size={20} /> Volver
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

            <CommentSection
              postId={post.id}
              comments={comments}
              setComments={setComments}
              onCommentCountChange={handleCommentCountChange}
              loading={false}
              error=""
              showTitle
            />
          </article>
        )}
      </main>
    </div>
  );
}
