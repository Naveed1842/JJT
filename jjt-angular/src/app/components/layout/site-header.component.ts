import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService, User } from '../../services/auth.service';

@Component({
  selector: 'app-site-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <header class="bg-white/90 backdrop-blur border-b border-slate-200 sticky top-0 z-20">
      <div class="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <div class="flex items-center gap-2">
          <div class="h-8 w-8 rounded-full bg-orange-600"></div>
          <div>
            <p class="text-sm font-semibold text-slate-900 leading-none">JJT Platform</p>
            <p class="text-[11px] text-slate-500 leading-none">Education Continuity</p>
          </div>
        </div>
        <nav class="hidden md:flex items-center gap-6 text-sm font-semibold text-slate-700">
          <a routerLink="/" class="hover:text-orange-700">Home</a>
          <a routerLink="/children" class="hover:text-orange-700">Children</a>
          <a *ngIf="authService.isAdmin()" routerLink="/admin" class="hover:text-orange-700">Admin</a>
          
          <div *ngIf="currentUser" class="flex items-center gap-3 ml-4 pl-4 border-l border-slate-300">
            <span class="text-xs bg-orange-100 text-orange-800 px-2 py-1 rounded">
              {{ currentUser.username }} ({{ currentUser.role }})
            </span>
            <button (click)="logout()" class="text-xs bg-slate-100 hover:bg-slate-200 px-3 py-1 rounded">
              Logout
            </button>
          </div>
          
          <a *ngIf="!currentUser" routerLink="/login" class="text-xs bg-orange-600 text-white hover:bg-orange-700 px-4 py-2 rounded">
            Login
          </a>
        </nav>
      </div>
    </header>
  `
})
export class SiteHeaderComponent implements OnInit {
  currentUser: User | null = null;

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
