import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SponsorService, CommitmentType, AvailabilityStatus } from '../../services/sponsor.service';

@Component({
  selector: 'app-sponsor-commit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './sponsor-commit.component.html',
})
export class SponsorCommitComponent implements OnInit {
  // Child data
  childId: string | null = null;
  childName = '';
  monthlyCost = '';
  campusName = '';
  city = '';
  supportStatus: AvailabilityStatus = 'AVAILABLE';

  // Step (1=Intention, 2=Plan, 3=Details, 4=Payment, 5=Confirmed)
  step: 1 | 2 | 3 | 4 | 5 = 1;

  readonly stepDefs = [
    { n: 1, label: 'Intention' },
    { n: 2, label: 'Plan' },
    { n: 3, label: 'Account' },
    { n: 4, label: 'Payment' },
  ];

  // Form
  intention: 'SADAQAH' | 'ZAKAT' | 'GENERAL' = 'SADAQAH';
  commitmentType: CommitmentType = 'MONTHLY';
  sponsorName = '';
  email = '';
  phone = '';

  // Bank info
  readonly paymentInfo = {
    accountTitle: 'JUNIOR JINNAH TRUST',
    accountNumber: '2000848908',
    iban: 'PK27SAMB0000002000848908',
    bankName: 'SAMBA BANK LIMITED'
  };
  copiedField: string | null = null;

  // State
  pageLoading = true;
  submitting = false;
  error: string | null = null;
  startMonth: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private sponsorService: SponsorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.childId = this.route.snapshot.paramMap.get('childId');
    if (this.childId) {
      this.sponsorService.getChild(this.childId).subscribe({
        next: (child) => {
          this.childName   = child.fullName;
          this.supportStatus = child.availabilityStatus;
          this.monthlyCost = `${child.educationCurrency} ${child.educationAmount}`;
          this.campusName  = child.campusName;
          this.city        = child.city;
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

  get yearlyPrice(): string {
    const match = this.monthlyCost.match(/[\d,]+\.?\d*/);
    if (!match) return this.monthlyCost;
    const numeric = parseFloat(match[0].replace(/,/g, ''));
    const yearly = Math.round(numeric * 12 * 0.9);
    const currency = this.monthlyCost.replace(match[0], '').trim();
    return `${currency} ${yearly.toLocaleString()}`;
  }

  get displayPrice(): string {
    return this.commitmentType === 'YEARLY' ? this.yearlyPrice : this.monthlyCost;
  }

  private emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  get emailValid(): boolean {
    return this.emailRegex.test(this.email.trim());
  }

  get stepValid(): boolean {
    if (this.step === 1) return true;
    if (this.step === 2) return true;
    if (this.step === 3) return !!this.sponsorName.trim() && this.emailValid;
    return true;
  }

  nextStep(): void {
    if (!this.stepValid) {
      if (this.step === 3 && !!this.sponsorName.trim() && !this.emailValid) {
        this.error = 'Please enter a valid email address.';
      } else {
        this.error = 'Please fill in all required fields.';
      }
      return;
    }
    this.error = null;
    if (this.step === 4) { this.submit(); return; }
    this.step = (this.step + 1) as 1 | 2 | 3 | 4 | 5;
  }

  prevStep(): void {
    if (this.step > 1) this.step = (this.step - 1) as 1 | 2 | 3 | 4 | 5;
  }

  submit(): void {
    if (this.submitting) return;
    if (this.supportStatus !== 'AVAILABLE') {
      this.error = 'This child is not available for new sponsorships.';
      return;
    }
    if (!this.childId) return;

    this.submitting = true;
    this.error = null;

    this.sponsorService.commitSponsorship({
      childId: this.childId,
      commitmentType: this.commitmentType,
      sponsor: { name: this.sponsorName, email: this.email, phone: this.phone || null }
    }).subscribe({
      next: (res) => {
        this.submitting = false;
        this.startMonth = res.startMonth;
        this.step = 5;
      },
      error: (err) => {
        this.submitting = false;
        this.error = err?.error?.message ?? 'Unable to submit sponsorship. Please try again.';
      }
    });
  }

  intentionLabel(): string {
    switch (this.intention) {
      case 'SADAQAH': return 'Sadaqah';
      case 'ZAKAT':   return 'Zakat';
      case 'GENERAL': return 'General';
    }
  }

  async copy(value: string, field: string): Promise<void> {
    try {
      await navigator.clipboard.writeText(value);
      this.copiedField = field;
      setTimeout(() => (this.copiedField = null), 2000);
    } catch { /* noop */ }
  }
}
