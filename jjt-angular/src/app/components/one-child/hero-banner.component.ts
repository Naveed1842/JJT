import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-hero-banner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section
      class="relative overflow-hidden rounded-[28px] border border-[#F2D9A4]/60 bg-gradient-to-br from-[#FDF7EA] via-[#F7E8C6] to-[#F9F0DA] px-7 py-8 text-center text-[#2B2A24] shadow-[0_30px_80px_rgba(12,20,17,0.55)]"
      aria-live="polite"
    >
      <div class="pointer-events-none absolute inset-0 opacity-35" style="background: radial-gradient(circle at top, rgba(255,255,255,0.75), transparent 55%);"></div>
      <div class="pointer-events-none absolute left-6 top-5 h-24 w-24 rounded-full bg-[#FFE9BE]/70 blur-[30px]"></div>
      <div class="pointer-events-none absolute right-8 bottom-4 h-24 w-24 rounded-full bg-[#EED8A2]/70 blur-[40px]"></div>
      <div class="pointer-events-none absolute inset-x-10 bottom-0 h-20 opacity-30">
        <svg viewBox="0 0 640 140" class="h-full w-full" aria-hidden="true">
          <path d="M0 120L60 90L120 110L180 80L240 100L300 70L360 95L420 80L480 105L540 85L600 100L640 92V140H0Z" fill="#D5B57B"/>
          <path d="M32 120h24v20H32zM88 100h28v40H88zM150 112h22v28H150zM210 96h26v44H210zM280 110h30v30H280zM350 92h26v48H350zM420 108h28v32H420zM500 100h24v40H500z" fill="#C7A66C"/>
        </svg>
      </div>
      <svg class="pointer-events-none absolute left-8 top-6 h-20 w-20 opacity-35" viewBox="0 0 80 80" fill="none" aria-hidden="true">
        <path d="M54 12C42.7 13.6 34 23.1 34 34.8C34 47 43.8 56.8 56 56.8C60.6 56.8 65 55.4 68.5 53C62.7 62.2 52.8 68 41 68C23 68 8 53 8 35C8 16.7 22.6 2 40.7 2C45.2 2 49.6 3 54 12Z" fill="#C9A45D"/>
      </svg>
      <p class="flex items-center justify-center gap-2 text-[11px] font-semibold uppercase tracking-[0.45em] text-[#8B6A33]">
        <span class="h-1 w-1 rounded-full bg-[#C9A45D]"></span>
        Ramadan focus
        <span class="h-1 w-1 rounded-full bg-[#C9A45D]"></span>
      </p>
      <h1 class="mt-4 text-3xl font-semibold text-[#1F2A1E] md:text-4xl">
        {{ headline }}
      </h1>
      <div class="mx-auto mt-4 h-0.5 w-28 rounded-full bg-gradient-to-r from-transparent via-[#C9A45D] to-transparent"></div>
      <p class="mx-auto mt-4 max-w-2xl text-sm text-[#3D3A2F]">
        {{ subtext }}
      </p>
      <p class="mt-3 text-xs font-semibold text-[#5D4B26]">
        Multiply your rewards this blessed month.
      </p>
    </section>
  `
})
export class HeroBannerComponent {
  @Input() headline = "This Ramadan, Change One Child's Future.";
  @Input() subtext = 'This child is out of school and needs support now.';
}
