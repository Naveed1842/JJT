import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ChildDto } from '../../services/api.models';
import { ChildrenStore } from '../../services/children.store';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';

type StatusFilter = 'AVAILABLE' | 'RESERVED' | 'ALL';

@Component({
  selector: 'app-one-child-at-a-time',
  standalone: true,
  imports: [FormsModule, RouterLink, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './one-child-at-a-time.component.html',
  // Safe under OnPush: state is store signals + fields mutated only by template events
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OneChildAtATimeComponent implements OnInit {
  private readonly store = inject(ChildrenStore);

  searchQuery = '';
  statusFilter: StatusFilter = 'ALL';

  pageSize = 12;
  page = 0;

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.store.load(); // served from cache when fresh — no refetch per navigation
  }

  get loading(): boolean       { return this.store.loading(); }
  get error(): string | null   { return this.store.error(); }

  get filtered(): ChildDto[] {
    let list = this.store.children();
    if (this.statusFilter !== 'ALL') {
      list = list.filter(c => c.availabilityStatus === this.statusFilter);
    }
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      list = list.filter(c =>
        c.fullName.toLowerCase().includes(q) ||
        c.city.toLowerCase().includes(q) ||
        c.campusName.toLowerCase().includes(q)
      );
    }
    return list;
  }

  get visible(): ChildDto[] {
    return this.filtered.slice(0, this.pageSize * (this.page + 1));
  }

  get hasMore(): boolean {
    return this.visible.length < this.filtered.length;
  }

  loadMore(): void {
    this.page++;
  }

  setFilter(f: StatusFilter): void {
    this.statusFilter = f;
    this.page = 0;
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.statusFilter = 'ALL';
    this.page = 0;
  }

  statusLabel(status: ChildDto['availabilityStatus']): string {
    switch (status) {
      case 'AVAILABLE': return 'Seeking';
      case 'RESERVED':  return 'Bridged';
      case 'ALLOCATED': return 'Sponsored';
      default:          return status;
    }
  }

  badgeStyle(status: ChildDto['availabilityStatus']): string {
    switch (status) {
      case 'AVAILABLE':
        return 'background:#f1ece2;color:#8a7a5f;font-size:10.5px;font-weight:600;border-radius:100px;padding:3px 8px;';
      case 'RESERVED':
        return 'background:#fdf7ec;color:#8a5f1f;font-size:10.5px;font-weight:600;border-radius:100px;padding:3px 8px;';
      case 'ALLOCATED':
        return 'background:#eef5f1;color:#214a3e;font-size:10.5px;font-weight:600;border-radius:100px;padding:3px 8px;';
      default:
        return 'background:#f1ece2;color:#8a7a5f;font-size:10.5px;font-weight:600;border-radius:100px;padding:3px 8px;';
    }
  }

  openChild(id: string): void {
    this.router.navigate(['/children', id]);
  }

  sponsorChild(event: Event, id: string): void {
    event.stopPropagation();
    this.router.navigate(['/children', id, 'sponsor']);
  }
}
