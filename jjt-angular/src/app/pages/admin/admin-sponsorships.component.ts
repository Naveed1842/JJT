import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AdminSponsorshipsService, AdminSponsorship } from '../../services/admin-sponsorships.service';
import { CommitmentType } from '../../services/sponsor.service';

@Component({
  selector: 'app-admin-sponsorships',
  standalone: true,
  imports: [CommonModule, FormsModule, TableModule, TagModule],
  templateUrl: './admin-sponsorships.component.html',
  styleUrl: './admin-sponsorships.component.css'
})
export class AdminSponsorshipsComponent {
  sponsorships: AdminSponsorship[] = [];
  statusFilter: string | 'ALL' = 'ALL';

  constructor(private sponsorshipsService: AdminSponsorshipsService) {
    this.sponsorshipsService.getSponsorships().subscribe({
      next: data => this.sponsorships = data,
      error: err => console.error('Error loading sponsorships', err)
    });
  }

  get filtered() {
    if (this.statusFilter === 'ALL') return this.sponsorships;
    return this.sponsorships.filter(s => s.status === this.statusFilter);
  }

  statusSeverity(status: string | undefined): 'success' | 'warning' | 'danger' {
    if (status === 'ACTIVE') return 'success';
    if (status === 'PENDING') return 'warning';
    return 'danger';
  }

  labelCommitment(type?: CommitmentType) {
    return type ?? '—';
  }
}
