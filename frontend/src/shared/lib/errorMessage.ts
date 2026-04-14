import { ApiError } from '@/shared/lib/apiClient'

export function errorMessage(err: unknown): string {
  if (err instanceof ApiError) return err.message
  if (err instanceof Error) return err.message
  return '요청에 실패했습니다.'
}
