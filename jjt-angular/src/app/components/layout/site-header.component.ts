import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-site-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <header class="sticky top-0 z-30 border-b border-[#2B3A33] bg-[#0B1512]/95 backdrop-blur">
      <div class="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
        <div class="flex items-center gap-3">
          <img
            src="assets/images/jj-FINAL-LOGO.png"
            alt="Junior Jinnah Trust"
            class="h-16 w-auto max-w-[260px] object-contain"
          />
        </div>
        <nav class="hidden md:flex items-center gap-6 text-sm font-semibold text-amber-100">
          <a routerLink="/" class="hover:text-amber-300">Home</a>
          <a routerLink="/children" class="hover:text-amber-300">Children</a>
          <!-- <a routerLink="/admin" class="hover:text-orange-700">Admin</a> -->
          <!-- <a routerLink="/sponsor/confirmation" class="hover:text-orange-700">Contact</a> -->
        </nav>
      </div>
    </header>
  `
})
export class SiteHeaderComponent {}
