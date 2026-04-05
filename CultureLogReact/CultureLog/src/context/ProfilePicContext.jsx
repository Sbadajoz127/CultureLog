import { createContext, useContext, useEffect, useState } from 'react';
import { useAuth } from './AuthContext';

const ProfilePicContext = createContext(null);

export function useProfilePic() {
  const ctx = useContext(ProfilePicContext);
  if (!ctx) throw new Error('useProfilePic must be used within ProfilePicProvider');
  return ctx;
}

export function ProfilePicProvider({ children }) {
  const { user } = useAuth();
  const [profilePic, setProfilePic] = useState(user?.profilePictureUrl ?? null);

  useEffect(() => {
    setProfilePic(user?.profilePictureUrl ?? null);
  }, [user?.profilePictureUrl]);

  return (
    <ProfilePicContext.Provider value={{ profilePic, setProfilePic }}>
      {children}
    </ProfilePicContext.Provider>
  );
}
