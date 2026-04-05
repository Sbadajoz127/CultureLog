import { createContext, useContext, useState } from 'react';

const ProfilePicContext = createContext(null);

export function useProfilePic() {
  const ctx = useContext(ProfilePicContext);
  if (!ctx) throw new Error('useProfilePic must be used within ProfilePicProvider');
  return ctx;
}

export function ProfilePicProvider({ children }) {
  const [profilePic, setProfilePic] = useState(null);
  return (
    <ProfilePicContext.Provider value={{ profilePic, setProfilePic }}>
      {children}
    </ProfilePicContext.Provider>
  );
}
