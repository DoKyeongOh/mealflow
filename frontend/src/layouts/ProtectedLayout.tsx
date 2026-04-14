import { Spin } from 'antd'
import { useQuery } from '@tanstack/react-query'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { checkAuthenticated } from '@/features/auth/api/authApi'

export function ProtectedLayout() {
  const location = useLocation()
  const { data: ok, isLoading } = useQuery({
    queryKey: ['auth', 'session'],
    queryFn: checkAuthenticated,
    staleTime: 60 * 1000,
  })

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', paddingTop: 120 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (!ok) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <Outlet />
}
