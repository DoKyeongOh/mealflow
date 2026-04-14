/** 백엔드 REST API 베이스 (스킴+호스트+포트). 예: http://localhost:8080 */
export function getApiBaseUrl(): string {
  return import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
}

/** OAuth·로그아웃 등 브라우저 네비게이션용 백엔드 원점 (보통 API와 동일) */
export function getBackendOrigin(): string {
  return getApiBaseUrl().replace(/\/$/, '')
}
