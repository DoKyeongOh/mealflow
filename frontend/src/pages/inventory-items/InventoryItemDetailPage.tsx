import { DeleteOutlined, EditOutlined } from '@ant-design/icons'
import { Alert, App, Button, Descriptions, Modal, Space, Spin, Table, Typography } from 'antd'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { ColumnsType } from 'antd/es/table'
import dayjs from 'dayjs'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getGroceryType } from '@/features/grocery-types/api/groceryTypeApi'
import {
  deleteInventoryItem,
  getInventoryItem,
  listItemEvents,
} from '@/features/inventory-items/api/inventoryItemApi'
import type { InventoryEventResponse } from '@/features/inventory-items/model/types'
import { GROCERY_UNIT_LABELS } from '@/features/inventory-items/model/types'
import { getStorageLocation } from '@/features/storage-locations/api/storageLocationApi'
import { errorMessage } from '@/shared/lib/errorMessage'

const { Title } = Typography

export function InventoryItemDetailPage() {
  const { id: inventoryItemIdParam } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const inventoryItemId = Number(inventoryItemIdParam)

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['inventory-items', inventoryItemId],
    queryFn: () => getInventoryItem(inventoryItemId),
    enabled: Number.isFinite(inventoryItemId),
  })

  const { data: grocery } = useQuery({
    queryKey: ['grocery-types', data?.groceryTypeId],
    queryFn: () => getGroceryType(data!.groceryTypeId),
    enabled: Boolean(data?.groceryTypeId),
  })

  const { data: storage } = useQuery({
    queryKey: ['storage-locations', data?.storageLocationId],
    queryFn: () => getStorageLocation(data!.storageLocationId),
    enabled: Boolean(data?.storageLocationId),
  })

  const { data: events, isLoading: eventsLoading } = useQuery({
    queryKey: ['inventory-items', inventoryItemId, 'events'],
    queryFn: () => listItemEvents(inventoryItemId, 50),
    enabled: Number.isFinite(inventoryItemId),
  })

  const deleteMutation = useMutation({
    mutationFn: () => deleteInventoryItem(inventoryItemId),
    onSuccess: () => {
      message.success('삭제했습니다.')
      queryClient.invalidateQueries({ queryKey: ['inventory-items'] })
      navigate('/inventory-items')
    },
    onError: (e) => message.error(errorMessage(e)),
  })

  const onDelete = () => {
    Modal.confirm({
      title: '보관 항목을 삭제할까요?',
      okText: '삭제',
      okType: 'danger',
      cancelText: '취소',
      onOk: () => deleteMutation.mutateAsync(),
    })
  }

  const eventColumns: ColumnsType<InventoryEventResponse> = [
    { title: '시각', dataIndex: 'occurredAt', width: 200, render: (t: string) => dayjs(t).format('YYYY-MM-DD HH:mm:ss') },
    { title: '이전', dataIndex: 'countBefore', render: (v: number | null) => v ?? '—' },
    { title: '변화', dataIndex: 'countDiff' },
    { title: '이후', dataIndex: 'countAfter', render: (v: number | null) => v ?? '—' },
    { title: '단위', dataIndex: 'unit', render: (u) => GROCERY_UNIT_LABELS[u as keyof typeof GROCERY_UNIT_LABELS] },
  ]

  if (!Number.isFinite(inventoryItemId)) {
    return (
      <div style={{ maxWidth: 1040, margin: '0 auto', width: '100%', padding: '0 16px' }}>
        <Alert type="error" message="잘못된 ID입니다." />
      </div>
    )
  }

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 48 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (isError || !data) {
    return (
      <div style={{ maxWidth: 1040, margin: '0 auto', width: '100%', padding: '0 16px' }}>
        <Typography.Text type="danger">{errorMessage(error)}</Typography.Text>
      </div>
    )
  }

  return (
    <div style={{ maxWidth: 1040, margin: '0 auto', width: '100%', padding: '0 16px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Space style={{ justifyContent: 'space-between', width: '100%', flexWrap: 'wrap' }}>
          <Title level={3} style={{ margin: 0 }}>
            보관 항목 상세
          </Title>
          <Space>
            <Link to="/inventory-items">목록</Link>
            <Button icon={<EditOutlined />} onClick={() => navigate(`/inventory-items/${inventoryItemId}/edit`)}>
              수정
            </Button>
            <Button danger icon={<DeleteOutlined />} loading={deleteMutation.isPending} onClick={onDelete}>
              삭제
            </Button>
          </Space>
        </Space>
        <Descriptions bordered column={1} size="small">
          <Descriptions.Item label="ID">{data.id}</Descriptions.Item>
          <Descriptions.Item label="식료품 종류">
            {grocery ? grocery.name : `#${data.groceryTypeId}`}
          </Descriptions.Item>
          <Descriptions.Item label="수량">
            {data.quantity} {GROCERY_UNIT_LABELS[data.unit]}
          </Descriptions.Item>
          <Descriptions.Item label="유통기한">{dayjs(data.expirationDate).format('YYYY-MM-DD')}</Descriptions.Item>
          <Descriptions.Item label="저장 장소">
            {storage ? storage.name : `#${data.storageLocationId}`}
          </Descriptions.Item>
          <Descriptions.Item label="생성">{data.createdAt}</Descriptions.Item>
          <Descriptions.Item label="수정">{data.updatedAt}</Descriptions.Item>
        </Descriptions>

        <Title level={4}>변경 이력</Title>
        <Table<InventoryEventResponse>
          size="small"
          rowKey="id"
          loading={eventsLoading}
          columns={eventColumns}
          dataSource={events ?? []}
          pagination={false}
        />
      </Space>
    </div>
  )
}
