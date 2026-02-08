import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AdminSponsorsService, AdminSponsor } from '../../services/admin-sponsors.service';

@Component({
  selector: 'app-admin-sponsors',
  standalone: true,
  imports: [CommonModule, TableModule, TagModule],
  templateUrl: './admin-sponsors.component.html',
  styleUrl: './admin-sponsors.component.css'
})
export class AdminSponsorsComponent {
  sponsors: AdminSponsor[] = [];

  constructor(private sponsorsService: AdminSponsorsService) {
    this.sponsorsService.getSponsors().subscribe({
      next: sponsors => this.sponsors = sponsors,
      error: err => console.error('Error loading sponsors', err)
    });
  }

  committedCount(sponsor: AdminSponsor): number {
    const list = sponsor.committedChildren ?? sponsor.children ?? [];
    return list.length;
  }
}
