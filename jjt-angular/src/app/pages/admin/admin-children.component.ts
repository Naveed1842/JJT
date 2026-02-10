import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-children',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-children.component.html',
  styleUrl: './admin-children.component.css'
})
export class AdminChildrenComponent {
  private readonly baseUrl = environment.apiBaseUrl;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  isLoading = false;

  childForm = {
    rollNumber: '',
    fullName: '',
    city: '',
    campusName: '',
    schoolName: '',
    educationAmount: '2000.00',
    educationCurrency: 'PKR'
  };

  constructor(private http: HttpClient) {}

  clearMessages() {
    this.successMessage = null;
    this.errorMessage = null;
  }

  createChild() {
    this.clearMessages();
    this.isLoading = true;

    this.http.post(`${this.baseUrl}/admin/children`, this.childForm)
      .subscribe({
        next: () => {
          this.successMessage = 'Child created successfully!';
          this.resetForm();
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Failed to create child';
          this.isLoading = false;
          console.error('Error:', err);
        }
      });
  }

  resetForm() {
    this.childForm = {
      rollNumber: '',
      fullName: '',
      city: '',
      campusName: '',
      schoolName: '',
      educationAmount: '2000.00',
      educationCurrency: 'PKR'
    };
  }
}
