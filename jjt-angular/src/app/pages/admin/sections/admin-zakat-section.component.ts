import {
  ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter,
  OnInit, Output, inject,
} from '@angular/core';
import { NgClass } from '@angular/common';
import { AdminService } from '../../../services/admin.service';
import { DonationResponse, ZakatStats } from '../../../services/api.models';

/**
 * Zakat module: Zakat donations tracked independently of general giving
 * (headline figures + the full ZAKAT-typed donation ledger).
 *
 * Recording and receipt-viewing reuse the host's existing modals — the section
 * emits and the admin shell opens the shared modal with type preset to ZAKAT.
 */
@Component({
  selector: 'app-admin-zakat-section',
  standalone: true,
  imports: [NgClass],
  templateUrl: './admin-zakat-section.component.html',
  styleUrl: '../admin.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminZakatSectionComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly cdr = inject(ChangeDetectorRef);

  @Output() recordZakat = new EventEmitter<void>();
  @Output() viewReceipt = new EventEmitter<string>();

  stats: ZakatStats | null = null;
  donations: DonationResponse[] = [];
  loading = false;
  page = 0;
  totalPages = 1;
  totalElements = 0;

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loadStats();
    this.load(this.page);
  }

  load(page = 0): void {
    this.loading = true;
    this.page = page;
    this.adminService.listDonations(page, 'ZAKAT').subscribe({
      next: (data) => {
        this.donations = data.content;
        this.totalPages = data.totalPages || 1;
        this.totalElements = data.totalElements;
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => { this.loading = false; this.cdr.markForCheck(); }
    });
  }

  private loadStats(): void {
    this.adminService.getZakatStats().subscribe({
      next: (data) => { this.stats = data; this.cdr.markForCheck(); },
      error: () => {}
    });
  }

  receive(id: string): void {
    this.adminService.receiveDonation(id).subscribe({
      next: () => this.refresh(),
      error: () => {}
    });
  }

  reverse(id: string): void {
    this.adminService.reverseDonation(id).subscribe({
      next: () => this.refresh(),
      error: () => {}
    });
  }

  donationStatusClass(status: string): string {
    if (status === 'RECEIPTED') return 'badge-green';
    if (status === 'REVERSED')  return 'badge-red';
    return 'badge-grey';
  }

  formatAmount(val: string | null, currency = 'PKR'): string {
    if (val == null) return '—';
    const n = parseFloat(val);
    return `${currency} ${n.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 2 })}`;
  }
}
