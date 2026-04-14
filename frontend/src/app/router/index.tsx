import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import { MainLayout } from '@/layouts/MainLayout'
import { ProtectedLayout } from '@/layouts/ProtectedLayout'
import { LoginPage } from '@/pages/auth/LoginPage'
import { SignupPage } from '@/pages/auth/SignupPage'
import { GroceryTypeDetailPage } from '@/pages/grocery-types/GroceryTypeDetailPage'
import { GroceryTypeFormPage } from '@/pages/grocery-types/GroceryTypeFormPage'
import { GroceryTypeListPage } from '@/pages/grocery-types/GroceryTypeListPage'
import { InventoryItemDetailPage } from '@/pages/inventory-items/InventoryItemDetailPage'
import { InventoryItemFormPage } from '@/pages/inventory-items/InventoryItemFormPage'
import { InventoryItemListPage } from '@/pages/inventory-items/InventoryItemListPage'
import { StorageLocationDetailPage } from '@/pages/storage-locations/StorageLocationDetailPage'
import { StorageLocationFormPage } from '@/pages/storage-locations/StorageLocationFormPage'
import { StorageLocationListPage } from '@/pages/storage-locations/StorageLocationListPage'

const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/signup', element: <SignupPage /> },
  {
    element: <ProtectedLayout />,
    children: [
      {
        path: '/',
        element: <MainLayout />,
        children: [
          { index: true, element: <Navigate to="/inventory-items" replace /> },
          { path: 'inventory-items', element: <InventoryItemListPage /> },
          { path: 'inventory-items/new', element: <InventoryItemFormPage /> },
          { path: 'inventory-items/:id/edit', element: <InventoryItemFormPage /> },
          { path: 'inventory-items/:id', element: <InventoryItemDetailPage /> },
          { path: 'storage-locations', element: <StorageLocationListPage /> },
          { path: 'storage-locations/new', element: <StorageLocationFormPage /> },
          { path: 'storage-locations/:id/edit', element: <StorageLocationFormPage /> },
          { path: 'storage-locations/:id', element: <StorageLocationDetailPage /> },
          { path: 'grocery-types', element: <GroceryTypeListPage /> },
          { path: 'grocery-types/new', element: <GroceryTypeFormPage /> },
          { path: 'grocery-types/:id/edit', element: <GroceryTypeFormPage /> },
          { path: 'grocery-types/:id', element: <GroceryTypeDetailPage /> },
        ],
      },
    ],
  },
])

export function AppRouter() {
  return <RouterProvider router={router} />
}
