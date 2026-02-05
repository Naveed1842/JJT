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
    name: '',
    birthDate: '',
    currentGrade: ''
  };

  // Create Sponsor Form
  sponsorForm = {
    name: '',
    email: '',
    phone: ''
  };

  // Record Early Support Form
  earlySupportForm = {
    childId: '',
    amountInCents: 0,
    description: '',
    supportDate: ''
  };

  // Add Progress Form
  progressForm = {
    childId: '',
    milestone: '',
    comments: ''
  };

  // Commit Sponsorship Form
  sponsorshipForm = {
    childId: '',
    sponsorId: '',
    startDate: ''
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
          this.childForm = { name: '', birthDate: '', currentGrade: '' };
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
          this.sponsorForm = { name: '', email: '', phone: '' };
        },
        error: (err) => {
          this.errorMessage = 'Failed to create sponsor';
          console.error('Error:', err);
        }
      });
  }

  recordEarlySupport() {
    this.clearMessages();
    this.http.post(
      `http://localhost:8080/api/admin/children/${this.earlySupportForm.childId}/early-support`,
      {
        amountInCents: this.earlySupportForm.amountInCents,
        description: this.earlySupportForm.description,
        supportDate: this.earlySupportForm.supportDate
      }
    ).subscribe({
      next: () => {
        this.successMessage = 'Early support recorded successfully!';
        this.earlySupportForm = { childId: '', amountInCents: 0, description: '', supportDate: '' };
      },
      error: (err) => {
        this.errorMessage = 'Failed to record early support';
        console.error('Error:', err);
      }
    });
  }

  addProgress() {
    this.clearMessages();
    this.http.post(
      `http://localhost:8080/api/admin/children/${this.progressForm.childId}/progress`,
      {
        milestone: this.progressForm.milestone,
        comments: this.progressForm.comments
      }
    ).subscribe({
      next: () => {
        this.successMessage = 'Progress update added successfully!';
        this.progressForm = { childId: '', milestone: '', comments: '' };
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
          this.sponsorshipForm = { childId: '', sponsorId: '', startDate: '' };
        },
        error: (err) => {
          this.errorMessage = 'Failed to commit sponsorship';
          console.error('Error:', err);
        }
      });
  }
}
