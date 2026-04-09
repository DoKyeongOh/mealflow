import { Button, Flex, Spin, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { fetchUserProfile } from '@/features/user/api/userApi'
import { UserProfileCard } from '@/features/user/ui/UserProfileCard'

const { Title } = Typography

export function UserPage() {
  const { data, isPending, isError, error, refetch } = useQuery({
    queryKey: ['user', 'profile'],
    queryFn: fetchUserProfile,
  })

  return (
    <Flex vertical gap="large" style={{ padding: 24 }}>
      <Flex align="center" justify="space-between" wrap="wrap" gap="small">
        <Title level={3} style={{ margin: 0 }}>
          사용자
        </Title>
        <Link to="/">
          <Button type="link">홈으로</Button>
        </Link>
      </Flex>

      {isPending && <Spin />}
      {isError && (
        <Typography.Text type="danger">
          {(error as Error).message}
        </Typography.Text>
      )}
      {data && <UserProfileCard profile={data} />}

      <div>
        <Button onClick={() => refetch()}>다시 불러오기</Button>
      </div>
    </Flex>
  )
}
