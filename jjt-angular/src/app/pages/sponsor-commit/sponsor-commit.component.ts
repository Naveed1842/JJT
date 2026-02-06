import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SponsorService, CommitmentType, AvailabilityStatus } from '../../services/sponsor.service';

@Component({
  selector: 'app-sponsor-commit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './sponsor-commit.component.html'
})
export class SponsorCommitComponent implements OnInit {
  childId: string | null = null;
  childName = '';
  monthlyCost = '2,000 PKR';
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
      sponsor: {
        name: this.sponsorName,
        email: this.email,
        phone: this.phone || null
      }
    }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/sponsor/confirmation']);
      },
      error: () => {
        this.loading = false;
        this.error = 'Unable to submit sponsorship. Please try again.';
      }
    });
  }

}
