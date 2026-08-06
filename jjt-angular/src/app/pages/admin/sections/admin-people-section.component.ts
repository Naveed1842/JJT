import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import {
  CreatePersonRequest,
  PayrollProfileResponse,
  PersonKind,
  PersonResponse,
  SalaryType,
  UpsertPayrollProfileRequest,
} from '../../../services/api.models';

@Component({
  selector: 'app-admin-people-section',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrl: '../admin.component.css',
  template: `
<div class="section-card">
  <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;">
    <h2 class="section-title">People</h2>
    <button class="btn-primary" (click)="showForm=!showForm">+ Add Person</button>
  </div>

  <div *ngIf="showForm" class="inline-form" style="margin-bottom:20px;">
    <div class="form-row">
      <input class="form-input" [(ngModel)]="form.firstName" placeholder="First name *" />
      <input class="form-input" [(ngModel)]="form.lastName" placeholder="Last name *" />
      <select class="form-select" [(ngModel)]="form.kind">
        <option value="STAFF">Staff</option>
        <option value="CONTRACTOR">Contractor</option>
        <option value="VOLUNTEER">Volunteer</option>
      </select>
    </div>
    <div class="form-row">
      <input class="form-input" [(ngModel)]="form.email" placeholder="Email" />
      <input class="form-input" [(ngModel)]="form.phone" placeholder="Phone" />
      <input class="form-input" [(ngModel)]="form.nationalId" placeholder="National ID" />
    </div>
    <div class="form-actions">
      <button class="btn-primary" (click)="createPerson()" [disabled]="saving">Save</button>
      <button class="btn-ghost" (click)="showForm=false">Cancel</button>
    </div>
    <div *ngIf="errorMsg" class="error-msg">{{errorMsg}}</div>
  </div>

  <!-- Payroll profile panel -->
  <div *ngIf="selectedPerson" class="inline-form" style="margin-bottom:20px;border-color:#2f5d4f;">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
      <strong style="font-size:14px;color:#1c352c;">Payroll Profile — {{selectedPerson.firstName}} {{selectedPerson.lastName}}</strong>
      <button class="btn-ghost" (click)="selectedPerson=null;profile=null">Close</button>
    </div>
    <div *ngIf="profile">
      <div class="kv-row"><span>Salary Type</span><span>{{profile.salaryType}}</span></div>
      <div class="kv-row"><span>Base Amount</span><span>{{profile.currency}} {{profile.baseAmount}}</span></div>
      <div class="kv-row"><span>Effective From</span><span>{{profile.effectiveFrom}}</span></div>
      <div class="kv-row"><span>Active</span><span>{{profile.active}}</span></div>
    </div>
    <div *ngIf="!profile" style="margin-bottom:12px;font-size:13px;color:#8a958d;">No payroll profile yet.</div>
    <h4 style="font-size:13px;font-weight:600;margin-bottom:10px;">{{profile ? 'Update' : 'Create'}} Profile</h4>
    <div class="form-row">
      <select class="form-select" [(ngModel)]="profileForm.salaryType">
        <option value="MONTHLY_FIXED">Monthly Fixed</option>
        <option value="HOURLY">Hourly</option>
        <option value="PRO_RATA">Pro Rata</option>
      </select>
      <input class="form-input" [(ngModel)]="profileForm.baseAmount" placeholder="Base amount" type="number" />
      <input class="form-input" [(ngModel)]="profileForm.currency" placeholder="Currency" style="max-width:90px;" />
    </div>
    <div class="form-row">
      <label style="font-size:13px;color:#54625b;">From</label>
      <input class="form-input" type="date" [(ngModel)]="profileForm.effectiveFrom" />
      <input class="form-input" [(ngModel)]="profileForm.bankAccountRef" placeholder="Bank account ref" />
    </div>
    <div class="form-actions">
      <button class="btn-primary" (click)="savePayrollProfile()" [disabled]="saving">Save Profile</button>
    </div>
    <div *ngIf="errorMsg" class="error-msg">{{errorMsg}}</div>
  </div>

  <div *ngIf="loading" class="loading-row">Loading people…</div>
  <div *ngIf="!loading && people.length===0" class="empty-state">No people records yet.</div>
  <div *ngIf="!loading && people.length>0" class="data-table-wrap">
    <table class="data-table">
      <thead><tr><th>Name</th><th>Kind</th><th>Email</th><th>Phone</th><th>Status</th><th></th></tr></thead>
      <tbody>
        <tr *ngFor="let p of people">
          <td>{{p.firstName}} {{p.lastName}}</td>
          <td><span class="badge badge-grey">{{p.kind}}</span></td>
          <td>{{p.email ?? '—'}}</td>
          <td>{{p.phone ?? '—'}}</td>
          <td><span class="badge" [ngClass]="p.active?'badge-green':'badge-grey'">{{p.active?'Active':'Inactive'}}</span></td>
          <td><button class="btn-xs" (click)="openPayrollProfile(p)">Payroll</button></td>
        </tr>
      </tbody>
    </table>
  </div>
</div>
  `,
})
export class AdminPeopleSectionComponent implements OnInit {
  private readonly svc = inject(AdminService);

