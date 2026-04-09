import { Tag } from 'antd'
import type { SessionUser } from '@/features/auth/model/types'

export function AuthStatusBadge({ user }: { user: SessionUser | null }) {
  if (!user) {
    return <Tag>비로그인(mock)</Tag>
  }
  return <Tag color="blue">{user.displayName}</Tag>
}
