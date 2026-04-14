import { App, Button, Card, Space, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import { Link, Navigate, useSearchParams } from 'react-router-dom'
import { checkAuthenticated } from '@/features/auth/api/authApi'
import { startGoogleOAuth } from '@/features/auth/lib/startGoogleOAuth'

const { Paragraph, Title } = Typography

export function LoginPage() {
  const [searchParams] = useSearchParams()
  const from = searchParams.get('from') || '/inventory-items'
  const { message } = App.useApp()

  const { data: authed, isFetched } = useQuery({
    queryKey: ['auth', 'session'],
    queryFn: checkAuthenticated,
  })

  if (isFetched && authed) {
    return <Navigate to={from} replace />
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
              로그인
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              Google 계정으로 로그인합니다. 별도 이메일·비밀번호 가입은 제공하지 않습니다.
            </Paragraph>
            <Button type="primary" size="large" block onClick={onStartOAuth}>
              Google로 계속하기
            </Button>
            <div>
              <Link to="/signup">처음이신가요? 회원가입</Link>
            </div>
          </Space>
        </Card>
      </div>
    </div>
  )
}
