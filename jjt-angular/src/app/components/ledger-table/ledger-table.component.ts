import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface LedgerRow {
  month: string;
  status: string;
  source: string;
}

@Component({
  selector: 'app-ledger-table',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <h3 class="text-base font-semibold text-slate-900">Education continuity</h3>
      <table class="mt-4 w-full text-left text-sm">
        <thead class="text-slate-500">
          <tr>
            <th class="pb-2">Month</th>
            <th class="pb-2">Status</th>
            <th class="pb-2">Source</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let entry of entries" class="border-t">
            <td class="py-2 text-slate-900">{{ entry.month }}</td>
            <td class="py-2 text-slate-700">{{ entry.status }}</td>
            <td class="py-2 text-slate-700">{{ entry.source }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class LedgerTableComponent {
  @Input({ required: true }) entries: LedgerRow[] = [];
}
