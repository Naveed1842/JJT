import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SponsorService, CommitmentType, AvailabilityStatus } from '../../services/sponsor.service';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';

@Component({
  selector: 'app-sponsor-commit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './sponsor-commit.component.html'
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
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sponsorService: SponsorService
  ) {}

  ngOnInit(): void {
    this.childId = this.route.snapshot.paramMap.get('childId');
    if (this.childId) {
      this.sponsorService.getChild(this.childId).subscribe({
        next: (child) => {
          this.childName = child.fullName;
          this.supportStatus = child.availabilityStatus;
          this.monthlyCost = `${child.educationAmount} ${child.educationCurrency}`;
          this.campusName = child.campusName;
          this.city = child.city;
        },
        error: () => {
          this.error = 'Failed to load child details.';
        }
      });
    }
  }

  submit(): void {
    this.error = null;
    if (this.supportStatus === 'ALLOCATED') {
      this.error = 'This child already has an active sponsorship.';
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
    this.sponsorService.commitSponsorship({
      childId: this.childId,
      commitmentType: this.commitmentType,
      sponsor: {
        name: this.sponsorName,
        email: this.email,
        phone: this.phone || null
      }
    }).subscribe({
      next: (res) => {
        this.loading = false;
        const confirmationPayload = {
          childId: this.childId,
          childName: this.childName,
          sponsorName: this.sponsorName,
          email: this.email,
          phone: this.phone,
          commitmentType: this.commitmentType,
          startMonth: res?.startMonth
        };
        sessionStorage.setItem('sponsorshipConfirmation', JSON.stringify(confirmationPayload));
        this.router.navigate([`/children/${this.childId}/sponsor/confirmation`]);
      },
      error: () => {
        this.loading = false;
        this.error = 'Unable to submit sponsorship. Please try again.';
      }
    });
  }

}
