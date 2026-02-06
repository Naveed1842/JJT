import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-site-footer',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <footer class="bg-slate-900 text-slate-200 py-10">
      <div class="mx-auto max-w-6xl px-4">
        <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-lg font-semibold">JJT Platform</p>
            <p class="text-sm text-slate-400">Education must not pause.</p>
          </div>
          <div class="flex gap-6 text-sm font-medium">
            <a routerLink="/children" class="hover:text-white">About</a>
            <a routerLink="/admin" class="hover:text-white">How it works</a>
            <a routerLink="/children" class="hover:text-white">Contact</a>
          </div>
        </div>
        <p class="mt-6 text-xs text-slate-500">© {{ year }} JJT. All rights reserved.</p>
      </div>
    </footer>
  `
})
export class SiteFooterComponent {
  readonly year = new Date().getFullYear();
}
