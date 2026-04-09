/** 백엔드 스펙이 정해지면 실제 요청/응답 형태에 맞춘다. */
export interface SessionUser {
  id: string
  email: string
  displayName: string
}
