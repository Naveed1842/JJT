import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-progress',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-progress.component.html',
  styleUrl: './admin-progress.component.css'
})
export class AdminProgressComponent {
  private readonly baseUrl = environment.apiBaseUrl;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  isLoading = false;

  progressForm = {
    childId: '',
    month: '',
    summary: ''
  };

  constructor(private http: HttpClient) {}

  clearMessages() {
    this.successMessage = null;
    this.errorMessage = null;
  }

  addProgress() {
    this.clearMessages();
    this.isLoading = true;

    const { childId, ...requestBody } = this.progressForm;
    this.http.post(
      `${this.baseUrl}/admin/children/${childId}/progress`,
      requestBody
    ).subscribe({
      next: () => {
        this.successMessage = 'Progress update added successfully!';
        this.resetForm();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to add progress update';
        this.isLoading = false;
        console.error('Error:', err);
      }
    });
  }

  resetForm() {
    this.progressForm = {
      childId: '',
      month: '',
      summary: ''
    };
  }

  getCharacterCount(): number {
    return this.progressForm.summary.length;
  }

  getMaxCharacters(): number {
    return 500;
  }
}
