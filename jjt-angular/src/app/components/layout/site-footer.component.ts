import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-site-footer',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <footer style="background:#1c352c;color:#9cc3b3;padding:40px 32px;margin-top:auto;">
      <div style="max-width:1200px;margin:0 auto;">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:32px;">
          <div>
            <span style="font-family:'Newsreader',serif;font-weight:600;font-size:22px;color:#fdfaf3;">JJT</span>
            <p style="margin:10px 0 0;font-size:13px;max-width:300px;line-height:1.6;color:#9cc3b3;">
              Junior Jinnah Trust — educating orphan, needy, and underprivileged children across Pakistan since 2014.
            </p>
          </div>
          <div style="display:flex;gap:40px;flex-wrap:wrap;">
            <div>
              <p style="font-size:11px;font-family:'IBM Plex Mono',monospace;letter-spacing:.1em;text-transform:uppercase;color:#6b9c8b;margin:0 0 12px;">Pages</p>
              <a routerLink="/children" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;margin-bottom:6px;">Children</a>
              <a routerLink="/why-give" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;margin-bottom:6px;">Why give</a>
              <a routerLink="/trust" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;margin-bottom:6px;">Trust</a>
              <a routerLink="/login" style="display:block;font-size:13.5px;color:#9cc3b3;text-decoration:none;">Sign in</a>
            </div>
            <div>
              <p style="font-size:11px;font-family:'IBM Plex Mono',monospace;letter-spacing:.1em;text-transform:uppercase;color:#6b9c8b;margin:0 0 12px;">Contact</p>
              <p style="font-size:13px;margin:0 0 5px;">info&#64;jjtrust.org</p>
              <p style="font-size:13px;margin:0 0 5px;">+92-332-311-1345</p>
              <p style="font-size:13px;margin:0;">Islamabad, Pakistan</p>
            </div>
            <div>
              <p style="font-size:11px;font-family:'IBM Plex Mono',monospace;letter-spacing:.1em;text-transform:uppercase;color:#6b9c8b;margin:0 0 12px;">Bank</p>
              <p style="font-size:13px;margin:0 0 4px;">SAMBA BANK LIMITED</p>
              <p style="font-size:13px;margin:0 0 4px;font-family:'IBM Plex Mono',monospace;">A/C: 2000848908</p>
              <p style="font-size:12px;margin:0;font-family:'IBM Plex Mono',monospace;">PK27SAMB0000002000848908</p>
            </div>
          </div>
        </div>
        <div style="margin-top:32px;padding-top:20px;border-top:1px solid #2b4a3d;display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:10px;">
          <p style="font-size:12px;margin:0;color:#6b9c8b;">
            © 2026 Junior Jinnah Trust · Registered under Trust Act 1882
          </p>
          <p style="font-size:12px;margin:0;color:#6b9c8b;">Powered by TeleNoc</p>
        </div>
      </div>
    </footer>
  `
})
export class SiteFooterComponent {}
