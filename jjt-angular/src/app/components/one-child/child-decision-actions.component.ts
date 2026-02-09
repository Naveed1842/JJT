import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-child-decision-actions',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-center">
      <button
        type="button"
        class="btn-primary w-full px-6 py-2 text-sm font-semibold sm:w-auto"
        (click)="sponsor.emit()"
        aria-label="Sponsor this child"
      >
        Sponsor this child
      </button>
      <button
        type="button"
        class="btn-secondary w-full px-6 py-2 text-sm font-semibold sm:w-auto"
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
