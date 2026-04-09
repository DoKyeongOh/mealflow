import type { SessionUser } from '@/features/auth/model/types'
import { mockSessionUser } from '@/features/auth/mock/session.mock'

function delay(ms: number) {
  return new Promise<void>((resolve) => {
    setTimeout(resolve, ms)
  })
}

/** 실제 API 연동 시 이 파일의 구현만 교체한다. */
export async function fetchSessionUser(): Promise<SessionUser> {
  await delay(250)
  return { ...mockSessionUser }
}
