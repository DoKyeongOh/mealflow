import { PlusOutlined } from '@ant-design/icons'
import { Alert, Button, Space, Table, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import type { ColumnsType } from 'antd/es/table'
import dayjs from 'dayjs'
import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { listGroceryTypes } from '@/features/grocery-types/api/groceryTypeApi'
import { listInventoryItems } from '@/features/inventory-items/api/inventoryItemApi'
import type { InventoryItemResponse } from '@/features/inventory-items/model/types'
import { GROCERY_UNIT_LABELS } from '@/features/inventory-items/model/types'
import { listStorageLocations } from '@/features/storage-locations/api/storageLocationApi'
import { errorMessage } from '@/shared/lib/errorMessage'

const { Title } = Typography

export function InventoryItemListPage() {
  const navigate = useNavigate()

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['inventory-items'],
    queryFn: listInventoryItems,
  })

  const { data: types } = useQuery({
    queryKey: ['grocery-types'],
    queryFn: listGroceryTypes,
  })

  const { data: locations } = useQuery({
    queryKey: ['storage-locations'],
    queryFn: listStorageLocations,
  })

  const typeName = useMemo(() => {
    const m = new Map<number, string>()
    types?.forEach((t) => m.set(t.id, t.name))
    return m
  }, [types])

  const locName = useMemo(() => {
    const m = new Map<number, string>()
    locations?.forEach((l) => m.set(l.id, l.name))
    return m
  }, [locations])

  const columns: ColumnsType<InventoryItemResponse> = [
    { title: 'ID', dataIndex: 'id', width: 72 },
    {
      title: '식료품 종류',
      key: 'groceryName',
      render: (_, row) => typeName.get(row.groceryTypeId) ?? `#${row.groceryTypeId}`,
    },
    {
      title: '수량',
      key: 'qty',
      render: (_, row) => `${row.quantity} ${GROCERY_UNIT_LABELS[row.unit]}`,
    },
    {
      title: '유통기한',
      dataIndex: 'expirationDate',
      render: (d: string) => dayjs(d).format('YYYY-MM-DD'),
    },
    {
      title: '저장 장소',
      key: 'loc',
      render: (_, row) => locName.get(row.storageLocationId) ?? `#${row.storageLocationId}`,
    },
    {
      title: '작업',
      key: 'actions',
      width: 200,
      render: (_, row) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/inventory-items/${row.id}`)}>
            상세
          </Button>
          <Button type="link" onClick={() => navigate(`/inventory-items/${row.id}/edit`)}>
            수정
          </Button>
        </Space>
      ),
    },
  ]

  const soon = (d: string) => {
    const diff = dayjs(d).diff(dayjs(), 'day')
    return diff >= 0 && diff <= 7
  }

  return (
    <div style={{ maxWidth: 1040, margin: '0 auto', width: '100%', padding: '0 16px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Space style={{ justifyContent: 'space-between', width: '100%' }}>
          <Title level={3} style={{ margin: 0 }}>
            보관 항목
          </Title>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/inventory-items/new')}>
            추가
          </Button>
        </Space>
        {isError ? (
          <Alert type="error" showIcon message="목록을 불러오지 못했습니다." description={errorMessage(error)} />
        ) : null}
        <Table<InventoryItemResponse>
          rowKey="id"
          loading={isLoading}
          columns={columns}
          dataSource={data ?? []}
          pagination={false}
          onRow={(record) => ({
            onClick: () => navigate(`/inventory-items/${record.id}`),
            style: { cursor: 'pointer' },
          })}
          rowClassName={(record) => (soon(record.expirationDate) ? 'row-expiring-soon' : '')}
        />
        <style>{`.row-expiring-soon { background: #fffbe6; }`}</style>
      </Space>
    </div>
  )
}
