import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SponsorService, CommitmentType, AvailabilityStatus } from '../../services/sponsor.service';
import { RamadanLoaderComponent } from '../../components/ramadan-loader/ramadan-loader.component';

@Component({
  selector: 'app-sponsor-commit',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RamadanLoaderComponent
  ],
  templateUrl: './sponsor-commit.component.html',
  styleUrl: './sponsor-commit.component.css'
})
export class SponsorCommitComponent implements OnInit {
  childId: string | null = null;
  childName = '';
  monthlyCost = '';
  campusName = '';
  city = '';
  supportStatus: AvailabilityStatus = 'AVAILABLE';

  sponsorName = '';
  email = '';
  phone = '';
  commitmentType: CommitmentType = 'MONTHLY';

  loading = false;
  pageLoading = true;
  error: string | null = null;
  submitted = false;
  startMonth: string | null = null;

  readonly paymentInfo = {
    accountTitle: 'JUNIOR JINNAH TRUST',
    accountNumber: '2000848908',
    iban: 'PK27SAMB0000002000848908',
    bankName: 'SAMBA BANK LIMITED'
  };

  copiedField: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private sponsorService: SponsorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.childId = this.route.snapshot.paramMap.get('childId');
    if (this.childId) {
      // Backend contract: fetch child via /api/org/children/{id} (requires X-ROLE)
      this.sponsorService.getChild(this.childId).subscribe({
        next: (child) => {
          this.childName = child.fullName;
          this.supportStatus = child.availabilityStatus;
          this.monthlyCost = `${child.educationCurrency} ${child.educationAmount}`;
          this.campusName = child.campusName;
          this.city = child.city;
          this.pageLoading = false;
        },
        error: () => {
          this.error = 'Failed to load child details.';
          this.pageLoading = false;
        }
      });
    } else {
      this.error = 'Child not found.';
      this.pageLoading = false;
    }
  }

  async copy(value: string, field: string) {
    try {
      await navigator.clipboard.writeText(value);
      this.copiedField = field;
      setTimeout(() => (this.copiedField = null), 2000);
    } catch (err) {
      console.error('Copy failed', err);
    }
  }

  submit(): void {
    this.error = null;
    if (this.loading || this.submitted) {
      return;
    }
    if (this.supportStatus === 'ALLOCATED') {
      this.error = 'This child already has an active sponsorship.';
      return;
    }
    if (this.supportStatus === 'RESERVED') {
      this.error = 'This child is reserved and pending activation.';
      return;
    }
    if (!this.childId) {
      this.error = 'Child not found.';
      return;
    }
    if (!this.sponsorName.trim() || !this.email.trim()) {
      this.error = 'Please provide your name and email.';
      return;
    }

    this.loading = true;
    // Backend contract: POST /api/public/sponsorships (creates PENDING sponsorship)
    this.sponsorService.commitSponsorship({
      childId: this.childId,
      commitmentType: this.commitmentType,
      sponsor: {
        name: this.sponsorName,
        email: this.email,
        phone: this.phone || null
      }
    }).subscribe({
      next: (response) => {
        // Backend lifecycle: public commit creates PENDING sponsorship; activation is manual.
        this.loading = false;
        this.submitted = true;
        this.startMonth = response.startMonth;
      },
      error: () => {
        this.loading = false;
        this.error = 'Unable to submit sponsorship. Please try again.';
      }
    });
  }

  // Navigate back to campaign landing (fragment scroll)
  backToCampaign(): void {
    this.router.navigate(['/'], { fragment: 'featured-child' });
  }
}
