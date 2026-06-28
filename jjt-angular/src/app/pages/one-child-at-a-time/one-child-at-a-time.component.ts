import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SponsorService, ChildDto } from '../../services/sponsor.service';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';

type StatusFilter = 'AVAILABLE' | 'RESERVED' | 'ALL';

@Component({
  selector: 'app-one-child-at-a-time',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './one-child-at-a-time.component.html',
})
export class OneChildAtATimeComponent implements OnInit {
  allChildren: ChildDto[] = [];
  loading = true;
  error: string | null = null;

  searchQuery = '';
  statusFilter: StatusFilter = 'ALL';

  pageSize = 12;
  page = 0;

  constructor(private sponsorService: SponsorService, private router: Router) {}

  ngOnInit(): void {
    this.sponsorService.getChildren().subscribe({
      next: (data) => {
        this.allChildren = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Unable to load children right now. Please try again.';
        this.loading = false;
      }
    });
  }

  get filtered(): ChildDto[] {
    let list = this.allChildren;
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
