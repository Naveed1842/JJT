import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { AdminChildrenService } from '../../services/admin-children.service';
import { ChildDto } from '../../services/sponsor.service';

type AdminChildRow = ChildDto & {
  sponsorName?: string | null;
  lastProgressMonth?: string | null;
};

@Component({
  selector: 'app-admin-children',
  standalone: true,
  imports: [CommonModule, FormsModule, TableModule, TagModule],
  templateUrl: './admin-children.component.html',
  styleUrl: './admin-children.component.css'
})
export class AdminChildrenComponent {
  children: AdminChildRow[] = [];
  searchTerm = '';

  constructor(private childrenService: AdminChildrenService) {
    this.childrenService.getChildren().subscribe({
      next: children => this.children = children.map(child => ({ ...child })),
      error: err => console.error('Error loading children', err)
    });
  }

  get filteredChildren() {
    if (!this.searchTerm) {
      return this.children;
    }
    const term = this.searchTerm.toLowerCase();
    return this.children.filter(child =>
      child.fullName.toLowerCase().includes(term) ||
      child.city.toLowerCase().includes(term) ||
      child.campusName.toLowerCase().includes(term)
    );
  }

  statusSeverity(status: string): 'success' | 'warning' | 'danger' | 'info' {
    if (status === 'ENROLLED' || status === 'ACTIVE') return 'success';
    if (status === 'PENDING') return 'warning';
    if (status === 'SUPPORT_PAUSED' || status === 'PAUSED') return 'danger';
    return 'info';
  }
}
