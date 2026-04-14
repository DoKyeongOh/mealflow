import type {
  ReferenceStatusResponse,
  StorageLocationCreateRequest,
  StorageLocationResponse,
  StorageLocationUpdateRequest,
} from '@/features/storage-locations/model/types'
import { apiDelete, apiGet, apiPost, apiPut } from '@/shared/lib/apiClient'

const BASE = '/api/v1/storage-locations'

export function listStorageLocations(): Promise<StorageLocationResponse[]> {
  return apiGet<StorageLocationResponse[]>(BASE)
}

export function getStorageLocation(id: number): Promise<StorageLocationResponse> {
  return apiGet<StorageLocationResponse>(`${BASE}/${id}`)
}

export function getReferenced(id: number): Promise<ReferenceStatusResponse> {
  return apiGet<ReferenceStatusResponse>(`${BASE}/${id}/referenced`)
}

export function createStorageLocation(
  body: StorageLocationCreateRequest,
): Promise<StorageLocationResponse> {
  return apiPost<StorageLocationCreateRequest, StorageLocationResponse>(BASE, body)
}

export function updateStorageLocation(
  id: number,
  body: StorageLocationUpdateRequest,
): Promise<StorageLocationResponse> {
  return apiPut<StorageLocationUpdateRequest, StorageLocationResponse>(`${BASE}/${id}`, body)
}

export function deleteStorageLocation(id: number): Promise<void> {
  return apiDelete(`${BASE}/${id}`)
}
