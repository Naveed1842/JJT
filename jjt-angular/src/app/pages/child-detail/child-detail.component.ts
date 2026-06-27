import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SponsorService, AvailabilityStatus } from '../../services/sponsor.service';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';

interface LedgerRow { month: string; status: string; source: string; }
interface ProgressItem { month: string; summary: string; }

@Component({
  selector: 'app-child-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './child-detail.component.html',
})
export class ChildDetailComponent implements OnInit {
  childId: string | null = null;
  childName = '';
  rollNumber = '';
  city = '';
  campusName = '';
  schoolName: string | null = null;
  monthlyCost = '—';
  supportStatus: AvailabilityStatus = 'AVAILABLE';
  ledgerEntries: LedgerRow[] = [];
  progressUpdates: ProgressItem[] = [];
  loading = true;
  error: string | null = null;

  constructor(private route: ActivatedRoute, private sponsorService: SponsorService) {}

  ngOnInit(): void {
    this.childId = this.route.snapshot.paramMap.get('childId');
    if (this.childId) {
      this.loadChildData();
    } else {
      this.error = 'Child not found.';
      this.loading = false;
    }
  }

  private loadChildData(): void {
    this.sponsorService.getChild(this.childId!).subscribe({
      next: (data) => {
        this.childName    = data.fullName;
        this.rollNumber   = data.rollNumber;
        this.city         = data.city;
        this.campusName   = data.campusName;
        this.schoolName   = data.schoolName;
        this.monthlyCost  = `${data.educationCurrency} ${data.educationAmount}`;
        this.supportStatus = data.availabilityStatus;
        this.loadLedger();
        this.loadProgress();
      },
      error: () => {
        this.error = 'Failed to load child details.';
        this.loading = false;
      }
    });
  }

  private loadLedger(): void {
    this.sponsorService.getLedger(this.childId!).subscribe({
      next: (data) => {
        this.ledgerEntries = (data.entries ?? []).map(e => ({
          month: e.month,
          status: 'Continued',
          source: e.coverageType === 'SPONSOR' ? 'Sponsor' : 'Early Support'
        }));
      }
    });
  }

  private loadProgress(): void {
    this.sponsorService.getProgress(this.childId!).subscribe({
      next: (data) => {
        this.progressUpdates = (data ?? []).map(u => ({ month: u.month, summary: u.summary }));
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }
}
