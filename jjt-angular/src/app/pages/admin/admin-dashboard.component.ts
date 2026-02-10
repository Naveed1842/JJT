import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface DashboardStats {
  totalChildren: number;
  totalSponsors: number;
  activeSponsorships: number;
  pendingSponsorships: number;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  private readonly baseUrl = environment.apiBaseUrl;
  stats: DashboardStats = {
    totalChildren: 0,
    totalSponsors: 0,
    activeSponsorships: 0,
    pendingSponsorships: 0
  };

  recentActivities: any[] = [];
  isLoading = true;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadDashboardStats();
  }

  loadDashboardStats() {
    this.isLoading = true;

    // Load pending sponsorships
    this.http.get<any[]>(`${this.baseUrl}/admin/sponsorships?status=PENDING`)
      .subscribe({
        next: (data) => {
          this.stats.pendingSponsorships = data.length;
        },
        error: (err) => console.error('Error loading pending sponsorships', err)
      });

    // Load active sponsorships
    this.http.get<any[]>(`${this.baseUrl}/admin/sponsorships?status=ACTIVE`)
      .subscribe({
        next: (data) => {
          this.stats.activeSponsorships = data.length;
        },
        error: (err) => console.error('Error loading active sponsorships', err)
      });

    // Simulate loading recent activities
    setTimeout(() => {
      this.isLoading = false;
    }, 500);
  }
}
