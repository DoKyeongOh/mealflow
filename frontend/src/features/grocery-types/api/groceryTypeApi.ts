import type {
  GroceryTypeCreateRequest,
  GroceryTypeResponse,
  GroceryTypeUpdateRequest,
  ReferenceStatusResponse,
} from '@/features/grocery-types/model/types'
import { apiDelete, apiGet, apiPost, apiPut } from '@/shared/lib/apiClient'

const BASE = '/api/v1/grocery-types'

export function listGroceryTypes(): Promise<GroceryTypeResponse[]> {
  return apiGet<GroceryTypeResponse[]>(BASE)
}

export function getGroceryType(id: number): Promise<GroceryTypeResponse> {
  return apiGet<GroceryTypeResponse>(`${BASE}/${id}`)
}

export function getReferenced(id: number): Promise<ReferenceStatusResponse> {
  return apiGet<ReferenceStatusResponse>(`${BASE}/${id}/referenced`)
}

export function createGroceryType(
  body: GroceryTypeCreateRequest,
): Promise<GroceryTypeResponse> {
  return apiPost<GroceryTypeCreateRequest, GroceryTypeResponse>(BASE, body)
}

export function updateGroceryType(
  id: number,
  body: GroceryTypeUpdateRequest,
): Promise<GroceryTypeResponse> {
  return apiPut<GroceryTypeUpdateRequest, GroceryTypeResponse>(`${BASE}/${id}`, body)
}

export function deleteGroceryType(id: number): Promise<void> {
  return apiDelete(`${BASE}/${id}`)
}
