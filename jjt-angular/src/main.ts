import { APP_INITIALIZER, ErrorHandler } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import {
  PreloadAllModules,
  provideRouter,
  withInMemoryScrolling,
  withPreloading,
} from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/interceptors/auth.interceptor';
import { httpErrorInterceptor } from './app/interceptors/http-error.interceptor';
import { GlobalErrorHandler } from './app/core/global-error-handler';
import { AuthService } from './app/services/auth.service';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(
      routes,
      // Back/forward restores scroll position; anchors (#how-it-works) scroll into view.
      withInMemoryScrolling({ scrollPositionRestoration: 'enabled', anchorScrolling: 'enabled' }),
      // Lazy chunks download in the background after first paint — later
      // navigations don't wait on the network.
      withPreloading(PreloadAllModules)
    ),
    // Order matters: httpErrorInterceptor is outermost so its timeout/retry wrap
    // the auth interceptor's 401-refresh flow, and every error that escapes the
    // chain is normalized to a guaranteed { error: { code, message } } shape.
    provideHttpClient(withInterceptors([httpErrorInterceptor, authInterceptor])),
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
    {
      provide: APP_INITIALIZER,
      // Kick off session restore WITHOUT returning the promise: bootstrap must not
      // block on a network round-trip (slow first paint on public pages, worse on
      // cold backend starts). Route guards await auth.ready before deciding.
      useFactory: (auth: AuthService) => () => { auth.restoreSession(); },
      deps: [AuthService],
      multi: true
    }
  ]
}).catch(err => console.error(err));
