import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';
import { SponsorService } from '../../services/sponsor.service';
import { ChildDto } from '../../services/api.models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit {
  private readonly sponsorService = inject(SponsorService);

  featuredChildren: ChildDto[] = [];
  totalChildren = 0;
  availableCount = 0;
  loadingFeatured = true;

  ngOnInit(): void {
    this.sponsorService.getChildren().subscribe({
      next: (children) => {
        this.totalChildren = children.length;
        this.availableCount = children.filter(c => c.availabilityStatus === 'AVAILABLE').length;
        this.featuredChildren = children
          .filter(c => c.availabilityStatus === 'AVAILABLE')
          .slice(0, 3);
        this.loadingFeatured = false;
      },
      error: () => { this.loadingFeatured = false; }
    });
  }

  badgeStyle(status: ChildDto['availabilityStatus']): string {
    const base = 'flex-shrink:0;font-size:11px;font-weight:600;border-radius:100px;padding:4px 9px;';
    if (status === 'RESERVED')  return base + 'background:#fdf7ec;color:#8a5f1f;';
    if (status === 'ALLOCATED') return base + 'background:#eef5f1;color:#214a3e;';
    return base + 'background:#f1ece2;color:#8a7a5f;';
  }

  statusLabel(status: ChildDto['availabilityStatus']): string {
    if (status === 'RESERVED')  return 'Bridged';
    if (status === 'ALLOCATED') return 'Sponsored';
    return 'Seeking';
  }
}
