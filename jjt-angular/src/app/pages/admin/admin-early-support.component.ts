import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-early-support',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-early-support.component.html',
  styleUrl: './admin-early-support.component.css'
})
export class AdminEarlySupportComponent {
  private readonly baseUrl = environment.apiBaseUrl;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  isLoading = false;

  earlySupportForm = {
    childId: '',
    month: '',
    educationAmount: '120.00',
    educationCurrency: 'USD'
  };

  constructor(private http: HttpClient) {}

  clearMessages() {
    this.successMessage = null;
    this.errorMessage = null;
  }

  recordEarlySupport() {
    this.clearMessages();
    this.isLoading = true;

    const { childId, ...requestBody } = this.earlySupportForm;
    this.http.post(
      `${this.baseUrl}/admin/children/${childId}/early-support`,
      requestBody
    ).subscribe({
      next: () => {
        this.successMessage = 'Early support recorded successfully!';
        this.resetForm();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to record early support';
        this.isLoading = false;
        console.error('Error:', err);
      }
    });
  }

  resetForm() {
    this.earlySupportForm = {
      childId: '',
      month: '',
      educationAmount: '120.00',
      educationCurrency: 'USD'
    };
  }
}
