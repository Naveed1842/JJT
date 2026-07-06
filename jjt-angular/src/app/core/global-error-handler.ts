import { ErrorHandler, Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

/**
 * Last line of defence for errors nothing else caught (template errors,
 * unhandled promise rejections, subscriber callbacks that throw).
 *
 * HTTP errors are normalized by the interceptor and handled where the call is
 * made — if one reaches here it means a component forgot its error callback,
 * which is exactly what we want surfaced loudly during development.
 */
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  handleError(error: unknown): void {
    if (error instanceof HttpErrorResponse) {
      console.error(
        `[JJT] Unhandled HTTP error (${error.status}) from ${error.url} — ` +
        `a component is missing its error handler:`,
        error.error?.message ?? error.message
      );
      return;
    }
    console.error('[JJT] Unhandled error:', error);
  }
}
