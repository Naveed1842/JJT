import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface ChildDetail {
  childId: string;
  name: string;
  birthDate: string;
  currentGrade: string;
  sponsorshipStatus: string;
}

interface LedgerEntry {
  monthYear: string;
  totalInCents: number;
  description: string;
}

interface ProgressUpdate {
  reportedAt: string;
  milestone: string;
  comments: string;
}

@Component({
  selector: 'app-child-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './child-detail.component.html',
  styleUrl: './child-detail.component.css'
})
export class ChildDetailComponent implements OnInit {
  childId: string | null = null;
  child: ChildDetail | null = null;
  ledgerEntries: LedgerEntry[] = [];
  progressUpdates: ProgressUpdate[] = [];
  loading = true;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.childId = this.route.snapshot.paramMap.get('id');
    if (this.childId) {
      this.loadChildData();
    }
  }

  loadChildData() {
    const baseUrl = 'http://localhost:8080/api/org';
    
    this.http.get<ChildDetail>(`${baseUrl}/children/${this.childId}`)
      .subscribe({
        next: (data) => {
          this.child = data;
          this.loadLedger();
          this.loadProgress();
        },
        error: (err) => {
          this.error = 'Failed to load child details';
          this.loading = false;
          console.error('Error loading child:', err);
        }
      });
  }

  loadLedger() {
    this.http.get<LedgerEntry[]>(`http://localhost:8080/api/org/children/${this.childId}/ledger`)
      .subscribe({
        next: (data) => this.ledgerEntries = data,
        error: (err) => console.error('Error loading ledger:', err)
      });
  }

  loadProgress() {
    this.http.get<ProgressUpdate[]>(`http://localhost:8080/api/org/children/${this.childId}/progress`)
      .subscribe({
        next: (data) => {
          this.progressUpdates = data;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error loading progress:', err);
          this.loading = false;
        }
      });
  }

  formatCurrency(cents: number): string {
    return `$${(cents / 100).toFixed(2)}`;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }
}
