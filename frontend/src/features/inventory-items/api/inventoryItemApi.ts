import type {
  CreateInventoryItemRequest,
  InventoryEventResponse,
  InventoryItemResponse,
  SuggestedDefaultsResponse,
  UpdateInventoryItemRequest,
} from '@/features/inventory-items/model/types'
import {
  apiFetch,
  apiGet,
  apiPatch,
  apiPost,
  parseResponseToJson,
  throwIfNotOk,
} from '@/shared/lib/apiClient'

const BASE = '/api/v1/inventory-items'

export function listInventoryItems(): Promise<InventoryItemResponse[]> {
  return apiGet<InventoryItemResponse[]>(BASE)
}

export function getInventoryItem(id: number): Promise<InventoryItemResponse> {
  return apiGet<InventoryItemResponse>(`${BASE}/${id}`)
}

export function listExpiring(withinDays = 7): Promise<InventoryItemResponse[]> {
  return apiGet<InventoryItemResponse[]>(`${BASE}/expiring?withinDays=${withinDays}`)
}

export function listRecent(limit = 10): Promise<InventoryItemResponse[]> {
  return apiGet<InventoryItemResponse[]>(`${BASE}/recent?limit=${limit}`)
}

export function getSuggestedDefaults(groceryTypeId: number): Promise<SuggestedDefaultsResponse> {
  return apiGet<SuggestedDefaultsResponse>(
    `${BASE}/suggested-defaults?groceryTypeId=${groceryTypeId}`,
  )
}

export function createInventoryItem(
  body: CreateInventoryItemRequest,
): Promise<InventoryItemResponse> {
  return apiPost<CreateInventoryItemRequest, InventoryItemResponse>(BASE, body)
}

export function patchInventoryItem(
  id: number,
  body: UpdateInventoryItemRequest,
): Promise<InventoryItemResponse> {
  return apiPatch<UpdateInventoryItemRequest, InventoryItemResponse>(`${BASE}/${id}`, body)
}

export async function deleteInventoryItem(id: number): Promise<InventoryItemResponse | undefined> {
  const res = await apiFetch(`${BASE}/${id}`, { method: 'DELETE' })
  if (!res.ok) await throwIfNotOk(res)
  if (res.status === 204) return undefined
  return parseResponseToJson<InventoryItemResponse>(res)
}

export function listItemEvents(id: number, limit = 50): Promise<InventoryEventResponse[]> {
  return apiGet<InventoryEventResponse[]>(`${BASE}/${id}/events?limit=${limit}`)
}
