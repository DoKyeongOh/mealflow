import { App, Button, Card, Space, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import { Link, Navigate } from 'react-router-dom'
import { checkAuthenticated } from '@/features/auth/api/authApi'
import { startGoogleOAuth } from '@/features/auth/lib/startGoogleOAuth'

const { Paragraph, Title } = Typography

export function SignupPage() {
  const { message } = App.useApp()

  const { data: authed, isFetched } = useQuery({
    queryKey: ['auth', 'session'],
    queryFn: checkAuthenticated,
  })

  if (isFetched && authed) {
    return <Navigate to="/inventory-items" replace />
  }

  const onStartOAuth = () => {
    startGoogleOAuth(() => message.error('백엔드 URL이 설정되지 않았습니다.'))
  }

  return (
    <div style={{ maxWidth: 1040, margin: '0 auto', width: '100%', padding: '0 16px' }}>
      <div style={{ maxWidth: 440, margin: '48px auto' }}>
        <Card>
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Title level={3} style={{ margin: 0 }}>
              회원가입
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              Google로 처음 로그인하면 계정이 자동으로 생성됩니다. 로그인과 동일한 OAuth 흐름을
              사용합니다.
            </Paragraph>
            <Button type="primary" size="large" block onClick={onStartOAuth}>
              Google로 가입하기
            </Button>
            <div>
              <Link to="/login">이미 계정이 있나요? 로그인</Link>
            </div>
          </Space>
        </Card>
      </div>
    </div>
  )
}
