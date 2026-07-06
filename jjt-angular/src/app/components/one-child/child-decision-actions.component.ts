import { Component, EventEmitter, Output } from '@angular/core';


@Component({
  selector: 'app-child-decision-actions',
  standalone: true,
  imports: [],
  template: `
    <div class="relative flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-center">
      <div class="pointer-events-none absolute -inset-6 rounded-full bg-gradient-to-r from-[#2E4A3B]/60 via-[#1B2B22]/40 to-[#2E4A3B]/60 blur-[30px]"></div>
      <button
        type="button"
        class="relative w-full rounded-xl bg-gradient-to-r from-[#C38A2E] via-[#E2B763] to-[#C38A2E] px-7 py-3 text-sm font-semibold text-[#2B1B0B] shadow-[0_16px_35px_rgba(135,92,26,0.5)] transition hover:scale-[1.02] sm:w-auto"
        (click)="sponsor.emit()"
        aria-label="Sponsor this child this Ramadan"
      >
        🌙 Sponsor This Child This Ramadan
      </button>
      <button
        type="button"
        class="relative w-full rounded-xl border border-[#D7BF8D] bg-transparent px-7 py-2.5 text-sm font-semibold text-[#F2E6C7] shadow-[0_10px_25px_rgba(15,23,18,0.4)] backdrop-blur sm:w-auto"
        (click)="showAnother.emit()"
        aria-label="Show another child"
      >
        Show another child
      </button>
    </div>
  `
})
export class ChildDecisionActionsComponent {
  @Output() sponsor = new EventEmitter<void>();
  @Output() showAnother = new EventEmitter<void>();
}
