import { Button, Flex, Space, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@/shared/hooks'
import { toggleSidebar } from '@/app/store/uiSlice'
import { APP_NAME } from '@/shared/constants'
import { AuthStatusBadge } from '@/features/auth/ui/AuthStatusBadge'
import { fetchSessionUser } from '@/features/auth/api/authApi'

const { Title, Text } = Typography

export function HomePage() {
  const dispatch = useAppDispatch()
  const sidebarCollapsed = useAppSelector((s) => s.ui.sidebarCollapsed)

  const { data: sessionUser } = useQuery({
    queryKey: ['auth', 'session'],
    queryFn: fetchSessionUser,
  })

  return (
    <Flex vertical gap="large" style={{ padding: 24, maxWidth: 720 }}>
      <div>
        <Title level={2}>{APP_NAME}</Title>
        <Text type="secondary">
          feature 기반 폴더와 mock 분리, TanStack Query + Redux Toolkit + Ant Design
          예시입니다.
        </Text>
      </div>

      <Space wrap>
        <AuthStatusBadge user={sessionUser ?? null} />
        <Text>
          사이드바(mock 상태): {sidebarCollapsed ? '접힘' : '펼침'}
        </Text>
        <Button type="primary" onClick={() => dispatch(toggleSidebar())}>
          UI 토글 (Redux)
        </Button>
        <Link to="/user">
          <Button>사용자 페이지</Button>
        </Link>
      </Space>
    </Flex>
  )
}
