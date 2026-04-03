import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import { getUserSettings, updateUserSettings } from '../services/api';
import { useAuth } from './AuthContext';

const ThemeContext = createContext(null);

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme must be used within ThemeProvider');
  return ctx;
}

function resolveEffectiveTheme(theme) {
  if (theme !== 'SYSTEM') return theme === 'LIGHT' ? 'light' : 'dark';
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function applyThemeToDOM(theme, accentColor) {
  document.documentElement.dataset.theme = resolveEffectiveTheme(theme);
  if (accentColor) {
    document.documentElement.style.setProperty('--accent-color', accentColor);
  }
}

export function ThemeProvider({ children }) {
  const { user, isAuthenticated } = useAuth();
  const [settings, setSettings] = useState(null);
  const [theme, setThemeState] = useState('DARK');
  const [accentColor, setAccentColorState] = useState('#448AFF');
  const [loading, setLoading] = useState(true);
  const settingsRef = useRef(null);

  useEffect(() => {
    if (!isAuthenticated || !user?.id) {
      applyThemeToDOM('DARK', '#448AFF');
      setLoading(false);
      return;
    }

    let cancelled = false;
    (async () => {
      try {
        const { data } = await getUserSettings(user.id);
        if (cancelled) return;
        settingsRef.current = data;
        setSettings(data);
        setThemeState(data.theme || 'DARK');
        setAccentColorState(data.accentColor || '#448AFF');
        applyThemeToDOM(data.theme || 'DARK', data.accentColor || '#448AFF');
      } catch {
        applyThemeToDOM('DARK', '#448AFF');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => { cancelled = true; };
  }, [isAuthenticated, user?.id]);

  useEffect(() => {
    if (theme !== 'SYSTEM') return;
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    const handler = () => applyThemeToDOM('SYSTEM', accentColor);
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, [theme, accentColor]);

  const persistSettings = useCallback(async (patch) => {
    if (!user?.id || !settingsRef.current) return;
    const merged = { ...settingsRef.current, ...patch };
    settingsRef.current = merged;
    setSettings(merged);
    try {
      await updateUserSettings(user.id, merged);
    } catch (err) {
      console.error('Error saving settings:', err);
    }
  }, [user?.id]);

  const updateTheme = useCallback((newTheme) => {
    setThemeState(newTheme);
    applyThemeToDOM(newTheme, accentColor);
    persistSettings({ theme: newTheme });
  }, [accentColor, persistSettings]);

  const updateAccentColor = useCallback((newColor) => {
    setAccentColorState(newColor);
    document.documentElement.style.setProperty('--accent-color', newColor);
    persistSettings({ accentColor: newColor });
  }, [persistSettings]);

  const value = {
    theme,
    accentColor,
    settings,
    loading,
    updateTheme,
    updateAccentColor,
  };

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}
