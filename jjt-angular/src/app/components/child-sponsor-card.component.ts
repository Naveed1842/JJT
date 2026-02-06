import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

export type ChildCardStatus = 'AVAILABLE' | 'ALLOCATED';

export interface ChildCardModel {
  id: string;
  name: string;
  age: number;
  grade: string;
  monthlyCost: number;
  currency: string;
  status: ChildCardStatus;
}

@Component({
  selector: 'app-child-sponsor-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div
      class="relative flex flex-col justify-between rounded-2xl border border-slate-200 bg-white/90 p-5 shadow-sm transition hover:shadow-md min-h-[320px]"
    >
      <span
        class="absolute right-4 top-4 rounded-full px-3 py-1 text-xs font-semibold"
        [ngClass]="child.status === 'AVAILABLE'
          ? 'bg-green-100 text-green-800'
          : 'bg-slate-200 text-slate-600'"
      >
        {{ child.status === 'AVAILABLE' ? 'Available' : 'Allocated' }}
      </span>

      <div class="flex flex-col items-center gap-3 pt-6">
        <div
          class="flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br from-slate-100 via-slate-50 to-slate-200 text-slate-500 shadow-inner"
          aria-hidden="true"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" class="h-10 w-10" fill="none" stroke="currentColor" stroke-width="1.6">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 12c1.657 0 3-1.57 3-3.5S13.657 5 12 5 9 6.57 9 8.5 10.343 12 12 12Z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M6.5 19c.5-2.5 2.75-4 5.5-4s5 .75 5.5 4" />
          </svg>
        </div>
        <div class="text-center">
          <p class="text-lg font-semibold text-slate-900">{{ child.name }}</p>
          <p class="text-sm text-slate-600">
            Age {{ child.age && child.age > 0 ? child.age : '—' }} · Grade {{ child.grade || '—' }}
          </p>
        </div>
      </div>

      <div class="mt-4 space-y-1 text-center">
        <p class="text-2xl font-bold text-slate-900">{{ child.monthlyCost | number:'1.0-0' }} {{ child.currency }} / month</p>
        <p class="text-sm text-slate-600">Keeps education continuous</p>
      </div>

      <div class="mt-5 grid grid-cols-1 gap-3">
        <a
          [routerLink]="['/children', child.id]"
          class="inline-flex items-center justify-center rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-800 transition hover:border-slate-400 hover:bg-slate-50"
        >
          View details
        </a>
        <a
          [routerLink]="['/children', child.id, 'sponsor']"
          class="inline-flex items-center justify-center rounded-lg px-4 py-2 text-sm font-semibold text-white transition"
          [ngClass]="child.status === 'AVAILABLE'
            ? 'bg-orange-600 hover:bg-orange-700'
            : 'bg-slate-300 cursor-not-allowed'"
          [attr.aria-disabled]="child.status === 'ALLOCATED'"
        >
          {{ child.status === 'AVAILABLE' ? 'Sponsor this child' : 'Already supported' }}
        </a>
      </div>
    </div>
  `
})
export class ChildSponsorCardComponent {
  @Input({ required: true }) child!: ChildCardModel;
}
