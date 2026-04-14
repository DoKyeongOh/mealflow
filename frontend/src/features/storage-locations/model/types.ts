export interface StorageLocationResponse {
  id: number
  name: string
  createdAt: string
  updatedAt: string
}

export interface StorageLocationCreateRequest {
  name: string
}

export interface StorageLocationUpdateRequest {
  name: string
}

export interface ReferenceStatusResponse {
  referenced: boolean
}
