import { Component, Input } from '@angular/core';


export interface ProgressItem {
  month: string;
  summary: string;
}

@Component({
  selector: 'app-progress-list',
  standalone: true,
  imports: [],
  template: `
    <div class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <h3 class="text-base font-semibold text-slate-900">Recent progress</h3>
      <div class="mt-4 space-y-3">
        @for (update of updates; track update.id) {
<div class="rounded border border-slate-200 p-3">
          <p class="text-xs font-semibold uppercase text-slate-500">{{ update.month }}</p>
          <p class="mt-2 text-sm text-slate-700">{{ update.summary }}</p>
        </div>
}
      </div>
    </div>
  `
})
export class ProgressListComponent {
  @Input({ required: true }) updates: ProgressItem[] = [];
}
