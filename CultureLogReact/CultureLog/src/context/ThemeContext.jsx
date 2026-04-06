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
  const [theme, setThemeState] = useState(
    () => localStorage.getItem('culturelog-theme') || 'DARK'
  );
  const [accentColor, setAccentColorState] = useState(
    () => localStorage.getItem('culturelog-accent') || '#448AFF'
  );
  const [loading, setLoading] = useState(true);
  const settingsRef = useRef(null);

  useEffect(() => {
    if (!isAuthenticated || !user?.id) {
      localStorage.removeItem('culturelog-theme');
      localStorage.removeItem('culturelog-accent');
      applyThemeToDOM('DARK', '#448AFF');
      setLoading(false);
      return;
    }

    let cancelled = false;
    (async () => {
      try {
        const { data } = await getUserSettings();
        if (cancelled) return;
        settingsRef.current = data;
        setSettings(data);
        const t = data.theme || 'DARK';
        const a = data.accentColor || '#448AFF';
        setThemeState(t);
        setAccentColorState(a);
        localStorage.setItem('culturelog-theme', t);
        localStorage.setItem('culturelog-accent', a);
        applyThemeToDOM(t, a);
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
    if (!isAuthenticated || !settingsRef.current) return;
    const merged = { ...settingsRef.current, ...patch };
    settingsRef.current = merged;
    setSettings(merged);
    try {
      await updateUserSettings(merged);
    } catch (err) {
      console.error('Error saving settings:', err);
    }
  }, [isAuthenticated]);

  const updateTheme = useCallback((newTheme) => {
    setThemeState(newTheme);
    localStorage.setItem('culturelog-theme', newTheme);
    applyThemeToDOM(newTheme, accentColor);
    return persistSettings({ theme: newTheme });
  }, [accentColor, persistSettings]);

  const updateAccentColor = useCallback((newColor) => {
    setAccentColorState(newColor);
    localStorage.setItem('culturelog-accent', newColor);
    document.documentElement.style.setProperty('--accent-color', newColor);
    return persistSettings({ accentColor: newColor });
  }, [persistSettings]);

  const updateSettings = useCallback((patch) => {
    return persistSettings(patch);
  }, [persistSettings]);

  const value = {
    theme,
    accentColor,
    settings,
    loading,
    updateTheme,
    updateAccentColor,
    updateSettings,
  };

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}
