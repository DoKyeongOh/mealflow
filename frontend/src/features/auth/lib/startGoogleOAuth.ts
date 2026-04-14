import { getGoogleOAuthUrl } from '@/features/auth/api/authApi'

export function startGoogleOAuth(onInvalidUrl?: () => void): void {
  const url = getGoogleOAuthUrl()
  if (!url.startsWith('http')) {
    onInvalidUrl?.()
    return
  }
  window.location.assign(url)
}
