import { Component, OnInit, AfterViewInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
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
export class OneChildAtATimeComponent implements OnInit, AfterViewInit, OnDestroy {
  children: ChildDto[] = [];
  eligibleChildren: ChildDto[] = [];
  currentChild: OneChildViewModel | null = null;
  loading = true;
  error: string | null = null;
  heroHeadline = "This Ramadan, Change One Child's Future.";
  heroSubtext = 'This child is out of school and needs support now.';
  animateCard = false;
  noAvailable = false;
  howItWorksVisible = false;

  private currentIndex = 0;
  private readonly featuredKey = 'jjt_featured_child';
  private howItWorksObserver?: IntersectionObserver;

  @ViewChild('howItWorksSection') howItWorksSection?: ElementRef<HTMLElement>;

  constructor(
    private sponsorService: SponsorService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Backend contract: cache featured child in sessionStorage to avoid flicker in the same session.
    const cached = sessionStorage.getItem(this.featuredKey);
    let cachedChild: ChildDto | null = null;
    if (cached) {
      try {
        cachedChild = JSON.parse(cached) as ChildDto;
      } catch {
        cachedChild = null;
      }
    }

    // Backend contract: GET /api/org/children (requires X-ROLE)
    this.sponsorService.getChildren().subscribe({
      next: (data) => {
        this.children = data;
        // Backend availability: only AVAILABLE children can be featured for sponsorship
        this.eligibleChildren = data.filter(child => child.availabilityStatus === 'AVAILABLE');
        if (this.eligibleChildren.length === 0) {
          this.noAvailable = true;
          this.currentChild = null;
          this.loading = false;
          return;
        }

        let selected = this.eligibleChildren[Math.floor(Math.random() * this.eligibleChildren.length)];
        if (cachedChild) {
          const found = this.eligibleChildren.find(child => child.id === cachedChild?.id);
          if (found) {
            selected = found;
          }
        }

        this.currentIndex = this.eligibleChildren.findIndex(child => child.id === selected.id);
        this.setCurrentChild(selected);
        sessionStorage.setItem(this.featuredKey, JSON.stringify(selected));
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Unable to load children right now.';
        this.loading = false;
        console.error('Error loading children:', err);
      }
    });
  }

  ngAfterViewInit(): void {
    if (!this.howItWorksSection) return;
    this.howItWorksObserver = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        if (entry?.isIntersecting) {
          this.howItWorksVisible = true;
          this.howItWorksObserver?.disconnect();
        }
      },
      { threshold: 0.2 }
    );
    this.howItWorksObserver.observe(this.howItWorksSection.nativeElement);
  }

  ngOnDestroy(): void {
    this.howItWorksObserver?.disconnect();
  }

  sponsorCurrent(): void {
    if (!this.currentChild) return;
    this.router.navigate(['/children', this.currentChild.id, 'sponsor']);
  }

  // Scroll target for "Learn How It Works" CTA
  scrollToHowItWorks(): void {
    document.getElementById('how-it-works')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
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
