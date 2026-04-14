import { apiFetch, apiGet } from '@/shared/lib/apiClient'
import { getBackendOrigin } from '@/shared/lib/env'

type AuthSessionResponse = { authenticated: boolean }

/** 인증 상태 조회 (permitAll 엔드포인트) */
export async function checkAuthenticated(): Promise<boolean> {
  const res = await apiGet<AuthSessionResponse>('/api/v1/auth/session')
  return res.authenticated
}

/** 브라우저 전체 이동용 Google OAuth 시작 URL */
export function getGoogleOAuthUrl(): string {
  return `${getBackendOrigin()}/oauth2/authorization/google`
}

/** Spring Security 기본 로그아웃 */
export async function logoutPost(): Promise<void> {
  const res = await apiFetch('/logout', {
    method: 'POST',
  })
  if (!res.ok && res.status !== 302) {
    throw new Error(`Logout failed: ${res.status}`)
  }
}

/** 세션 확인용 경량 호출 (선택) */
export async function pingApi(): Promise<void> {
  await apiGet<unknown[]>('/api/v1/storage-locations')
}
