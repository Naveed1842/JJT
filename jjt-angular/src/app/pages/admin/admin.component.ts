import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent {
  activeForm: string = 'child';
  successMessage: string | null = null;
  errorMessage: string | null = null;

  // Create Child Form
  childForm = {
    fullName: '',
    educationAmount: '120.00',
    educationCurrency: 'USD'
  };

  // Create Sponsor Form
  sponsorForm = {
    displayName: '',
    contactEmail: ''
  };

  // Record Early Support Form
  earlySupportForm = {
    childId: '',
    month: '',
    educationAmount: '120.00',
    educationCurrency: 'USD'
  };

  // Add Progress Form
  progressForm = {
    childId: '',
    month: '',
    summary: ''
  };

  // Commit Sponsorship Form
  sponsorshipForm = {
    sponsorId: '',
    childId: '',
    startMonth: ''
  };

  constructor(private http: HttpClient) {}

  setActiveForm(form: string) {
    this.activeForm = form;
    this.clearMessages();
  }

  clearMessages() {
    this.successMessage = null;
    this.errorMessage = null;
  }

  createChild() {
    this.clearMessages();
    this.http.post('http://localhost:8080/api/admin/children', this.childForm)
      .subscribe({
        next: () => {
          this.successMessage = 'Child created successfully!';
          this.childForm = { fullName: '', educationAmount: '120.00', educationCurrency: 'USD' };
        },
        error: (err) => {
          this.errorMessage = 'Failed to create child';
          console.error('Error:', err);
        }
      });
  }

  createSponsor() {
    this.clearMessages();
    this.http.post('http://localhost:8080/api/admin/sponsors', this.sponsorForm)
      .subscribe({
        next: () => {
          this.successMessage = 'Sponsor created successfully!';
          this.sponsorForm = { displayName: '', contactEmail: '' };
        },
        error: (err) => {
          this.errorMessage = 'Failed to create sponsor';
          console.error('Error:', err);
        }
      });
  }

  recordEarlySupport() {
    this.clearMessages();
    const { childId, ...requestBody } = this.earlySupportForm;
    this.http.post(
      `http://localhost:8080/api/admin/children/${childId}/early-support`,
      requestBody
    ).subscribe({
      next: () => {
        this.successMessage = 'Early support recorded successfully!';
        this.earlySupportForm = { childId: '', month: '', educationAmount: '120.00', educationCurrency: 'USD' };
      },
      error: (err) => {
        this.errorMessage = 'Failed to record early support';
        console.error('Error:', err);
      }
    });
  }

  addProgress() {
    this.clearMessages();
    const { childId, ...requestBody } = this.progressForm;
    this.http.post(
      `http://localhost:8080/api/admin/children/${childId}/progress`,
      requestBody
    ).subscribe({
      next: () => {
        this.successMessage = 'Progress update added successfully!';
        this.progressForm = { childId: '', month: '', summary: '' };
      },
      error: (err) => {
        this.errorMessage = 'Failed to add progress update';
        console.error('Error:', err);
      }
    });
  }

  commitSponsorship() {
    this.clearMessages();
    this.http.post('http://localhost:8080/api/admin/sponsorships', this.sponsorshipForm)
      .subscribe({
        next: () => {
          this.successMessage = 'Sponsorship committed successfully!';
          this.sponsorshipForm = { sponsorId: '', childId: '', startMonth: '' };
        },
        error: (err) => {
          this.errorMessage = 'Failed to commit sponsorship';
          console.error('Error:', err);
        }
      });
  }
}
