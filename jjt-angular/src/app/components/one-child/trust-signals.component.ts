import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-trust-signals',
  standalone: true,
  imports: [CommonModule],
  template: `
    <footer class="rounded-2xl border border-white/60 bg-white/75 px-6 py-4 text-center text-xs text-slate-600 shadow-sm backdrop-blur">
      <p>No payment is taken here. You will see bank details on sponsor page.</p>
      <p class="mt-2">Your support goes directly towards education.</p>
      <p class="mt-2">Monthly updates are shared.</p>
    </footer>
  `
})
export class TrustSignalsComponent {}
