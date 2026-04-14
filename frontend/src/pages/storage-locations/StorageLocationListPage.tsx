import { PlusOutlined } from '@ant-design/icons'
import { Alert, Button, Space, Table, Typography } from 'antd'
import { useQuery } from '@tanstack/react-query'
import type { ColumnsType } from 'antd/es/table'
import { useNavigate } from 'react-router-dom'
import { listStorageLocations } from '@/features/storage-locations/api/storageLocationApi'
import type { StorageLocationResponse } from '@/features/storage-locations/model/types'
import { errorMessage } from '@/shared/lib/errorMessage'

const { Title } = Typography

export function StorageLocationListPage() {
  const navigate = useNavigate()

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['storage-locations'],
    queryFn: listStorageLocations,
  })

  const columns: ColumnsType<StorageLocationResponse> = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '이름', dataIndex: 'name' },
    {
      title: '작업',
      key: 'actions',
      width: 200,
      render: (_, row) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/storage-locations/${row.id}`)}>
            상세
          </Button>
          <Button type="link" onClick={() => navigate(`/storage-locations/${row.id}/edit`)}>
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
            저장 장소
          </Title>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/storage-locations/new')}>
            추가
          </Button>
        </Space>
        {isError ? (
          <Alert type="error" showIcon message="목록을 불러오지 못했습니다." description={errorMessage(error)} />
        ) : null}
        <Table<StorageLocationResponse>
          rowKey="id"
          loading={isLoading}
          columns={columns}
          dataSource={data ?? []}
          pagination={false}
          onRow={(record) => ({
            onClick: () => navigate(`/storage-locations/${record.id}`),
            style: { cursor: 'pointer' },
          })}
        />
      </Space>
    </div>
  )
}
