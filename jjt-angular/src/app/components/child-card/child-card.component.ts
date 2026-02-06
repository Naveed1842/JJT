import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

export interface ChildCardView {
  id: string;
  name: string;
  age: number;
  grade: string;
  monthlyCost: string;
  status: 'AVAILABLE' | 'EARLY_SUPPORTED' | 'SPONSORED';
}

@Component({
  selector: 'app-child-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-3 h-24 w-24 rounded-full bg-slate-100"></div>
      <h3 class="text-lg font-semibold text-slate-900">{{ child.name }}</h3>
      <p class="text-sm text-slate-600">Age: {{ child.age }} • Grade: {{ child.grade }}</p>
      <p class="mt-3 text-sm font-semibold text-slate-800">Monthly cost: {{ child.monthlyCost }}</p>
      <span
        class="mt-3 inline-flex rounded-full px-3 py-1 text-xs font-semibold"
        [ngClass]="{
          'bg-amber-100 text-amber-800': child.status === 'AVAILABLE',
          'bg-emerald-100 text-emerald-800': child.status === 'EARLY_SUPPORTED',
          'bg-slate-200 text-slate-700': child.status === 'SPONSORED'
        }"
      >
        {{
          child.status === 'AVAILABLE'
            ? 'Available'
            : child.status === 'EARLY_SUPPORTED'
              ? 'Early Supported'
              : 'Sponsored'
        }}
      </span>
      <div class="mt-4 flex gap-3">
        <a
          [routerLink]="['/children', child.id]"
          class="rounded bg-slate-900 px-4 py-2 text-xs font-semibold text-white"
        >
          View details
        </a>
        <a
          [routerLink]="['/children', child.id, 'sponsor']"
          class="rounded border border-slate-300 px-4 py-2 text-xs font-semibold text-slate-700"
          [class.pointer-events-none]="child.status === 'SPONSORED'"
          [class.opacity-50]="child.status === 'SPONSORED'"
          [attr.aria-disabled]="child.status === 'SPONSORED'"
        >
          Sponsor
        </a>
      </div>
    </div>
  `
})
export class ChildCardComponent {
  @Input({ required: true }) child!: ChildCardView;
}
