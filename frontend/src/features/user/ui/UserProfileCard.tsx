import { Card, Typography } from 'antd'
import type { UserProfile } from '@/features/user/model/types'

const { Paragraph, Title } = Typography

export function UserProfileCard({ profile }: { profile: UserProfile }) {
  return (
    <Card title="프로필" style={{ maxWidth: 480 }}>
      <Title level={4}>{profile.name}</Title>
      <Paragraph type="secondary">{profile.bio}</Paragraph>
    </Card>
  )
}
