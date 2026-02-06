import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SponsorService, AvailabilityStatus } from '../../services/sponsor.service';
import { ChildSnapshotComponent } from '../../components/child-snapshot/child-snapshot.component';
import { LedgerRow, LedgerTableComponent } from '../../components/ledger-table/ledger-table.component';
import { ProgressItem, ProgressListComponent } from '../../components/progress-list/progress-list.component';

@Component({
  selector: 'app-child-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ChildSnapshotComponent,
    LedgerTableComponent,
    ProgressListComponent
  ],
  templateUrl: './child-detail.component.html',
  styleUrl: './child-detail.component.css'
})
export class ChildDetailComponent implements OnInit {
  childId: string | null = null;
  childName = '';
  age = 8;
  grade = 'Grade 3';
  monthlyCost = '2,000 PKR';
  supportStatus: AvailabilityStatus = 'AVAILABLE';
  ledgerEntries: LedgerRow[] = [];
  progressUpdates: ProgressItem[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private sponsorService: SponsorService
  ) {}

  ngOnInit() {
    this.childId = this.route.snapshot.paramMap.get('childId');
    if (this.childId) {
      this.loadChildData();
    } else {
      this.error = 'Child not found.';
      this.loading = false;
    }
  }

  loadChildData() {
    if (!this.childId) {
      return;
    }
    this.sponsorService.getChild(this.childId).subscribe({
      next: (data) => {
        this.childName = data.fullName;
        this.supportStatus = data.availabilityStatus;
        this.loadLedger();
        this.loadProgress();
      },
      error: (err) => {
        this.error = 'Failed to load child details.';
        this.loading = false;
        console.error('Error loading child:', err);
      }
    });
  }

  loadLedger() {
    if (!this.childId) {
      return;
    }
    this.sponsorService.getLedger(this.childId).subscribe({
      next: (data) => {
        this.ledgerEntries = (data.entries || []).map((entry, index) => ({
          month: entry.month,
          status: 'Continued',
          source: index % 2 === 0 ? 'Early Support' : 'Sponsor'
        }));
      },
      error: (err) => console.error('Error loading ledger:', err)
    });
  }

  loadProgress() {
    if (!this.childId) {
      return;
    }
    this.sponsorService.getProgress(this.childId).subscribe({
      next: (data) => {
        this.progressUpdates = (data || []).slice(0, 2).map(update => ({
          month: update.month,
          summary: update.summary
        }));
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading progress:', err);
        this.loading = false;
      }
    });
  }
}
