const tokenKey = 'ticketing.accessToken';

export function accessToken() {
  return sessionStorage.getItem(tokenKey);
}

export function saveAccessToken(token) {
  sessionStorage.setItem(tokenKey, token);
}

export function clearAccessToken() {
  sessionStorage.removeItem(tokenKey);
}

export async function requestApi(path, { authenticated = false, ...options } = {}) {
  const url = new URL(`/api/v1/${path}`, location.origin);
  if (url.origin !== location.origin || !url.pathname.startsWith('/api/v1/')) {
    throw new Error('지원하지 않는 API 주소입니다.');
  }

  const headers = new Headers(options.headers);
  headers.set('Accept', 'application/json');
  const token = authenticated ? accessToken() : null;
  if (authenticated) {
    if (!token) throw Object.assign(new Error('로그인이 필요합니다.'), { status: 401 });
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(url, { cache: 'no-store', ...options, headers });
  if (response.ok) return response.status === 204 ? null : response.json();

  if (response.status === 401 && token && token === accessToken()) clearAccessToken();
  const error = await response.json().catch(() => null);
  const fieldMessage = Object.values(error?.fieldErrors ?? {})[0];
  throw Object.assign(new Error(fieldMessage || error?.message || ''), { status: response.status });
}
