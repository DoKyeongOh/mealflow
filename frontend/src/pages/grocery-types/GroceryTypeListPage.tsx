import { PlusOutlined } from '@ant-design/icons'
import { Alert, Button, Space, Table, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import type { ColumnsType } from 'antd/es/table'
import { useNavigate } from 'react-router-dom'
import { listGroceryTypes } from '@/features/grocery-types/api/groceryTypeApi'
import type { GroceryTypeResponse } from '@/features/grocery-types/model/types'
import { listStorageLocations } from '@/features/storage-locations/api/storageLocationApi'
import { errorMessage } from '@/shared/lib/errorMessage'

const { Title } = Typography

export function GroceryTypeListPage() {
  const navigate = useNavigate()

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['grocery-types'],
    queryFn: listGroceryTypes,
  })

  const { data: locations } = useQuery({
    queryKey: ['storage-locations'],
    queryFn: listStorageLocations,
  })

  const locName = (id: number | null) => {
    if (id == null) return '—'
    return locations?.find((l) => l.id === id)?.name ?? `#${id}`
  }

  const columns: ColumnsType<GroceryTypeResponse> = [
    { title: 'ID', dataIndex: 'id', width: 72 },
    { title: '이름', dataIndex: 'name' },
    {
      title: '기본 저장 장소',
      dataIndex: 'defaultStorageLocationId',
      render: (v: number | null) => locName(v),
    },
    {
      title: '평균 유통기한(일)',
      dataIndex: 'defaultShelfLifeDays',
      render: (v: number | null) => (v != null ? v : '—'),
    },
    {
      title: '작업',
      key: 'actions',
      width: 200,
      render: (_, row) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/grocery-types/${row.id}`)}>
            상세
          </Button>
          <Button type="link" onClick={() => navigate(`/grocery-types/${row.id}/edit`)}>
            수정
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div style={{ maxWidth: 1040, margin: '0 auto', width: '100%', padding: '0 16px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Space style={{ justifyContent: 'space-between', width: '100%' }}>
          <Title level={3} style={{ margin: 0 }}>
            식료품 종류
          </Title>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/grocery-types/new')}>
            추가
          </Button>
        </Space>
        {isError ? (
          <Alert type="error" showIcon message="목록을 불러오지 못했습니다." description={errorMessage(error)} />
        ) : null}
        <Table<GroceryTypeResponse>
          rowKey="id"
          loading={isLoading}
          columns={columns}
          dataSource={data ?? []}
          pagination={false}
          onRow={(record) => ({
            onClick: () => navigate(`/grocery-types/${record.id}`),
            style: { cursor: 'pointer' },
          })}
        />
      </Space>
    </div>
  )
}
