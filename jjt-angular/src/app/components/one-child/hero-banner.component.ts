import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-hero-banner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section
      class="rounded-2xl border border-white/60 bg-white/85 px-6 py-7 text-center shadow-[0_18px_45px_rgba(15,23,42,0.12)] backdrop-blur"
      aria-live="polite"
    >
      <p class="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">
        One child at a time
      </p>
      <h1 class="mt-3 text-3xl font-semibold text-slate-900 md:text-4xl">
        {{ headline }}
      </h1>
      <p class="mx-auto mt-3 max-w-2xl text-sm text-slate-600">
        {{ subtext }}
      </p>
    </section>
  `
})
export class HeroBannerComponent {
  @Input() headline = "You don't need to help everyone. Just one child.";
  @Input() subtext = 'This child is out of school and needs support now.';
}
