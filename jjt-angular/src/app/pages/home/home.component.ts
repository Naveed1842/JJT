import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';

import { RouterLink } from '@angular/router';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';
import { ChildrenStore } from '../../services/children.store';
import { ChildDto } from '../../services/api.models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './home.component.html',
  // Safe under OnPush: all template state is store signals (tracked reactively)
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomeComponent implements OnInit {
  private readonly store = inject(ChildrenStore);

  ngOnInit(): void {
    this.store.load(); // served from cache when fresh — no refetch per navigation
  }

  get featuredChildren(): ChildDto[] { return this.store.featured(); }
  get totalChildren(): number        { return this.store.totalCount(); }
  get availableCount(): number       { return this.store.availableCount(); }
  get loadingFeatured(): boolean     { return this.store.loading(); }

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
