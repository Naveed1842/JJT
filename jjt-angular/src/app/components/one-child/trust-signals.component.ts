import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-trust-signals',
  standalone: true,
  imports: [CommonModule],
  template: `
    <footer class="relative rounded-[26px] border border-[#E6D4AE]/70 bg-gradient-to-br from-[#FDF7EA] via-[#F7E6C4] to-[#F4DEB2] px-6 py-6 text-center text-xs text-[#4B3C1C] shadow-[0_16px_40px_rgba(12,20,17,0.5)]">
      <div class="absolute -top-4 left-1/2 -translate-x-1/2 rounded-full bg-emerald-700 px-3 py-1 text-[10px] font-semibold uppercase tracking-[0.2em] text-amber-50 shadow-lg">
        Trusted
      </div>
      <div class="grid gap-3 text-sm text-[#5C4A23] sm:grid-cols-4 sm:text-xs">
        <span class="inline-flex items-center justify-center gap-2">
          <span class="h-2 w-2 rounded-full bg-emerald-600"></span>
          Zakat Eligible
        </span>
        <span class="inline-flex items-center justify-center gap-2">
          <span class="h-2 w-2 rounded-full bg-emerald-600"></span>
          100% goes to education
        </span>
        <span class="inline-flex items-center justify-center gap-2">
          <span class="h-2 w-2 rounded-full bg-emerald-600"></span>
          Transparent monthly updates
        </span>
        <span class="inline-flex items-center justify-center gap-2">
          <span class="h-2 w-2 rounded-full bg-emerald-600"></span>
          Shariah compliant
        </span>
      </div>
      <p class="mt-4 text-xs font-semibold text-[#6C5630]">
        No payment is taken here. You will see bank details on sponsor page.
      </p>
      <p class="mt-2 text-xs text-[#6C5630]">Giving in Ramadan multiplies impact and keeps education open.</p>
      <div class="mt-4 flex flex-col items-center justify-center gap-3 sm:flex-row">
        <div class="rounded-full border border-[#D7BF8D] bg-[#FFF4DE] px-4 py-2 text-xs font-semibold text-[#6A4F1F]">
          Trusted by 1,200+ donors last Ramadan
        </div>
      </div>
    </footer>
  `
})
export class TrustSignalsComponent {}
