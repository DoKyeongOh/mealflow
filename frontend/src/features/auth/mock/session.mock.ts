import type { SessionUser } from '@/features/auth/model/types'

/** mock은 컴포넌트가 아닌 mock 전용 모듈에 둔다. */
export const mockSessionUser: SessionUser = {
  id: 'user-mock-1',
  email: 'demo@example.com',
  displayName: '데모 사용자',
}
