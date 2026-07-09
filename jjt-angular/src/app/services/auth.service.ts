import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CurrentUser {
  id: string;
  email: string;
  role: 'JJT_ADMIN' | 'ORG_ADMIN' | 'SPONSOR';
  sponsorId: string | null;
  orgId: string | null;
}

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

const RT_KEY = 'rt';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly base = environment.apiBaseUrl;

  private accessToken: string | null = null;
  private readonly userSubject = new BehaviorSubject<CurrentUser | null>(null);
  readonly currentUser$ = this.userSubject.asObservable();

  /**
   * Single-flight refresh: when several requests 401 at once, they must all await
   * the SAME refresh call. The backend rotates refresh tokens (the old token is
   * revoked on first use), so concurrent refreshes with the same token would fail
   * and force a logout.
   */
  private refreshInFlight: Promise<string> | null = null;

  /**
   * Resolves once the initial session restore has settled (successfully or not).
   * Guards await this instead of the app blocking bootstrap on a network call.
   */
  private _ready: Promise<void> = Promise.resolve();
  get ready(): Promise<void> { return this._ready; }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  isAuthenticated(): boolean {
    return this.accessToken !== null;
  }

  getCurrentUser(): CurrentUser | null {
    return this.userSubject.value;
  }

  async login(email: string, password: string): Promise<void> {
    const resp = await firstValueFrom(
      this.http.post<LoginResponse>(`${this.base}/api/auth/login`, { email, password })
    );
    this.applyTokens(resp);
    await this.fetchCurrentUser();
  }

  async logout(): Promise<void> {
    const rt = localStorage.getItem(RT_KEY);
    const at = this.accessToken;
    this.clearSession();
    if (rt && at) {
      // Best-effort revocation — fire and forget with captured tokens
      this.http.post(`${this.base}/api/auth/logout`, { refreshToken: rt }, {
        headers: { Authorization: `Bearer ${at}` }
      }).subscribe({ error: () => {} });
    }
    await this.router.navigate(['/login']);
  }

  refreshAccessToken(): Promise<string> {
    if (this.refreshInFlight) return this.refreshInFlight;
    this.refreshInFlight = this.doRefresh().finally(() => {
      this.refreshInFlight = null;
    });
    return this.refreshInFlight;
  }

  private async doRefresh(): Promise<string> {
    const rt = localStorage.getItem(RT_KEY);
    if (!rt) throw new Error('No refresh token available');
    const resp = await firstValueFrom(
      this.http.post<LoginResponse>(`${this.base}/api/auth/refresh`, { refreshToken: rt })
    );
    this.applyTokens(resp);
    return this.accessToken!;
  }

  /**
   * Kicked off (not awaited) at bootstrap to restore the session from the stored
   * refresh token. Public pages render immediately; route guards await `ready`
   * so protected routes still see the restored session before deciding.
   */
  restoreSession(): Promise<void> {
    const rt = localStorage.getItem(RT_KEY);
    if (!rt) return this._ready;
    this._ready = (async () => {
      try {
        await this.refreshAccessToken();
        await this.fetchCurrentUser();
      } catch {
        this.clearSession();
      }
    })();
    return this._ready;
  }

  clearSession(): void {
    this.accessToken = null;
    localStorage.removeItem(RT_KEY);
    this.userSubject.next(null);
  }

  private applyTokens(resp: LoginResponse): void {
    this.accessToken = resp.accessToken;
    localStorage.setItem(RT_KEY, resp.refreshToken);
  }

  private async fetchCurrentUser(): Promise<void> {
    const user = await firstValueFrom(
      this.http.get<CurrentUser>(`${this.base}/api/auth/me`)
    );
    this.userSubject.next(user);
  }
}
