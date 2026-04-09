import type { UserProfile } from '@/features/user/model/types'
import { mockUserProfile } from '@/features/user/mock/user.mock'

function delay(ms: number) {
  return new Promise<void>((resolve) => {
    setTimeout(resolve, ms)
  })
}

export async function fetchUserProfile(): Promise<UserProfile> {
  await delay(300)
  return { ...mockUserProfile }
}
