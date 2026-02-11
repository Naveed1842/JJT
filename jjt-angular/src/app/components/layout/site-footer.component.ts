import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-site-footer',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <footer class="bg-[#0B1512] text-amber-100 py-12 border-t border-[#2B3A33]">
      <div class="mx-auto max-w-6xl px-4">
        <div class="grid gap-10 lg:grid-cols-12">
          <div class="lg:col-span-5">
            <img
              src="assets/images/JJT-Logo-768x251.png"
              alt="Junior Jinnah Trust"
              class="h-16 w-auto"
            />
            <p class="mt-4 text-sm text-amber-100/70">
              Junior Jinnah Trust is an authoritative and registered organization under Trust Act 1882 that has been
              providing education to orphans, Needy, Underprivileged, and Zakat Eligible Children and catering to the
              educational needs of Child Labor, Rag picking & Beggary victims in Pakistan since 2014.
            </p>
          </div>

          <div class="lg:col-span-3">
            <p class="text-sm font-semibold text-amber-50">Get in Touch</p>
            <p class="mt-3 text-sm text-amber-100/70">
              1st Floor, Plot-4, Al-Rehman Plaza, Ghouri VIP Phase, Express Way, Islamabad
            </p>
            <p class="mt-3 text-sm text-amber-100/70">Cell: +92-332-311-1345</p>
            <p class="text-sm text-amber-100/70">Cell: +92-332-548-8876</p>
            <p class="mt-3 text-sm text-amber-100/70">Mail: info&#64;jjtrust.org</p>
            <a href="#" class="mt-4 inline-flex text-sm font-semibold text-amber-200 hover:text-amber-100">
              Get Direction
            </a>
          </div>

          <div class="lg:col-span-2">
            <p class="text-sm font-semibold text-amber-50">Documents</p>
            <ul class="mt-3 space-y-2 text-sm text-amber-100/70">
              <li>Junior Jinnah Trust (Newsletter) Oct-24</li>
              <li>JJT-Rregistration Certificate-2018</li>
              <li>JJT-Registration-24-25</li>
              <li>FBR Certificate</li>
            </ul>
          </div>

          <div class="lg:col-span-2">
            <p class="text-sm font-semibold text-amber-50">Donate From Anywhere</p>
            <div class="mt-3 space-y-2 text-sm text-amber-100/70">
              <p>SAMBA BANK LIMITED</p>
              <p>JUNIOR JINNAH TRUST</p>
              <p>A/C#: 2000848908</p>
              <p>IBAN# PK27SAMB0000002000848908</p>
              <p>Jinnah Avenue, Islamabad</p>
            </div>
          </div>
        </div>

        <div class="mt-10 flex flex-wrap items-center justify-between gap-4 border-t border-[#2B3A33] pt-6 text-sm text-amber-100/70">
          <div class="flex flex-wrap gap-5">
            <a routerLink="/children" class="hover:text-amber-100">About</a>
            <a routerLink="/admin" class="hover:text-amber-100">How it works</a>
            <a routerLink="/children" class="hover:text-amber-100">Contact</a>
            <span>Facebook</span>
            <span>Instagram</span>
            <span>Linkedin-in</span>
          </div>
          <p class="text-xs text-amber-100/50">
            All Right Reserved. JJ-Trust &#64;2024 – Powered By TeleNoc
          </p>
        </div>
      </div>
    </footer>
  `
})
export class SiteFooterComponent {
}
