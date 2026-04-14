import { getApiBaseUrl } from '@/shared/lib/env'

export class ApiError extends Error {
  readonly status: number
  readonly body?: unknown

  constructor(status: number, message: string, body?: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export type ApiFetchOptions = RequestInit

export async function apiFetch(path: string, init?: ApiFetchOptions): Promise<Response> {
  const rest = init ?? {}
  const base = getApiBaseUrl()
  const url = path.startsWith('http') ? path : `${base.replace(/\/$/, '')}${path}`

  const headers = new Headers(rest.headers)
  if (
    rest.body != null &&
    typeof rest.body === 'string' &&
    !headers.has('Content-Type')
  ) {
    headers.set('Content-Type', 'application/json')
  }

  const res = await fetch(url, {
    ...rest,
    headers,
    credentials: 'include',
  })

  if (res.status === 401) {
    const from = encodeURIComponent(`${window.location.pathname}${window.location.search}`)
    window.location.assign(`/login?from=${from}`)
    throw new ApiError(401, 'Unauthorized')
  }

  return res
}

export async function apiGet<T>(path: string, init?: ApiFetchOptions): Promise<T> {
  const res = await apiFetch(path, { ...init, method: 'GET' })
  if (!res.ok) {
    await throwIfNotOk(res)
  }
  return parseResponseToJson<T>(res)
}

export async function apiPost<TReq, TRes>(
  path: string,
  body: TReq,
  init?: ApiFetchOptions,
): Promise<TRes> {
  const res = await apiFetch(path, {
    ...init,
    method: 'POST',
    body: JSON.stringify(body),
  })
  if (!res.ok) await throwIfNotOk(res)
  return parseResponseToJson<TRes>(res)
}

export async function apiPut<TReq, TRes>(
  path: string,
  body: TReq,
  init?: ApiFetchOptions,
): Promise<TRes> {
  const res = await apiFetch(path, {
    ...init,
    method: 'PUT',
    body: JSON.stringify(body),
  })
  if (!res.ok) await throwIfNotOk(res)
  return parseResponseToJson<TRes>(res)
}

export async function apiPatch<TReq, TRes>(
  path: string,
  body: TReq,
  init?: ApiFetchOptions,
): Promise<TRes> {
  const res = await apiFetch(path, {
    ...init,
    method: 'PATCH',
    body: JSON.stringify(body),
  })
  if (!res.ok) await throwIfNotOk(res)
  return parseResponseToJson<TRes>(res)
}

export async function apiDelete(path: string, init?: ApiFetchOptions): Promise<void> {
  const res = await apiFetch(path, { ...init, method: 'DELETE' })
  if (!res.ok) await throwIfNotOk(res)
}

export async function parseResponseToJson<T>(res: Response): Promise<T> {
  if (res.status === 204 || res.status === 205) {
    return undefined as T
  }
  const text = await res.text()
  if (!text) {
    return undefined as T
  }
  return JSON.parse(text) as T
}

export async function throwIfNotOk(res: Response): Promise<void> {
  if (res.ok) return
  const text = await res.text()
  let body: unknown
  if (!text) {
    body = undefined
  } else {
    try {
      body = JSON.parse(text) as unknown
    } catch {
      body = text
    }
  }
  const message =
    typeof body === 'object' && body !== null && 'message' in body
      ? String((body as { message: unknown }).message)
      : res.statusText
  throw new ApiError(res.status, message || res.statusText, body)
}
