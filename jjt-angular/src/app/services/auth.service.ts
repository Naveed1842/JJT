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

  async refreshAccessToken(): Promise<string> {
    const rt = localStorage.getItem(RT_KEY);
    if (!rt) throw new Error('No refresh token available');
    const resp = await firstValueFrom(
      this.http.post<LoginResponse>(`${this.base}/api/auth/refresh`, { refreshToken: rt })
    );
    this.applyTokens(resp);
    return this.accessToken!;
  }

  /** Called by APP_INITIALIZER to restore session on page load/refresh. */
  async restoreSession(): Promise<void> {
    const rt = localStorage.getItem(RT_KEY);
    if (!rt) return;
    try {
      await this.refreshAccessToken();
      await this.fetchCurrentUser();
    } catch {
      this.clearSession();
    }
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
