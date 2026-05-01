import { Toaster } from 'sonner';
import { useTheme } from '../context/ThemeContext';

function resolveEffective(theme) {
  if (theme === 'SYSTEM')
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  return theme === 'LIGHT' ? 'light' : 'dark';
}

export default function ThemedToaster() {
  const { theme } = useTheme();
  return (
    <Toaster
      theme={resolveEffective(theme)}
      position="bottom-right"
      richColors
      closeButton
      visibleToasts={4}
      toastOptions={{ duration: 4000 }}
    />
  );
}
