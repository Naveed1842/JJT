import { Component } from '@angular/core';

import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-site-footer',
  standalone: true,
  imports: [RouterLink],
  template: `
    <footer style="background:#1c352c;color:#9cc3b3;padding:40px 32px;margin-top:auto;">
      <div style="max-width:1200px;margin:0 auto;">
        <div class="footer-top" style="display:flex;justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:32px;">
          <div>
            <span style="display:inline-flex;align-items:center;gap:10px;">
              <img src="assets/logos/jjt-mark-reversed.svg" alt="" style="height:26px;width:auto;display:block;" />
              <span style="font-family:'Newsreader',serif;font-weight:600;font-size:22px;color:#fdfaf3;">JJT</span>
            </span>
            <p style="margin:10px 0 0;font-size:13px;max-width:300px;line-height:1.6;color:#9cc3b3;">
              Junior Jinnah Trust — educating orphan, needy, and underprivileged children across Pakistan since 2014.
            </p>
          </div>
          <div class="footer-links" style="display:flex;gap:36px;flex-wrap:wrap;">
            <div>
              <p style="font-size:11px;font-family:'IBM Plex Mono',monospace;letter-spacing:.1em;text-transform:uppercase;color:#6b9c8b;margin:0 0 12px;">Give</p>
              <a routerLink="/children" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;margin-bottom:6px;">Sponsor a child</a>
              <a routerLink="/why-give" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;margin-bottom:6px;">Give to the fund</a>
              <a routerLink="/children" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;">Browse children</a>
            </div>
            <div>
              <p style="font-size:11px;font-family:'IBM Plex Mono',monospace;letter-spacing:.1em;text-transform:uppercase;color:#6b9c8b;margin:0 0 12px;">Proof</p>
              <a routerLink="/trust" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;margin-bottom:6px;">How we're accountable</a>
              <a routerLink="/trust" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;margin-bottom:6px;">Annual report</a>
              <a routerLink="/trust" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;">Zakat policy</a>
            </div>
            <div>
              <p style="font-size:11px;font-family:'IBM Plex Mono',monospace;letter-spacing:.1em;text-transform:uppercase;color:#6b9c8b;margin:0 0 12px;">JJT</p>
              <a routerLink="/trust" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;margin-bottom:6px;">About</a>
              <a routerLink="/login" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;margin-bottom:6px;">Sign in</a>
              <a routerLink="/admin" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;">Admin</a>
            </div>
            <div>
              <p style="font-size:11px;font-family:'IBM Plex Mono',monospace;letter-spacing:.1em;text-transform:uppercase;color:#6b9c8b;margin:0 0 12px;">Contact</p>
              <p style="font-size:13px;margin:0 0 5px;">info&#64;jjtrust.org</p>
              <p style="font-size:13px;margin:0 0 5px;">+92-332-311-1345</p>
              <p style="font-size:13px;margin:0 0 8px;">Islamabad, Pakistan</p>
              <p style="font-size:11.5px;font-family:'IBM Plex Mono',monospace;margin:0 0 2px;color:#6b9c8b;">SAMBA BANK</p>
              <p style="font-size:11.5px;font-family:'IBM Plex Mono',monospace;margin:0;color:#6b9c8b;">A/C 2000848908</p>
            </div>
          </div>
        </div>
        <div class="footer-bottom" style="margin-top:32px;padding-top:20px;border-top:1px solid #2b4a3d;display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:10px;">
          <p style="font-size:12px;margin:0;color:#6b9c8b;">
            © 2026 Junior Jinnah Trust · Registered under Trust Act 1882
          </p>
          <p style="font-size:12px;margin:0;color:#6b9c8b;">Powered by TeleNoc</p>
        </div>
      </div>
    <style>
      footer a:hover { color: #fdfaf3 !important; }
      @media (max-width: 768px) {
        footer { padding: 32px 18px !important; }
        .footer-top { flex-direction: column !important; gap: 20px !important; }
        .footer-top > div:first-child p { max-width: 100% !important; }
        .footer-links { gap: 20px !important; flex-wrap: wrap !important; }
        .footer-bottom { flex-direction: column !important; gap: 6px !important; }
      }
    </style>
    </footer>
  `
})
export class SiteFooterComponent {}