  people: PersonResponse[] = [];
  loading = false;
  showForm = false;
  saving = false;
  errorMsg: string | null = null;

  form: CreatePersonRequest & { nationalId?: string } = {
    kind: 'STAFF', firstName: '', lastName: '', email: '', phone: '', nationalId: ''
  };

  selectedPerson: PersonResponse | null = null;
  profile: PayrollProfileResponse | null = null;
  profileForm: UpsertPayrollProfileRequest = {
    salaryType: 'MONTHLY_FIXED', baseAmount: '', currency: 'PKR',
    effectiveFrom: new Date().toISOString().split('T')[0], bankAccountRef: ''
  };

  readonly kinds: PersonKind[] = ['STAFF', 'CONTRACTOR', 'VOLUNTEER'];
  readonly salaryTypes: SalaryType[] = ['MONTHLY_FIXED', 'HOURLY', 'PRO_RATA'];

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.svc.listPeople().subscribe({
      next: (d) => { this.people = d; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  createPerson(): void {
    if (this.saving || !this.form.firstName.trim() || !this.form.lastName.trim()) {
      this.errorMsg = 'First name and last name are required.';
      return;
    }
    this.saving = true;
    this.errorMsg = null;
    this.svc.createPerson({
      kind: this.form.kind,
      firstName: this.form.firstName.trim(),
      lastName: this.form.lastName.trim(),
      email: this.form.email?.trim() || null,
      phone: this.form.phone?.trim() || null,
      nationalId: (this.form as any).nationalId?.trim() || null,
    }).subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.form = { kind: 'STAFF', firstName: '', lastName: '', email: '', phone: '', nationalId: '' };
        this.load();
      },
      error: (e) => { this.saving = false; this.errorMsg = e?.error?.message ?? 'Failed to create person.'; }
    });
  }

  openPayrollProfile(p: PersonResponse): void {
    this.selectedPerson = p;
    this.profile = null;
    this.errorMsg = null;
    this.svc.getPersonPayrollProfile(p.id).subscribe({
      next: (pr) => {
        this.profile = pr;
        this.profileForm = {
          salaryType: pr.salaryType,
          baseAmount: pr.baseAmount,
          currency: pr.currency,
          effectiveFrom: pr.effectiveFrom,
          bankAccountRef: pr.bankAccountRef ?? ''
        };
      },
      error: () => {}
    });
  }

  savePayrollProfile(): void {
    if (!this.selectedPerson || this.saving || !this.profileForm.baseAmount) {
      this.errorMsg = 'Base amount is required.';
      return;
    }
    this.saving = true;
    this.errorMsg = null;
    this.svc.upsertPayrollProfile(this.selectedPerson.id, {
      salaryType: this.profileForm.salaryType,
      baseAmount: this.profileForm.baseAmount,
      currency: this.profileForm.currency,
      effectiveFrom: this.profileForm.effectiveFrom,
      bankAccountRef: (this.profileForm as any).bankAccountRef || null,
    }).subscribe({
      next: (pr) => {
        this.saving = false;
        this.profile = pr;
      },
      error: (e) => { this.saving = false; this.errorMsg = e?.error?.message ?? 'Failed to save profile.'; }
    });
  }
}
