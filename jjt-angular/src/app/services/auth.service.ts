import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
  sponsorId?: string;
}

export interface User {
  username: string;
  role: string;
  sponsorId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'jjt_token';
  private readonly USER_KEY = 'jjt_user';
  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          this.setSession(response);
        })
      );
  }

  register(username: string, password: string, email: string, role: string): Observable<string> {
    return this.http.post(`${environment.apiBaseUrl}/auth/register`,
      { username, password, email, role },
      { responseType: 'text' }
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem('userRole'); // Legacy support
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    // Check if token is expired
    return !this.isTokenExpired(token);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  hasRole(roles: string[]): boolean {
    const user = this.getCurrentUser();
    return user ? roles.includes(user.role) : false;
  }

  isAdmin(): boolean {
    return this.hasRole(['ADMIN', 'JJT_ADMIN', 'ORG_ADMIN']);
  }

  isSponsor(): boolean {
    return this.hasRole(['SPONSOR']);
  }

  private setSession(authResult: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, authResult.token);
    const user: User = {
      username: authResult.username,
      role: authResult.role,
      sponsorId: authResult.sponsorId
    };
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  private getUserFromStorage(): User | null {
    const userStr = localStorage.getItem(this.USER_KEY);
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = this.decodeJwtPayload(token);
      const expiry = payload.exp;
      return Math.floor(new Date().getTime() / 1000) >= expiry;
    } catch (e) {
      return true;
    }
  }

  private decodeJwtPayload(token: string): any {
    const base64Url = token.split('.')[1];
    if (!base64Url) {
      throw new Error('Invalid JWT: missing payload segment');
    }

    // Convert from Base64URL to standard Base64
    let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');

    // Pad with '=' to make length a multiple of 4
    const paddingNeeded = (4 - (base64.length % 4)) % 4;
    if (paddingNeeded > 0) {
      base64 = base64 + '='.repeat(paddingNeeded);
    }

    const json = atob(base64);
    return JSON.parse(json);
  }
}
