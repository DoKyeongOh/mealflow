export interface GroceryTypeResponse {
  id: number
  name: string
  defaultStorageLocationId: number | null
  defaultShelfLifeDays: number | null
  createdAt: string
  updatedAt: string
}

export interface GroceryTypeCreateRequest {
  name: string
  defaultStorageLocationId: number
  defaultShelfLifeDays: number | null
}

export interface GroceryTypeUpdateRequest {
  name: string
  defaultStorageLocationId: number
  defaultShelfLifeDays: number | null
}

export interface ReferenceStatusResponse {
  referenced: boolean
}
