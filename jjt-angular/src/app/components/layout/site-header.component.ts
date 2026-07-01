import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService, CurrentUser } from '../../services/auth.service';

@Component({
  selector: 'app-site-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <header style="background:#fffdf9;border-bottom:1px solid #efe9dd;position:sticky;top:0;z-index:30;">
      <div style="max-width:1200px;margin:0 auto;display:flex;align-items:center;gap:16px;padding:16px 32px;">

        <!-- Logo -->
        <a routerLink="/" style="display:flex;align-items:center;gap:10px;text-decoration:none;flex-shrink:0;">
          <span style="font-family:'Newsreader',serif;font-weight:600;font-size:22px;color:#1c352c;">JJT</span>
        </a>

        <!-- Desktop nav -->
        <nav style="display:flex;gap:24px;margin-left:20px;" class="desktop-nav">
          <a routerLink="/children" routerLinkActive="nav-active"
             style="font-size:14px;color:#54625b;text-decoration:none;transition:color .15s;"
             class="nav-link">Children</a>
          <a routerLink="/why-give" routerLinkActive="nav-active"
             style="font-size:14px;color:#54625b;text-decoration:none;"
             class="nav-link">Why give</a>
          <a routerLink="/trust" routerLinkActive="nav-active"
             style="font-size:14px;color:#54625b;text-decoration:none;"
             class="nav-link">Trust</a>
          <a *ngIf="user && (user.role === 'JJT_ADMIN' || user.role === 'ORG_ADMIN')"
             routerLink="/admin" routerLinkActive="nav-active"
             style="font-size:14px;color:#54625b;text-decoration:none;"
             class="nav-link">Admin</a>
          <a *ngIf="user && user.role === 'SPONSOR'"
             routerLink="/sponsor/portal" routerLinkActive="nav-active"
             style="font-size:14px;color:#54625b;text-decoration:none;"
             class="nav-link">My Children</a>
        </nav>

        <!-- Auth area -->
        <div style="margin-left:auto;display:flex;align-items:center;gap:12px;">
          <!-- Logged out -->
          <a *ngIf="!user" routerLink="/why-give"
             style="font-size:14px;color:#2f5d4f;font-weight:600;text-decoration:none;" class="hide-sm">Give any amount</a>
          <a *ngIf="!user" routerLink="/children"
             style="background:#2f5d4f;color:#fff;font-size:14px;font-weight:600;border-radius:8px;
                    padding:10px 18px;text-decoration:none;transition:background .15s;"
             class="cta-btn">Sponsor a child</a>

          <!-- Logged in -->
          <ng-container *ngIf="user">
            <div style="display:flex;flex-direction:column;align-items:flex-end;line-height:1.3;" class="hide-sm">
              <span style="font-size:13px;font-weight:600;color:#1c352c;">{{ user.email }}</span>
              <span style="font-size:11px;color:#8a958d;font-family:'IBM Plex Mono',monospace;text-transform:uppercase;letter-spacing:.06em;">
                {{ roleLabel(user.role) }}
              </span>
            </div>
            <button (click)="logout()"
                    style="font-size:13px;font-weight:600;color:#8a5f1f;border:1px solid #ecd6b0;
                           background:transparent;border-radius:8px;padding:8px 14px;cursor:pointer;
                           transition:border-color .15s;">
              Sign out
            </button>
          </ng-container>

          <!-- Mobile hamburger -->
          <button class="hamburger" (click)="mobileOpen = !mobileOpen" aria-label="Menu"
                  style="display:none;background:none;border:none;cursor:pointer;padding:4px;">
            <svg width="22" height="22" fill="none" stroke="#1c352c" stroke-width="2" viewBox="0 0 24 24">
              <path *ngIf="!mobileOpen" stroke-linecap="round" d="M4 6h16M4 12h16M4 18h16"/>
              <path *ngIf="mobileOpen"  stroke-linecap="round" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Mobile drawer -->
      <div *ngIf="mobileOpen"
           style="border-top:1px solid #efe9dd;background:#fffdf9;padding:16px 32px;
                  display:flex;flex-direction:column;gap:14px;">
        <a routerLink="/" (click)="mobileOpen=false"
           style="font-size:14px;font-weight:500;color:#54625b;text-decoration:none;">Home</a>
        <a routerLink="/children" (click)="mobileOpen=false"
           style="font-size:14px;font-weight:500;color:#54625b;text-decoration:none;">Children</a>
        <a routerLink="/why-give" (click)="mobileOpen=false"
           style="font-size:14px;font-weight:500;color:#54625b;text-decoration:none;">Why give</a>
        <a routerLink="/trust" (click)="mobileOpen=false"
           style="font-size:14px;font-weight:500;color:#54625b;text-decoration:none;">Trust</a>
        <a *ngIf="user && (user.role === 'JJT_ADMIN' || user.role === 'ORG_ADMIN')"
           routerLink="/admin" (click)="mobileOpen=false"
           style="font-size:14px;font-weight:500;color:#54625b;text-decoration:none;">Admin</a>
        <a *ngIf="user && user.role === 'SPONSOR'"
           routerLink="/sponsor/portal" (click)="mobileOpen=false"
           style="font-size:14px;font-weight:500;color:#54625b;text-decoration:none;">My Children</a>
        <a *ngIf="!user" routerLink="/login" (click)="mobileOpen=false"
           style="font-size:14px;font-weight:500;color:#54625b;text-decoration:none;">Sign in</a>
        <button *ngIf="user" (click)="logout()"
                style="text-align:left;font-size:14px;font-weight:600;color:#8a5f1f;background:none;border:none;cursor:pointer;padding:0;">
          Sign out
        </button>
      </div>
    </header>

    <style>
      .nav-link:hover { color: #2f5d4f !important; }
      .nav-active { color: #2f5d4f !important; font-weight: 600; }
      .cta-btn:hover { background: #214a3e !important; }
      @media (max-width: 768px) {
        .desktop-nav { display: none !important; }
        .hamburger   { display: block !important; }
        .hide-sm     { display: none !important; }
      }
    </style>
  `
})
export class SiteHeaderComponent implements OnInit {
  private readonly auth   = inject(AuthService);
  private readonly router = inject(Router);

  user: CurrentUser | null = null;
  mobileOpen = false;

  ngOnInit(): void {
    this.auth.currentUser$.subscribe(u => { this.user = u; });
  }

  roleLabel(role: string): string {
    switch (role) {
      case 'JJT_ADMIN': return 'Super Admin';
      case 'ORG_ADMIN':  return 'Org Admin';
      case 'SPONSOR':    return 'Sponsor';
      default:           return role;
    }
  }

  async logout(): Promise<void> {
    this.mobileOpen = false;
    await this.auth.logout();
  }
}
