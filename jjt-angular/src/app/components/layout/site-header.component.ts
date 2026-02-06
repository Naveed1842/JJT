import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

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
          <a routerLink="/admin" class="hover:text-orange-700">Admin</a>
          <a routerLink="/sponsor/confirmation" class="hover:text-orange-700">Contact</a>
        </nav>
      </div>
    </header>
  `
})
export class SiteHeaderComponent {}
