import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChildCardComponent, ChildCardView } from '../../components/child-card/child-card.component';
import { SponsorService } from '../../services/sponsor.service';

@Component({
  selector: 'app-children',
  standalone: true,
  imports: [CommonModule, ChildCardComponent],
  templateUrl: './children.component.html',
  styleUrl: './children.component.css'
})
export class ChildrenComponent implements OnInit {
  children: ChildCardView[] = [];
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
          age: index % 2 === 0 ? 8 : 10,
          grade: index % 2 === 0 ? 'Grade 3' : 'Grade 5',
          monthlyCost: '2,000 PKR',
          status: child.availabilityStatus
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
