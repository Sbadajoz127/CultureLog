import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Heart, MessageCircle, Expand } from 'lucide-react';
import { UserAvatar } from './UserAvatar';
import { CommentSection } from './CommentSection';
import { MEDIA_TYPE_LABELS } from '../constants/media';
import { getPostComments, togglePostLike } from '../services/api';

const CONTENT_TRUNCATE_LENGTH = 300;

function formatDate(iso) {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
  } catch {
    return iso;
  }
}

export function PostCard({ post, onPostUpdate, authorAvatar, showAuthorLink = true }) {
  const navigate = useNavigate();

  const [commentsOpen, setCommentsOpen] = useState(false);
  const [comments, setComments] = useState([]);
  const [commentsLoaded, setCommentsLoaded] = useState(false);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [commentsError, setCommentsError] = useState('');
  const [liking, setLiking] = useState(false);
  const [expanded, setExpanded] = useState(false);

  const avatarUrl = authorAvatar ?? post.authorProfilePictureUrl;
  const isLiked = post.likedByCurrentUser;
  const content = post.content || '';
  const isTruncated = content.length > CONTENT_TRUNCATE_LENGTH && !expanded;

  const handleLike = async () => {
    if (liking) return;
    setLiking(true);
    try {
      const { data } = await togglePostLike(post.id);
      onPostUpdate?.(post.id, { likedByCurrentUser: data.liked, likeCount: data.likeCount });
    } catch {
      /* ignore */
    } finally {
      setLiking(false);
    }
  };

  const toggleComments = useCallback(async () => {
    if (commentsOpen) {
      setCommentsOpen(false);
      return;
    }
    setCommentsOpen(true);
    if (commentsLoaded) return;
    setCommentsLoading(true);
    setCommentsError('');
    try {
      const { data } = await getPostComments(post.id);
      setComments(Array.isArray(data) ? data : []);
      setCommentsLoaded(true);
    } catch (e) {
      setCommentsError(e.response?.data?.message || e.response?.data?.error || 'No se pudieron cargar los comentarios.');
    } finally {
      setCommentsLoading(false);
    }
  }, [commentsOpen, commentsLoaded, post.id]);

  const handleCommentCountChange = (delta) => {
    onPostUpdate?.(post.id, { commentCount: (post.commentCount || 0) + delta });
  };

  return (
    <article className="post-card">
      <div className="post-card-header">
        <div className="post-card-author">
          <UserAvatar src={avatarUrl} name={post.authorName} size="small" />
          <div>
            {showAuthorLink ? (
              <h4
                className="post-author post-author-link"
                role="button"
                tabIndex={0}
                onClick={() => navigate(`/user/${post.authorName}`)}
                onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/user/${post.authorName}`); }}
              >
                {post.authorName}
              </h4>
            ) : (
              <h4 className="post-author">{post.authorName}</h4>
            )}
            {post.linkedItemType && (
              <span className="post-category-tag">
                {MEDIA_TYPE_LABELS[post.linkedItemType] || post.linkedItemType}
              </span>
            )}
          </div>
        </div>
        <span className="text-dim post-date">{formatDate(post.createdAt)}</span>
      </div>

      {(post.linkedItemTitle || post.linkedItemId) && (
        <div className="post-linked-work">
          Reseña de: <strong>{post.linkedItemTitle}</strong>
          {post.linkedItemRating != null && (
            <span className="text-muted"> · {post.linkedItemRating}/10</span>
          )}
        </div>
      )}

      <p className="post-content">
        {isTruncated ? (
          <>
            {content.slice(0, CONTENT_TRUNCATE_LENGTH)}...{' '}
            <span
              className="post-read-more"
              role="button"
              tabIndex={0}
              onClick={() => navigate(`/posts/${post.id}`)}
              onKeyDown={(e) => { if (e.key === 'Enter') navigate(`/posts/${post.id}`); }}
            >
              leer más
            </span>
          </>
        ) : (
          content
        )}
      </p>

      <div className="post-footer">
        <button
          type="button"
          className={`post-like-btn ${isLiked ? 'liked' : ''}`}
          onClick={handleLike}
          disabled={liking}
        >
          <Heart size={16} fill={isLiked ? 'currentColor' : 'none'} /> {post.likeCount} Me gusta
        </button>
        <div className="post-footer-actions">
          <button
            type="button"
            className={`post-action-btn ${commentsOpen ? 'active' : ''}`}
            onClick={toggleComments}
          >
            <MessageCircle size={16} /> {post.commentCount || 0} comentarios
          </button>
          <button
            type="button"
            className="post-action-btn"
            onClick={() => navigate(`/posts/${post.id}`)}
          >
            <Expand size={16} /> Ver publicación
          </button>
        </div>
      </div>

      {commentsOpen && (
        <>
          <div className="post-comments-separator" />
          <CommentSection
            postId={post.id}
            comments={comments}
            setComments={setComments}
            onCommentCountChange={handleCommentCountChange}
            loading={commentsLoading}
            error={commentsError}
            showTitle
          />
        </>
      )}
    </article>
  );
}
