import { LogoutOutlined, MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons'
import { Button, Layout, Menu, theme } from 'antd'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useMemo } from 'react'
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { logoutPost } from '@/features/auth/api/authApi'
import { useAppDispatch, useAppSelector } from '@/shared/hooks'
import { toggleSidebar } from '@/app/store/uiSlice'
import { APP_NAME } from '@/shared/constants'

const { Header, Sider, Content } = Layout

function menuSelectedKey(pathname: string): string {
  if (pathname.startsWith('/inventory-items')) return '/inventory-items'
  if (pathname.startsWith('/storage-locations')) return '/storage-locations'
  if (pathname.startsWith('/grocery-types')) return '/grocery-types'
  return pathname
}

export function MainLayout() {
  const location = useLocation()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const dispatch = useAppDispatch()
  const collapsed = useAppSelector((s) => s.ui.sidebarCollapsed)
  const { token } = theme.useToken()

  const logoutMutation = useMutation({
    mutationFn: logoutPost,
    onSettled: () => {
      queryClient.removeQueries({ queryKey: ['auth'] })
      navigate('/login', { replace: true })
    },
  })

  const selectedKey = useMemo(() => menuSelectedKey(location.pathname), [location.pathname])

  const menuItems = useMemo(
    () => [
      {
        key: '/inventory-items',
        label: <Link to="/inventory-items">보관 항목</Link>,
      },
      {
        key: '/storage-locations',
        label: <Link to="/storage-locations">저장 장소</Link>,
      },
      {
        key: '/grocery-types',
        label: <Link to="/grocery-types">식료품 종류</Link>,
      },
    ],
    [],
  )

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        breakpoint="lg"
        style={{ background: token.colorBgContainer }}
      >
        <div
          style={{
            height: 48,
            margin: 12,
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'flex-start',
            fontWeight: 600,
            fontSize: 15,
          }}
        >
          <Link to="/inventory-items" style={{ color: 'inherit' }}>
            {collapsed ? 'M' : APP_NAME}
          </Link>
        </div>
        <Menu mode="inline" selectedKeys={[selectedKey]} items={menuItems} style={{ borderInlineEnd: 0 }} />
      </Sider>
      <Layout>
        <Header
          style={{
            padding: '0 16px',
            background: token.colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            borderBottom: `1px solid ${token.colorBorderSecondary}`,
          }}
        >
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => dispatch(toggleSidebar())}
            aria-label="사이드바 접기"
          />
          <span style={{ fontWeight: 500 }}>Mealflow</span>
          <div style={{ marginLeft: 'auto' }}>
            <Button
              type="text"
              icon={<LogoutOutlined />}
              loading={logoutMutation.isPending}
              onClick={() => logoutMutation.mutate()}
            >
              로그아웃
            </Button>
          </div>
        </Header>
        <Content style={{ padding: 24, background: token.colorBgLayout }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
