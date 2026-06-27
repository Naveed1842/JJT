import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';
import { SponsorService } from '../../services/sponsor.service';
import { AuthService } from '../../services/auth.service';
import { ChildDto, LedgerDto, ProgressUpdateDto } from '../../services/api.models';

interface ChildDetail {
  child: ChildDto;
  ledger: LedgerDto | null;
  progress: ProgressUpdateDto[];
  expanded: boolean;
  loadingDetail: boolean;
}

@Component({
  selector: 'app-sponsor-portal',
  standalone: true,
  imports: [CommonModule, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './sponsor-portal.component.html',
})
export class SponsorPortalComponent implements OnInit {
  private readonly sponsorService = inject(SponsorService);
  private readonly authService = inject(AuthService);

  children: ChildDetail[] = [];
  loading = true;
  error: string | null = null;

  get sponsorEmail(): string {
    return this.authService.getCurrentUser()?.email ?? '';
  }

  ngOnInit(): void {
    this.loadChildren();
  }

  loadChildren(): void {
    this.loading = true;
    this.error = null;

    this.sponsorService.getSponsorChildren().subscribe({
      next: (list) => {
        this.loading = false;
        this.children = list.map(child => ({
          child,
          ledger: null,
          progress: [],
          expanded: false,
          loadingDetail: false,
        }));
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message ?? 'Failed to load your sponsored children.';
      }
    });
  }

  toggleChild(detail: ChildDetail): void {
    detail.expanded = !detail.expanded;
    if (detail.expanded && detail.ledger === null) {
      this.loadDetail(detail);
    }
  }

  loadDetail(detail: ChildDetail): void {
    detail.loadingDetail = true;
    const id = detail.child.id;

    this.sponsorService.getSponsorLedger(id).subscribe({
      next: (l) => { detail.ledger = l; },
      error: () => { /* leave null */ }
    });

    this.sponsorService.getSponsorProgress(id).subscribe({
      next: (p) => {
        detail.progress = [...p].sort((a, b) => b.month.localeCompare(a.month));
        detail.loadingDetail = false;
      },
      error: () => { detail.loadingDetail = false; }
    });
  }

  statusBadgeStyle(status: string): string {
    switch (status) {
      case 'AVAILABLE': return 'background:#f1ece2;color:#8a7a5f;font-size:11px;font-weight:600;border-radius:100px;padding:4px 10px;';
      case 'RESERVED':  return 'background:#fdf7ec;color:#8a5f1f;font-size:11px;font-weight:600;border-radius:100px;padding:4px 10px;';
      case 'ALLOCATED': return 'background:#eef5f1;color:#214a3e;font-size:11px;font-weight:600;border-radius:100px;padding:4px 10px;';
      default:          return 'background:#f1ece2;color:#8a7a5f;font-size:11px;font-weight:600;border-radius:100px;padding:4px 10px;';
    }
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'AVAILABLE': return 'Seeking';
      case 'RESERVED':  return 'Bridged';
      case 'ALLOCATED': return 'Sponsored';
      default:          return status;
    }
  }

  coverageLabel(type: string): string {
    return type === 'EARLY_SUPPORT' ? 'Early Support' : 'Sponsored';
  }
}
