import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface ChildDetail {
  id: string;
  fullName: string;
  educationAmount: string;
  educationCurrency: string;
}

interface LedgerEntry {
  id: string;
  month: string;
  educationAmount: string;
  educationCurrency: string;
}

interface Ledger {
  childId: string;
  entries: LedgerEntry[];
}

interface ProgressUpdate {
  id: string;
  month: string;
  summary: string;
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
    this.http.get<Ledger>(`http://localhost:8080/api/org/children/${this.childId}/ledger`)
      .subscribe({
        next: (data) => this.ledgerEntries = data.entries || [],
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
}
