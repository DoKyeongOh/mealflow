import { App, Button, Form, Input, InputNumber, Select, Space, Spin, Typography } from 'antd'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  createGroceryType,
  getGroceryType,
  updateGroceryType,
} from '@/features/grocery-types/api/groceryTypeApi'
import { listStorageLocations } from '@/features/storage-locations/api/storageLocationApi'
import { errorMessage } from '@/shared/lib/errorMessage'

const { Title } = Typography

type FormValues = {
  name: string
  defaultStorageLocationId: number
  defaultShelfLifeDays: number | null
}

export function GroceryTypeFormPage() {
  const { id: groceryTypeIdParam } = useParams<{ id?: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const [form] = Form.useForm<FormValues>()
  const isEdit = Boolean(groceryTypeIdParam)
  const groceryTypeId = groceryTypeIdParam ? Number(groceryTypeIdParam) : NaN

  const { data: locations, isLoading: locLoading } = useQuery({
    queryKey: ['storage-locations'],
    queryFn: listStorageLocations,
  })

  const { data, isLoading } = useQuery({
    queryKey: ['grocery-types', groceryTypeId],
    queryFn: () => getGroceryType(groceryTypeId),
    enabled: isEdit && Number.isFinite(groceryTypeId),
  })

  const createMutation = useMutation({
    mutationFn: (values: FormValues) =>
      createGroceryType({
        name: values.name.trim(),
        defaultStorageLocationId: values.defaultStorageLocationId,
        defaultShelfLifeDays: values.defaultShelfLifeDays ?? null,
      }),
    onSuccess: (res) => {
      message.success('저장했습니다.')
      queryClient.invalidateQueries({ queryKey: ['grocery-types'] })
      navigate(`/grocery-types/${res.id}`)
    },
    onError: (e) => message.error(errorMessage(e)),
  })

  const updateMutation = useMutation({
    mutationFn: (values: FormValues) =>
      updateGroceryType(groceryTypeId, {
        name: values.name.trim(),
        defaultStorageLocationId: values.defaultStorageLocationId,
        defaultShelfLifeDays: values.defaultShelfLifeDays ?? null,
      }),
    onSuccess: (res) => {
      message.success('저장했습니다.')
      queryClient.invalidateQueries({ queryKey: ['grocery-types'] })
      queryClient.invalidateQueries({ queryKey: ['grocery-types', res.id] })
      navigate(`/grocery-types/${res.id}`)
    },
    onError: (e) => message.error(errorMessage(e)),
  })

  if (isEdit && !Number.isFinite(groceryTypeId)) {
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

  const initialValues: Partial<FormValues> | undefined = data
    ? {
        name: data.name,
        defaultStorageLocationId: data.defaultStorageLocationId ?? undefined,
        defaultShelfLifeDays: data.defaultShelfLifeDays,
      }
    : undefined

  const onFinish = (values: FormValues) => {
    if (isEdit) {
      updateMutation.mutate(values)
    } else {
      createMutation.mutate(values)
    }
  }

  const pending = createMutation.isPending || updateMutation.isPending

  const locationOptions =
    locations?.map((l) => ({ value: l.id, label: l.name })) ?? []

  return (
    <div style={{ maxWidth: 1040, margin: '0 auto', width: '100%', padding: '0 16px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%', maxWidth: 520 }}>
        <Title level={3}>{isEdit ? '식료품 종류 수정' : '식료품 종류 추가'}</Title>
        <Link to={isEdit ? `/grocery-types/${groceryTypeId}` : '/grocery-types'}>← 돌아가기</Link>
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
          <Form.Item
            name="defaultStorageLocationId"
            label="기본 저장 장소"
            rules={[{ required: true, message: '기본 저장 장소를 선택하세요.' }]}
          >
            <Select
              showSearch
              optionFilterProp="label"
              options={locationOptions}
              loading={locLoading}
              placeholder="저장 장소를 먼저 등록해야 합니다"
            />
          </Form.Item>
          <Form.Item name="defaultShelfLifeDays" label="평균 유통기한 기본값(일)">
            <InputNumber min={0} style={{ width: '100%' }} placeholder="선택" />
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
