import { Component, Input } from '@angular/core';

import { OneChildViewModel } from '../../pages/one-child-at-a-time/one-child-at-a-time.models';

@Component({
  selector: 'app-single-child-focus-card',
  standalone: true,
  imports: [],
  template: `
    <section
      class="relative rounded-[28px] border border-[#F2D9A4]/60 bg-gradient-to-br from-[#FDF7EA] via-[#F8E8C8] to-[#F6E2BE] px-7 py-7 text-[#2B2A24] shadow-[0_24px_70px_rgba(13,21,18,0.55)]"
      [class.child-fade-enter]="animate"
      aria-live="polite"
    >
      <div
        class="pointer-events-none absolute inset-0 opacity-35"
        style="background: radial-gradient(circle at 20% 20%, rgba(255,255,255,0.8), transparent 45%), radial-gradient(circle at 80% 80%, rgba(233,210,159,0.6), transparent 55%);"
      ></div>
      @if (child) {

        <div class="relative flex flex-col gap-7 md:flex-row md:items-center">
          <div class="relative flex h-28 w-28 items-center justify-center rounded-3xl bg-gradient-to-br from-[#FFF6E0] to-[#EAD6AE] shadow-[inset_0_0_18px_rgba(200,160,86,0.4)]">
            <div class="absolute -right-3 -top-3 flex h-7 w-7 items-center justify-center rounded-full bg-emerald-600 text-white shadow-lg">
              <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M5 12l4 4 10-10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <img
              src="assets/icons/child-avatar.svg"
              alt="Symbolic child avatar"
              class="h-16 w-16 opacity-90"
            />
            <span class="absolute bottom-2 left-1/2 -translate-x-1/2 rounded-full bg-emerald-600/90 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-white">
              Verified child
            </span>
          </div>

          <div class="flex-1">
            <div class="flex flex-wrap items-center gap-3">
              <h2 class="text-2xl font-semibold text-[#1F2A1E]">{{ child.name }}</h2>
              <span class="rounded-full border border-[#D8BE7B] bg-[#FFF1CC] px-3 py-1 text-[11px] font-semibold text-[#7A5A22]">
                Verified child
              </span>
              @if (child.status === 'ALLOCATED') {
<span
                class="rounded-full bg-emerald-50 px-3 py-1 text-[11px] font-semibold text-emerald-700"
               
              >
                Sponsored
              </span>
}
              @if (child.status === 'RESERVED') {
<span
                class="rounded-full bg-amber-50 px-3 py-1 text-[11px] font-semibold text-amber-700"
               
              >
                Pending
              </span>
}
              @if (child.status === 'AVAILABLE') {
<span
                class="rounded-full bg-orange-50 px-3 py-1 text-[11px] font-semibold text-orange-700"
               
              >
                Needs sponsor
              </span>
}
            </div>

            @if (child.storyLine) {
<p class="mt-2 inline-flex items-center gap-2 text-sm font-semibold text-[#5D4B26]">
              <span class="inline-flex h-6 w-6 items-center justify-center rounded-full bg-[#F1D7A2] text-[#7A5A22]">
                <svg class="h-3 w-3" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M12 3l2.2 4.7 5.1.7-3.7 3.6.9 5.1L12 14.8 7.5 17l.9-5.1L4.7 8.4l5.1-.7L12 3z" fill="currentColor"/>
                </svg>
              </span>
              {{ child.storyLine }}
            </p>
}

            <div class="mt-2 flex flex-wrap items-center gap-4 text-sm text-[#4B3C1C]">
              <span class="inline-flex items-center gap-2">
                <svg class="h-4 w-4 text-[#9C7A3A]" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M12 4a5 5 0 100 10 5 5 0 000-10z" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M4 20c1.6-3.4 4.5-5 8-5s6.4 1.6 8 5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                {{ child.ageText }}
              </span>
              <span class="inline-flex items-center gap-2">
                <svg class="h-4 w-4 text-[#9C7A3A]" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M12 3a7 7 0 00-7 7c0 4.7 7 11 7 11s7-6.3 7-11a7 7 0 00-7-7z" stroke="currentColor" stroke-width="1.5"/>
                  <circle cx="12" cy="10" r="2.5" stroke="currentColor" stroke-width="1.5"/>
                </svg>
                {{ child.city }}
              </span>
            </div>

            <div class="mt-4 flex flex-wrap gap-2">
              @for (tag of child.tags; track tag) {
<span
               
                class="rounded-full border border-[#E6D4AE] bg-white/80 px-3 py-1 text-xs font-semibold text-[#6A4F1F] shadow-sm"
              >
                {{ tag }}
              </span>
}
            </div>
          </div>

          <div class="relative min-w-[220px] overflow-hidden rounded-2xl border border-[#E4D0A7]/80 bg-gradient-to-br from-[#FFF6E3] to-[#F4E0B8] px-5 py-5 text-sm text-[#4B3C1C] shadow-[0_16px_35px_rgba(76,59,22,0.2)]">
            <div class="pointer-events-none absolute right-4 top-4 rounded-full bg-emerald-700/90 px-3 py-1 text-[10px] font-semibold uppercase tracking-[0.18em] text-amber-50">
              Coverage
            </div>
            <p class="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-[0.2em] text-[#7A5A22]">
              <svg class="h-4 w-4 text-[#B48A3C]" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M4 7h16v10H4z" stroke="currentColor" stroke-width="1.5" />
                <path d="M7 10h4M7 13h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
              Monthly educ
            </p>
            <div class="mt-4 flex items-center gap-4">
              <div class="relative h-20 w-20">
                <div
                  class="absolute inset-0 rounded-full"
                  [style.background]="'conic-gradient(#6C8A4A ' + (child.coveragePercent ?? 0) + '%, #E7D8B6 0)'"
                ></div>
                <div class="absolute inset-2 rounded-full bg-[#FFF4DE]"></div>
                <div class="absolute inset-0 flex items-center justify-center text-xs font-semibold text-[#4B3C1C]">
                  {{ child.coveragePercent ?? 0 }}%
                </div>
              </div>
              <div class="flex-1">
                @if (child.dailyCost) {
<p class="text-xs font-semibold text-[#7A5A22]">
                  {{ child.dailyCost }}
                </p>
}
                <p class="mt-1 text-lg font-semibold text-[#1F2A1E]">{{ child.monthlyCost }}</p>
                @if (child.trustNote) {
<p class="mt-2 text-[11px] text-[#6C5630]">
                  {{ child.trustNote }}
                </p>
}
              </div>
            </div>
            @if (child.ramadanDonors !== undefined) {
<p class="mt-3 text-[11px] text-[#6C5630]">
              {{ child.ramadanDonors }} donors already helped this Ramadan
            </p>
}
          </div>
        </div>
      
} @else {

        <div class="text-center text-sm text-[#6C5630]">
          No eligible child available right now.
        </div>
      
}

      
    </section>
  `
})
export class SingleChildFocusCardComponent {
  @Input() child: OneChildViewModel | null = null;
  @Input() animate = false;
}
