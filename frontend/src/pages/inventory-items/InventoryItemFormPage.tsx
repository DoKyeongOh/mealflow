import { App, Button, DatePicker, Form, InputNumber, Select, Space, Spin, Typography } from 'antd'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import dayjs, { type Dayjs } from 'dayjs'
import { useEffect } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { listGroceryTypes } from '@/features/grocery-types/api/groceryTypeApi'
import {
  createInventoryItem,
  getInventoryItem,
  getSuggestedDefaults,
  patchInventoryItem,
} from '@/features/inventory-items/api/inventoryItemApi'
import type { CreateInventoryItemRequest, GroceryUnit, UpdateInventoryItemRequest } from '@/features/inventory-items/model/types'
import { GROCERY_UNIT_LABELS, GROCERY_UNITS } from '@/features/inventory-items/model/types'
import { listStorageLocations } from '@/features/storage-locations/api/storageLocationApi'
import { errorMessage } from '@/shared/lib/errorMessage'

const { Title } = Typography

type FormValues = {
  groceryTypeId: number
  storageLocationId: number
  quantity: number
  unit: GroceryUnit
  expirationDate: Dayjs
}

export function InventoryItemFormPage() {
  const { id: inventoryItemIdParam } = useParams<{ id?: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const [form] = Form.useForm<FormValues>()
  const isEdit = Boolean(inventoryItemIdParam)
  const inventoryItemId = inventoryItemIdParam ? Number(inventoryItemIdParam) : NaN

  const groceryTypeIdWatch = Form.useWatch('groceryTypeId', form)

  const { data: locations, isLoading: locLoading } = useQuery({
    queryKey: ['storage-locations'],
    queryFn: listStorageLocations,
  })

  const { data: groceries, isLoading: gLoading } = useQuery({
    queryKey: ['grocery-types'],
    queryFn: listGroceryTypes,
  })

  const { data, isLoading } = useQuery({
    queryKey: ['inventory-items', inventoryItemId],
    queryFn: () => getInventoryItem(inventoryItemId),
    enabled: isEdit && Number.isFinite(inventoryItemId),
  })

  useEffect(() => {
    if (isEdit || !groceryTypeIdWatch || !Number.isFinite(groceryTypeIdWatch)) return
    let cancelled = false
    getSuggestedDefaults(groceryTypeIdWatch)
      .then((d) => {
        if (cancelled) return
        form.setFieldsValue({
          storageLocationId: d.suggestedStorageLocationId ?? undefined,
          expirationDate: d.suggestedExpirationDate ? dayjs(d.suggestedExpirationDate) : undefined,
        })
      })
      .catch(() => {
        /* ignore */
      })
    return () => {
      cancelled = true
    }
  }, [groceryTypeIdWatch, isEdit, form])

  const createMutation = useMutation({
    mutationFn: (values: FormValues) => {
      const body: CreateInventoryItemRequest = {
        groceryTypeId: values.groceryTypeId,
        storageLocationId: values.storageLocationId,
        quantity: values.quantity,
        unit: values.unit,
        expirationDate: values.expirationDate.format('YYYY-MM-DD'),
      }
      return createInventoryItem(body)
    },
    onSuccess: (res) => {
      message.success('저장했습니다.')
      queryClient.invalidateQueries({ queryKey: ['inventory-items'] })
      navigate(`/inventory-items/${res.id}`)
    },
    onError: (e) => message.error(errorMessage(e)),
  })

  const updateMutation = useMutation({
    mutationFn: (values: FormValues) => {
      const body: UpdateInventoryItemRequest = {
        quantity: values.quantity,
        unit: values.unit,
        expirationDate: values.expirationDate.format('YYYY-MM-DD'),
        storageLocationId: values.storageLocationId,
        groceryTypeId: values.groceryTypeId,
      }
      return patchInventoryItem(inventoryItemId, body)
    },
    onSuccess: (res) => {
      message.success('저장했습니다.')
      queryClient.invalidateQueries({ queryKey: ['inventory-items'] })
      queryClient.invalidateQueries({ queryKey: ['inventory-items', res.id] })
      navigate(`/inventory-items/${res.id}`)
    },
    onError: (e) => message.error(errorMessage(e)),
  })

  if (isEdit && !Number.isFinite(inventoryItemId)) {
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
        groceryTypeId: data.groceryTypeId,
        storageLocationId: data.storageLocationId,
        quantity: data.quantity,
        unit: data.unit,
        expirationDate: dayjs(data.expirationDate),
      }
    : {
        unit: 'COUNT',
        quantity: 1,
      }

  const onFinish = (values: FormValues) => {
    if (isEdit) {
      updateMutation.mutate(values)
    } else {
      createMutation.mutate(values)
    }
  }

  const pending = createMutation.isPending || updateMutation.isPending

  const groceryOptions = groceries?.map((g) => ({ value: g.id, label: g.name })) ?? []
  const locationOptions = locations?.map((l) => ({ value: l.id, label: l.name })) ?? []
  const unitOptions = GROCERY_UNITS.map((u) => ({
    value: u,
    label: GROCERY_UNIT_LABELS[u],
  }))

  return (
    <div style={{ maxWidth: 1040, margin: '0 auto', width: '100%', padding: '0 16px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%', maxWidth: 520 }}>
        <Title level={3}>{isEdit ? '보관 항목 수정' : '보관 항목 추가'}</Title>
        <Link to={isEdit ? `/inventory-items/${inventoryItemId}` : '/inventory-items'}>← 돌아가기</Link>
        <Form<FormValues>
          form={form}
          layout="vertical"
          initialValues={initialValues}
          onFinish={onFinish}
          key={data?.id ?? 'new'}
        >
          <Form.Item
            name="groceryTypeId"
            label="식료품 종류"
            rules={[{ required: true, message: '식료품 종류를 선택하세요.' }]}
          >
            <Select showSearch optionFilterProp="label" options={groceryOptions} loading={gLoading} />
          </Form.Item>
          <Form.Item
            name="storageLocationId"
            label="저장 장소"
            rules={[{ required: true, message: '저장 장소를 선택하세요.' }]}
          >
            <Select showSearch optionFilterProp="label" options={locationOptions} loading={locLoading} />
          </Form.Item>
          <Form.Item
            name="quantity"
            label="수량"
            rules={[{ required: true, message: '수량을 입력하세요.' }]}
          >
            <InputNumber min={0.0001} step={0.1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="unit" label="단위" rules={[{ required: true }]}>
            <Select options={unitOptions} />
          </Form.Item>
          <Form.Item
            name="expirationDate"
            label="유통기한"
            rules={[{ required: true, message: '유통기한을 선택하세요.' }]}
          >
            <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
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
