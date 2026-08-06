import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../services/admin.service';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';
import { TransparencyResponse } from '../../services/api.models';

@Component({
  selector: 'app-trust',
  standalone: true,
  imports: [CommonModule, RouterLink, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './trust.component.html',
})
export class TrustComponent implements OnInit {
  private readonly svc = inject(AdminService);

  transparency: TransparencyResponse | null = null;

  // Fallback values used when live data is unavailable
  readonly fallback = {
    programmePct: 87,
    fundraisingPct: 9,
    adminPct: 4,
  };

  ngOnInit(): void {
    this.svc.getPublicTransparency().subscribe({
      next: (d) => { this.transparency = d; },
      error: () => { /* keep fallback values */ }
    });
  }

  get programmePct(): number {
    return parseFloat(this.transparency?.programmePct ?? '') || this.fallback.programmePct;
  }

  get fundraisingPct(): number {
    return parseFloat(this.transparency?.fundraisingPct ?? '') || this.fallback.fundraisingPct;
  }

  get adminPct(): number {
    return parseFloat(this.transparency?.adminPct ?? '') || this.fallback.adminPct;
  }

  get asOf(): string | null {
    return this.transparency?.asOf ?? null;
  }

  get beneficiaryCount(): number {
    return this.transparency?.beneficiaryCount ?? 0;
  }
}
