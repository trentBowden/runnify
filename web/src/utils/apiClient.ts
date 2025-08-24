import { Configuration } from "../runnify-api-v1/runtime";
import type { Middleware } from "../runnify-api-v1/runtime";

/**
 * Create middleware to add JWT token to all requests
 */
function createAuthMiddleware(getToken: () => string | null): Middleware {
  return {
    pre: async (context) => {
      const token = getToken();

      if (token) {
        context.init.headers = {
          ...context.init.headers,
          Authorization: `Bearer ${token}`,
        };
      }

      return context;
    },
  };
}

/**
 * Create a configured API client that automatically includes JWT token
 */
export function createConfiguredApiClient(getToken: () => string | null) {
  return new Configuration({
    basePath: "http://localhost:8080",
    headers: {
      "Content-Type": "application/json",
    },
    middleware: [createAuthMiddleware(getToken)],
  });
}

/**
 * Create a basic API client without authentication (for public endpoints)
 */
export function createPublicApiClient() {
  return new Configuration({
    basePath: "http://localhost:8080",
    headers: {
      "Content-Type": "application/json",
    },
  });
}

/**
 * Get the current auth token from localStorage
 * This avoids circular dependency with the store
 */
export function getAuthToken(): string | null {
  return localStorage.getItem("authToken");
}

/**
 * Create a token getter that uses Redux store (for use outside of slices)
 * Call this function after the store is initialized
 */
export function createStoreTokenGetter(getStore: () => any) {
  return () => {
    try {
      const state = getStore();
      return state?.auth?.token || localStorage.getItem("authToken");
    } catch {
      // Fallback to localStorage if store is not available
      return localStorage.getItem("authToken");
    }
  };
}

/**
 * Create headers with authorization if token exists
 */
export function createAuthHeaders(): Record<string, string> {
  const token = getAuthToken();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  return headers;
}
