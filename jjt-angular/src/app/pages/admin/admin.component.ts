import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent {
  activeForm: string = 'child';
  successMessage: string | null = null;
  errorMessage: string | null = null;

  // Create Child Form
  childForm = {
    rollNumber: '',
    fullName: '',
    city: '',
    campusName: '',
    schoolName: '',
    educationAmount: '2000.00',
    educationCurrency: 'PKR'
  };

  // Create Sponsor Form
  sponsorForm = {
    displayName: '',
    contactEmail: '',
    phone: ''
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
    startMonth: '',
    commitmentType: 'MONTHLY'
  };

  pendingSponsorships: any[] = [];
  activeSponsorships: any[] = [];

  constructor(private http: HttpClient) {}

  setActiveForm(form: string) {
    this.activeForm = form;
    this.clearMessages();
    if (form === 'pending') {
      this.loadPending();
    }
    if (form === 'active') {
      this.loadActive();
    }
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
          this.childForm = { rollNumber: '', fullName: '', city: '', campusName: '', schoolName: '', educationAmount: '2000.00', educationCurrency: 'PKR' };
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
          this.sponsorForm = { displayName: '', contactEmail: '', phone: '' };
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
          this.sponsorshipForm = { sponsorId: '', childId: '', startMonth: '', commitmentType: 'MONTHLY' };
        },
        error: (err) => {
          this.errorMessage = 'Failed to commit sponsorship';
          console.error('Error:', err);
        }
      });
  }

  loadPending() {
    this.http.get<any[]>('http://localhost:8080/api/admin/sponsorships?status=PENDING')
      .subscribe({
        next: (data) => this.pendingSponsorships = data,
        error: (err) => {
          console.error('Error loading pending sponsorships', err);
        }
      });
  }

  loadActive() {
    this.http.get<any[]>('http://localhost:8080/api/admin/sponsorships?status=ACTIVE')
      .subscribe({
        next: (data) => this.activeSponsorships = data,
        error: (err) => {
          console.error('Error loading active sponsorships', err);
        }
      });
  }

  activate(id: string) {
    this.http.post(`http://localhost:8080/api/admin/sponsorships/${id}/activate`, {})
      .subscribe({
        next: () => {
          this.successMessage = 'Sponsorship activated.';
          this.loadPending();
          if (this.activeForm === 'active') {
            this.loadActive();
          }
        },
        error: (err) => console.error('Error activating sponsorship', err)
      });
  }

  expire(id: string) {
    this.http.post(`http://localhost:8080/api/admin/sponsorships/${id}/expire`, {})
      .subscribe({
        next: () => {
          this.successMessage = 'Sponsorship expired.';
          this.loadPending();
          if (this.activeForm === 'active') {
            this.loadActive();
          }
        },
        error: (err) => console.error('Error expiring sponsorship', err)
      });
  }
}
