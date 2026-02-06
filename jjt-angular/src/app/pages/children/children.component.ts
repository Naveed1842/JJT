import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChildSponsorCardComponent, ChildCardModel } from '../../components/child-sponsor-card.component';
import { SponsorService } from '../../services/sponsor.service';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';

@Component({
  selector: 'app-children',
  standalone: true,
  imports: [CommonModule, ChildSponsorCardComponent, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './children.component.html',
  styleUrl: './children.component.css'
})
export class ChildrenComponent implements OnInit {
  children: ChildCardModel[] = [];
  loading = true;
  error: string | null = null;

  constructor(private sponsorService: SponsorService) {}

  ngOnInit() {
    this.loadChildren();
  }

  loadChildren() {
    this.sponsorService.getChildren().subscribe({
      next: (data) => {
        this.children = data.map((child, index) => ({
          id: child.id,
          name: child.fullName,
          age: 0,
          grade: '',
          monthlyCost: parseFloat(child.educationAmount),
          currency: child.educationCurrency,
          status: child.availabilityStatus === 'AVAILABLE' ? 'AVAILABLE' : 'ALLOCATED'
        }));
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load children.';
        this.loading = false;
        console.error('Error loading children:', err);
      }
    });
  }
}
