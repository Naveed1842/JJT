import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-sponsorships',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-sponsorships.component.html',
  styleUrl: './admin-sponsorships.component.css'
})
export class AdminSponsorshipsComponent implements OnInit {
  private readonly baseUrl = environment.apiBaseUrl;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  isLoading = false;
  isLoadingData = true;
  activeTab: 'create' | 'pending' | 'active' = 'pending';

  sponsorshipForm = {
    sponsorId: '',
    childId: '',
    startMonth: '',
    commitmentType: 'MONTHLY'
  };

  pendingSponsorships: any[] = [];
  activeSponsorships: any[] = [];

  constructor(private http: HttpClient, private route: ActivatedRoute, private router: Router) {}

  ngOnInit() {
    this.route.queryParamMap.subscribe(params => {
      const tab = params.get('tab') as 'create' | 'pending' | 'active' | null;
      if (tab) {
        this.activeTab = tab;
      }
      if (this.activeTab === 'pending') {
        this.loadPending();
      }
      if (this.activeTab === 'active') {
        this.loadActive();
      }
    });
  }

  clearMessages() {
    this.successMessage = null;
    this.errorMessage = null;
  }

  setActiveTab(tab: 'create' | 'pending' | 'active') {
    this.activeTab = tab;
    this.clearMessages();
    this.router.navigate([], { relativeTo: this.route, queryParams: { tab } });

    if (tab === 'pending') {
      this.loadPending();
    } else if (tab === 'active') {
      this.loadActive();
    }
  }

  commitSponsorship() {
    this.clearMessages();
    this.isLoading = true;

    this.http.post(`${this.baseUrl}/admin/sponsorships`, this.sponsorshipForm)
      .subscribe({
        next: () => {
          this.successMessage = 'Sponsorship committed successfully!';
          this.sponsorshipForm = {
            sponsorId: '',
            childId: '',
            startMonth: '',
            commitmentType: 'MONTHLY'
          };
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to commit sponsorship';
          this.isLoading = false;
          console.error('Error:', err);
        }
      });
  }

  loadPending() {
    this.isLoadingData = true;
    this.http.get<any[]>(`${this.baseUrl}/admin/sponsorships?status=PENDING`)
      .subscribe({
        next: (data) => {
          this.pendingSponsorships = data;
          this.isLoadingData = false;
        },
        error: (err) => {
          console.error('Error loading pending sponsorships', err);
          this.isLoadingData = false;
        }
      });
  }

  loadActive() {
    this.isLoadingData = true;
    this.http.get<any[]>(`${this.baseUrl}/admin/sponsorships?status=ACTIVE`)
      .subscribe({
        next: (data) => {
          this.activeSponsorships = data;
          this.isLoadingData = false;
        },
        error: (err) => {
          console.error('Error loading active sponsorships', err);
          this.isLoadingData = false;
        }
      });
  }

  activate(id: string) {
    this.clearMessages();
    this.http.post(`${this.baseUrl}/admin/sponsorships/${id}/activate`, {})
      .subscribe({
        next: () => {
          this.successMessage = 'Sponsorship activated successfully!';
          this.loadPending();
          if (this.activeTab === 'active') {
            this.loadActive();
          }
        },
        error: (err) => {
          this.errorMessage = 'Failed to activate sponsorship';
          console.error('Error activating sponsorship', err);
        }
      });
  }

  expire(id: string) {
    this.clearMessages();
    this.http.post(`${this.baseUrl}/admin/sponsorships/${id}/expire`, {})
      .subscribe({
        next: () => {
          this.successMessage = 'Sponsorship expired successfully!';
          this.loadPending();
          if (this.activeTab === 'active') {
            this.loadActive();
          }
        },
        error: (err) => {
          this.errorMessage = 'Failed to expire sponsorship';
          console.error('Error expiring sponsorship', err);
        }
      });
  }

  resetForm() {
    this.sponsorshipForm = {
      sponsorId: '',
      childId: '',
      startMonth: '',
      commitmentType: 'MONTHLY'
    };
  }
}
