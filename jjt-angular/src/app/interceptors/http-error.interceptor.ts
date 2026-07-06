import {
  HttpErrorResponse,
  HttpInterceptorFn,
} from '@angular/common/http';
import { throwError, timer, TimeoutError } from 'rxjs';
import { catchError, retry, timeout } from 'rxjs/operators';

/** Generous ceiling: the Heroku backend can take a while on a cold start. */
const REQUEST_TIMEOUT_MS = 30_000;
/** Transient statuses worth one retry — but only for idempotent GETs. */
const TRANSIENT_STATUSES = [0, 502, 503, 504];
const RETRY_DELAY_MS = 1_000;

/**
 * Cross-cutting HTTP hygiene, applied outermost (before the auth interceptor):
 *
 * 1. Timeout   — no request hangs forever; timeouts surface as a normal error.
 * 2. Retry     — GETs get one retry on transient failures (network down, bad
 *                gateway, cold start). Writes are never retried.
 * 3. Normalize — every error that reaches a component is an HttpErrorResponse
 *                whose `error.message` is guaranteed to exist and be humane,
 *                so `err?.error?.message ?? fallback` always works.
 */
export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const isGet = req.method === 'GET';

  return next(req).pipe(
    timeout(REQUEST_TIMEOUT_MS),
    retry({
      count: isGet ? 1 : 0,
      delay: (err) => {
        const transient =
          err instanceof TimeoutError ||
          (err instanceof HttpErrorResponse && TRANSIENT_STATUSES.includes(err.status));
        return transient ? timer(RETRY_DELAY_MS) : throwError(() => err);
      },
    }),
    catchError((err) => throwError(() => normalizeError(err, req.url)))
  );
};

function normalizeError(err: unknown, url: string): HttpErrorResponse {
  if (err instanceof TimeoutError) {
    return new HttpErrorResponse({
      error: { code: 'TIMEOUT', message: 'The server took too long to respond. Please try again.' },
      status: 0,
      statusText: 'Timeout',
      url,
    });
  }

  if (err instanceof HttpErrorResponse) {
    // Backend errors already carry {code, message}; pass them through untouched.
    if (err.error && typeof err.error === 'object' && typeof err.error.message === 'string') {
      return err;
    }
    // Network failure, HTML error pages, empty bodies: synthesize a message.
    return new HttpErrorResponse({
      error: { code: 'HTTP_' + err.status, message: messageForStatus(err.status) },
      status: err.status,
      statusText: err.statusText,
      url: err.url ?? url,
    });
  }

  return new HttpErrorResponse({
    error: { code: 'UNKNOWN', message: 'An unexpected error occurred. Please try again.' },
    status: 0,
    statusText: 'Unknown',
    url,
  });
}

function messageForStatus(status: number): string {
  switch (status) {
    case 0:   return 'Cannot reach the server. Check your connection and try again.';
    case 401: return 'Your session has expired. Please sign in again.';
    case 403: return 'You do not have permission to perform this action.';
    case 404: return 'The requested resource was not found.';
    case 502:
    case 503:
    case 504: return 'The server is temporarily unavailable. Please try again shortly.';
    default:  return status >= 500
      ? 'Something went wrong on the server. Please try again.'
      : 'The request could not be completed.';
  }
}
