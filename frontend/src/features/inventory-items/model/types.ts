export type GroceryUnit = 'COUNT' | 'G' | 'KG' | 'ML' | 'L' | 'PACK' | 'BOTTLE'

export const GROCERY_UNITS: GroceryUnit[] = [
  'COUNT',
  'G',
  'KG',
  'ML',
  'L',
  'PACK',
  'BOTTLE',
]

export const GROCERY_UNIT_LABELS: Record<GroceryUnit, string> = {
  COUNT: '개',
  G: 'g',
  KG: 'kg',
  ML: 'ml',
  L: 'L',
  PACK: '팩',
  BOTTLE: '병',
}

export interface InventoryItemResponse {
  id: number
  groceryTypeId: number
  storageLocationId: number
  quantity: number
  unit: GroceryUnit
  expirationDate: string
  createdAt: string
  updatedAt: string
}

export interface CreateInventoryItemRequest {
  groceryTypeId: number
  storageLocationId: number
  quantity: number
  unit: GroceryUnit
  expirationDate: string
}

export interface UpdateInventoryItemRequest {
  quantity?: number
  unit?: GroceryUnit
  expirationDate?: string
  storageLocationId?: number
  groceryTypeId?: number
}

export interface InventoryEventResponse {
  id: number
  inventoryItemId: number
  groceryTypeId: number
  countBefore: number | null
  countDiff: number
  countAfter: number | null
  unit: GroceryUnit
  occurredAt: string
}

export interface SuggestedDefaultsResponse {
  suggestedExpirationDate: string | null
  suggestedStorageLocationId: number | null
}
