import { useEffect, useState } from 'react';
import '../App.css';

function initialsFromName(name) {
  if (!name || typeof name !== 'string') return '?';
  const parts = name.trim().split(/\s+/);
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase();
  }
  return name.slice(0, 2).toUpperCase();
}

export function UserAvatar({ src, alt, name, className = '', size = 'small' }) {
  const [imgError, setImgError] = useState(false);
  const sizeClass = size === 'large' ? 'user-avatar-large' : 'user-avatar-small';

  useEffect(() => setImgError(false), [src]);

  if (src && !imgError) {
    return (
      <img
        src={src}
        alt={alt || name || 'Usuario'}
        className={`user-avatar-img ${sizeClass} ${className}`.trim()}
        onError={() => setImgError(true)}
      />
    );
  }
  const label = initialsFromName(name || alt);
  return (
    <span
      className={`user-avatar-placeholder ${sizeClass} ${className}`.trim()}
      aria-hidden={!alt}
      title={alt || name}
    >
      {label}
    </span>
  );
}
