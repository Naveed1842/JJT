import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';
import { PaymentInfoCardComponent } from '../../components/payment-info-card/payment-info-card.component';

@Component({
  selector: 'app-sponsor-confirmation',
  standalone: true,
  imports: [CommonModule, RouterLink, SiteHeaderComponent, SiteFooterComponent, PaymentInfoCardComponent],
  templateUrl: './sponsor-confirmation.component.html'
})
export class SponsorConfirmationComponent implements OnInit {
  data: any = null;

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    const stored = sessionStorage.getItem('sponsorshipConfirmation');
    if (stored) {
      this.data = JSON.parse(stored);
      const childIdFromRoute = this.route.snapshot.paramMap.get('childId');
      if (childIdFromRoute && this.data.childId !== childIdFromRoute) {
        this.data = null;
      }
    }
    if (!this.data) {
      // No confirmation data — return user to children list.
      this.router.navigate(['/children']);
    }
  }
}
