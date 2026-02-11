import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SponsorService, ChildDto } from '../../services/sponsor.service';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';
import { RamadanLoaderComponent } from '../../components/ramadan-loader/ramadan-loader.component';
import { HeroBannerComponent } from '../../components/one-child/hero-banner.component';
import { SingleChildFocusCardComponent } from '../../components/one-child/single-child-focus-card.component';
import { ChildDecisionActionsComponent } from '../../components/one-child/child-decision-actions.component';
import { TrustSignalsComponent } from '../../components/one-child/trust-signals.component';
import { OneChildViewModel } from './one-child-at-a-time.models';

@Component({
  selector: 'app-one-child-at-a-time',
  standalone: true,
  imports: [
    CommonModule,
    SiteHeaderComponent,
    SiteFooterComponent,
    RamadanLoaderComponent,
    HeroBannerComponent,
    SingleChildFocusCardComponent,
    ChildDecisionActionsComponent,
    TrustSignalsComponent
  ],
  templateUrl: './one-child-at-a-time.component.html'
})
export class OneChildAtATimeComponent implements OnInit {
  children: ChildDto[] = [];
  eligibleChildren: ChildDto[] = [];
  currentChild: OneChildViewModel | null = null;
  loading = true;
  error: string | null = null;
  heroHeadline = "This Ramadan, Change One Child's Future.";
  heroSubtext = 'This child is out of school and needs support now.';
  animateCard = false;

  private currentIndex = 0;

  constructor(
    private sponsorService: SponsorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.sponsorService.getChildren().subscribe({
      next: (data) => {
        this.children = data;
        this.eligibleChildren = data.filter(child => child.availabilityStatus === 'AVAILABLE');
        if (this.eligibleChildren.length === 0) {
          this.eligibleChildren = data;
          this.heroSubtext = 'This child needs support to stay in school.';
        }
        this.currentIndex = 0;
        this.setCurrentChild(this.eligibleChildren[this.currentIndex]);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Unable to load children right now.';
        this.loading = false;
        console.error('Error loading children:', err);
      }
    });
  }

  sponsorCurrent(): void {
    if (!this.currentChild) return;
    this.router.navigate(['/children', this.currentChild.id, 'sponsor']);
  }

  showAnother(): void {
    if (this.eligibleChildren.length <= 1) return;
    this.currentIndex = (this.currentIndex + 1) % this.eligibleChildren.length;
    this.animateCard = false;
    const next = this.eligibleChildren[this.currentIndex];
    setTimeout(() => {
      this.setCurrentChild(next);
    }, 80);
  }

  private setCurrentChild(child: ChildDto | undefined): void {
    if (!child) return;
    this.currentChild = this.mapChild(child);
    this.animateCard = true;
    setTimeout(() => {
      this.animateCard = false;
    }, 500);
  }

  private mapChild(child: ChildDto): OneChildViewModel {
    const dailyCost = this.formatDailyCost(child.educationAmount, child.educationCurrency);
    return {
      id: child.id,
      name: child.fullName,
      ageText: 'Age -',
      city: child.city,
      tags: this.tagsForStatus(child.availabilityStatus),
      monthlyCost: `${child.educationCurrency} ${child.educationAmount} / month`,
      dailyCost,
      storyLine: this.storyForStatus(child.availabilityStatus),
      ramadanDonors: this.donorsForStatus(child.availabilityStatus),
      trustNote: 'Trusted support for 2 months',
      coveragePercent: this.coverageForStatus(child.availabilityStatus),
      status: child.availabilityStatus
    };
  }

  private tagsForStatus(status: ChildDto['availabilityStatus']): string[] {
    switch (status) {
      case 'AVAILABLE':
        return ['Needs sponsor', 'Education at risk', 'Priority'];
      case 'RESERVED':
        return ['Pending support', 'Awaiting confirmation'];
      case 'ALLOCATED':
        return ['In school', 'Sponsored'];
      default:
        return ['Needs support'];
    }
  }

  private coverageForStatus(status: ChildDto['availabilityStatus']): number {
    switch (status) {
      case 'ALLOCATED':
        return 100;
      case 'RESERVED':
        return 60;
      default:
        return 20;
    }
  }

  private donorsForStatus(status: ChildDto['availabilityStatus']): number {
    switch (status) {
      case 'ALLOCATED':
        return 6;
      case 'RESERVED':
        return 3;
      default:
        return 1;
    }
  }

  private storyForStatus(status: ChildDto['availabilityStatus']): string {
    switch (status) {
      case 'ALLOCATED':
        return 'Back in school and staying on track this year.';
      case 'RESERVED':
        return 'Support is pending; needs confirmation to continue.';
      default:
        return 'Dreams of becoming a teacher.';
    }
  }

  private formatDailyCost(amount: string, currency: string): string {
    const numeric = Number(amount.replace(/[^0-9.]/g, ''));
    if (!Number.isFinite(numeric) || numeric <= 0) {
      return '';
    }
    const perDay = Math.round(numeric / 30);
    return `Just ${currency} ${perDay} per day in Ramadan`;
  }
}
