import { DeleteOutlined, EditOutlined } from '@ant-design/icons'
import { Alert, App, Button, Descriptions, Modal, Space, Spin, Typography } from 'antd'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  deleteGroceryType,
  getGroceryType,
  getReferenced,
} from '@/features/grocery-types/api/groceryTypeApi'
import { getStorageLocation } from '@/features/storage-locations/api/storageLocationApi'
import { ApiError } from '@/shared/lib/apiClient'
import { errorMessage } from '@/shared/lib/errorMessage'

const { Title } = Typography

export function GroceryTypeDetailPage() {
  const { id: groceryTypeIdParam } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const groceryTypeId = Number(groceryTypeIdParam)

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['grocery-types', groceryTypeId],
    queryFn: () => getGroceryType(groceryTypeId),
    enabled: Number.isFinite(groceryTypeId),
  })

  const { data: defaultLoc } = useQuery({
    queryKey: ['storage-locations', data?.defaultStorageLocationId],
    queryFn: () => getStorageLocation(data!.defaultStorageLocationId!),
    enabled: Boolean(data?.defaultStorageLocationId != null),
  })

  const deleteMutation = useMutation({
    mutationFn: async () => {
      const ref = await getReferenced(groceryTypeId)
      if (ref.referenced) {
        message.warning('보관 항목에서 사용 중이라 삭제할 수 없습니다.')
        throw new Error('referenced')
      }
      await deleteGroceryType(groceryTypeId)
    },
    onSuccess: () => {
      message.success('삭제했습니다.')
      queryClient.invalidateQueries({ queryKey: ['grocery-types'] })
      navigate('/grocery-types')
    },
    onError: (err) => {
      if (err instanceof Error && err.message === 'referenced') return
      if (err instanceof ApiError && err.status === 409) {
        message.error('참조가 있어 삭제할 수 없습니다.')
        return
      }
      message.error(errorMessage(err))
    },
  })

  const onDelete = () => {
    Modal.confirm({
      title: '식료품 종류를 삭제할까요?',
      okText: '삭제',
      okType: 'danger',
      cancelText: '취소',
      onOk: () => deleteMutation.mutateAsync(),
    })
  }

  if (!Number.isFinite(groceryTypeId)) {
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
            식료품 종류 상세
          </Title>
          <Space>
            <Link to="/grocery-types">목록</Link>
            <Button icon={<EditOutlined />} onClick={() => navigate(`/grocery-types/${groceryTypeId}/edit`)}>
              수정
            </Button>
            <Button danger icon={<DeleteOutlined />} loading={deleteMutation.isPending} onClick={onDelete}>
              삭제
            </Button>
          </Space>
        </Space>
        <Descriptions bordered column={1} size="small">
          <Descriptions.Item label="ID">{data.id}</Descriptions.Item>
          <Descriptions.Item label="이름">{data.name}</Descriptions.Item>
          <Descriptions.Item label="기본 저장 장소">
            {data.defaultStorageLocationId != null
              ? `${defaultLoc?.name ?? ''} (#${data.defaultStorageLocationId})`
              : '—'}
          </Descriptions.Item>
          <Descriptions.Item label="평균 유통기한(일)">
            {data.defaultShelfLifeDays != null ? data.defaultShelfLifeDays : '—'}
          </Descriptions.Item>
          <Descriptions.Item label="생성">{data.createdAt}</Descriptions.Item>
          <Descriptions.Item label="수정">{data.updatedAt}</Descriptions.Item>
        </Descriptions>
      </Space>
    </div>
  )
}
