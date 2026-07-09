import { Component, Input } from '@angular/core';


@Component({
  selector: 'app-child-snapshot',
  standalone: true,
  imports: [],
  template: `
    <div class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-4 flex items-center gap-4">
        <div class="h-20 w-20 rounded bg-slate-100"></div>
        @if (age !== null) {
<div>
          <p class="text-sm text-slate-600">Age</p>
          <p class="text-lg font-semibold text-slate-900">{{ age }}</p>
        </div>
}
        <div>
          <p class="text-sm text-slate-600">Grade</p>
          <p class="text-lg font-semibold text-slate-900">{{ grade }}</p>
        </div>
      </div>
      <div class="border-t pt-4">
        <p class="text-sm text-slate-600">Monthly cost</p>
        <p class="text-lg font-semibold text-slate-900">{{ monthlyCost }}</p>
      </div>
    </div>
  `
})
export class ChildSnapshotComponent {
  @Input() age: number | null = null;
  @Input({ required: true }) grade!: string;
  @Input({ required: true }) monthlyCost!: string;
}
