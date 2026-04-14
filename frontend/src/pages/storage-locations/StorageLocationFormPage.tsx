import { App, Button, Form, Input, Space, Spin, Typography } from 'antd'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  createStorageLocation,
  getStorageLocation,
  updateStorageLocation,
} from '@/features/storage-locations/api/storageLocationApi'
import { errorMessage } from '@/shared/lib/errorMessage'

const { Title } = Typography

type FormValues = { name: string }

export function StorageLocationFormPage() {
  const { id: storageLocationIdParam } = useParams<{ id?: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const [form] = Form.useForm<FormValues>()
  const isEdit = Boolean(storageLocationIdParam)
  const storageLocationId = storageLocationIdParam ? Number(storageLocationIdParam) : NaN

  const { data, isLoading } = useQuery({
    queryKey: ['storage-locations', storageLocationId],
    queryFn: () => getStorageLocation(storageLocationId),
    enabled: isEdit && Number.isFinite(storageLocationId),
  })

  const createMutation = useMutation({
    mutationFn: (values: FormValues) => createStorageLocation({ name: values.name.trim() }),
    onSuccess: (res) => {
      message.success('저장했습니다.')
      queryClient.invalidateQueries({ queryKey: ['storage-locations'] })
      navigate(`/storage-locations/${res.id}`)
    },
    onError: (e) => message.error(errorMessage(e)),
  })

  const updateMutation = useMutation({
    mutationFn: (values: FormValues) =>
      updateStorageLocation(storageLocationId, { name: values.name.trim() }),
    onSuccess: (res) => {
      message.success('저장했습니다.')
      queryClient.invalidateQueries({ queryKey: ['storage-locations'] })
      queryClient.invalidateQueries({ queryKey: ['storage-locations', res.id] })
      navigate(`/storage-locations/${res.id}`)
    },
    onError: (e) => message.error(errorMessage(e)),
  })

  if (isEdit && !Number.isFinite(storageLocationId)) {
    return (
      <div style={{ maxWidth: 1040, margin: '0 auto', width: '100%', padding: '0 16px' }}>
        <Typography.Text type="danger">잘못된 ID입니다.</Typography.Text>
      </div>
    )
  }

  if (isEdit && isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 48 }}>
        <Spin size="large" />
      </div>
    )
  }

  const initialValues = data ? { name: data.name } : undefined

  const onFinish = (values: FormValues) => {
    if (isEdit) {
      updateMutation.mutate(values)
    } else {
      createMutation.mutate(values)
    }
  }

  const pending = createMutation.isPending || updateMutation.isPending

  return (
    <div style={{ maxWidth: 1040, margin: '0 auto', width: '100%', padding: '0 16px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%', maxWidth: 480 }}>
        <Title level={3}>{isEdit ? '저장 장소 수정' : '저장 장소 추가'}</Title>
        <Link to={isEdit ? `/storage-locations/${storageLocationId}` : '/storage-locations'}>← 돌아가기</Link>
        <Form<FormValues>
          form={form}
          layout="vertical"
          initialValues={initialValues}
          onFinish={onFinish}
          key={data?.id ?? 'new'}
        >
          <Form.Item
            name="name"
            label="이름"
            rules={[{ required: true, message: '이름을 입력하세요.' }]}
          >
            <Input maxLength={255} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={pending}>
              저장
            </Button>
          </Form.Item>
        </Form>
      </Space>
    </div>
  )
}
