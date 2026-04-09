import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import { HomePage } from '@/pages/HomePage/HomePage'
import { UserPage } from '@/pages/UserPage/UserPage'

const router = createBrowserRouter([
  { path: '/', element: <HomePage /> },
  { path: '/user', element: <UserPage /> },
])

export function AppRouter() {
  return <RouterProvider router={router} />
}
