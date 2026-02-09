import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OneChildViewModel } from '../../pages/one-child-at-a-time/one-child-at-a-time.models';

@Component({
  selector: 'app-single-child-focus-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section
      class="rounded-2xl border border-white/70 bg-white/90 px-6 py-6 shadow-[0_16px_40px_rgba(15,23,42,0.1)] backdrop-blur"
      [class.child-fade-enter]="animate"
      aria-live="polite"
    >
      <ng-container *ngIf="child; else empty">
        <div class="flex flex-col gap-6 md:flex-row md:items-center">
          <div class="flex h-28 w-28 items-center justify-center rounded-2xl bg-slate-50">
            <img
              src="assets/icons/child-avatar.svg"
              alt="Symbolic child avatar"
              class="h-16 w-16 opacity-80"
            />
          </div>

          <div class="flex-1">
            <div class="flex flex-wrap items-center gap-3">
              <h2 class="text-2xl font-semibold text-slate-900">{{ child.name }}</h2>
              <span
                class="rounded-full bg-emerald-50 px-3 py-1 text-[11px] font-semibold text-emerald-700"
                *ngIf="child.status === 'ALLOCATED'"
              >
                Sponsored
              </span>
              <span
                class="rounded-full bg-amber-50 px-3 py-1 text-[11px] font-semibold text-amber-700"
                *ngIf="child.status === 'RESERVED'"
              >
                Pending
              </span>
              <span
                class="rounded-full bg-orange-50 px-3 py-1 text-[11px] font-semibold text-orange-700"
                *ngIf="child.status === 'AVAILABLE'"
              >
                Needs sponsor
              </span>
            </div>

            <p class="mt-2 text-sm text-slate-600">
              {{ child.ageText }} - {{ child.city }}
            </p>

            <div class="mt-4 flex flex-wrap gap-2">
              <span
                *ngFor="let tag of child.tags"
                class="rounded-full border border-slate-200 bg-white px-3 py-1 text-xs font-semibold text-slate-600"
              >
                {{ tag }}
              </span>
            </div>
          </div>

          <div class="min-w-[200px] rounded-xl border border-slate-200 bg-slate-50 px-4 py-4 text-sm text-slate-700">
            <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">Monthly education cost</p>
            <p class="mt-2 text-lg font-semibold text-slate-900">{{ child.monthlyCost }}</p>
            <p class="mt-3 text-xs text-slate-500" *ngIf="child.trustNote">
              {{ child.trustNote }}
            </p>
          </div>
        </div>
      </ng-container>

      <ng-template #empty>
        <div class="text-center text-sm text-slate-500">
          No eligible child available right now.
        </div>
      </ng-template>
    </section>
  `
})
export class SingleChildFocusCardComponent {
  @Input() child: OneChildViewModel | null = null;
  @Input() animate = false;
}
