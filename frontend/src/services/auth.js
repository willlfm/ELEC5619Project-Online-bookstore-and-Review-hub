const API_BASE =
  (typeof process !== "undefined" &&
    process.env &&
    process.env.REACT_APP_API_BASE) ||
  "";

export class HttpError extends Error {
  constructor(message, status, data) {
    super(message);
    this.name = "HttpError";
    this.status = status;
    this.data = data;
  }
}

export async function apiFetch(path, options = {}) {
  const {
    method = "GET",
    headers,
    body,
    signal,
    credentials = "include",
  } = options;

  const resp = await fetch(`${API_BASE}${path}`, {
    method,
    credentials,
    signal,
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      ...(headers || {}),
    },
    body: body != null ? JSON.stringify(body) : undefined,
  });

  const text = await resp.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text || null;
  }

  if (!resp.ok) {
    throw new HttpError(
      `HTTP ${resp.status}${data && data.message ? `: ${data.message}` : ""}`,
      resp.status,
      data
    );
  }
  return data;
}

export async function signIn({ username, password }, options = {}) {
  return apiFetch("/api/auth/signin", {
    method: "POST",
    body: { username, password },
    ...options,
  });
}

export async function signOut(options = {}) {
  return apiFetch("/api/auth/signout", {
    method: "POST",
    ...options,
  });
}

export async function getCurrentUser(options = {}) {
  try {
    const me = await apiFetch("/api/auth/me", options);
    return me;
  } catch (err) {
    if (err instanceof HttpError && err.status === 401) return null;
    throw err;
  }
}

export function getQueryParam(name, search = window.location.search) {
  const params = new URLSearchParams(search);
  return params.get(name);
}

export function getNextPath(search = window.location.search) {
  return getQueryParam("next", search) || "/";
}

export function isForceStayOnSignin(search = window.location.search) {
  return getQueryParam("force", search) === "1";
}