import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-sponsors',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-sponsors.component.html',
  styleUrl: './admin-sponsors.component.css'
})
export class AdminSponsorsComponent {
  private readonly baseUrl = environment.apiBaseUrl;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  isLoading = false;

  sponsorForm = {
    displayName: '',
    contactEmail: '',
    phone: ''
  };

  constructor(private http: HttpClient) {}

  clearMessages() {
    this.successMessage = null;
    this.errorMessage = null;
  }

  createSponsor() {
    this.clearMessages();
    this.isLoading = true;

    this.http.post(`${this.baseUrl}/admin/sponsors`, this.sponsorForm)
      .subscribe({
        next: () => {
          this.successMessage = 'Sponsor created successfully!';
          this.resetForm();
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to create sponsor';
          this.isLoading = false;
          console.error('Error:', err);
        }
      });
  }

  resetForm() {
    this.sponsorForm = {
      displayName: '',
      contactEmail: '',
      phone: ''
    };
  }

  validateEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}
