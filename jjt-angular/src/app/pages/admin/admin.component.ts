import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import {
  ChildDto,
  CreateSponsorResponse,
  SponsorshipSummaryResponse,
  SponsorshipStatus,
  UserResponse,
} from '../../services/api.models';

type TabId =
  | 'child' | 'sponsor' | 'earlySupport' | 'progress' | 'sponsorship'
  | 'pending' | 'active' | 'users' | 'docs';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly authService  = inject(AuthService);

  activeTab: TabId = 'child';
  successMessage: string | null = null;
  errorMessage:   string | null = null;
  isLoading = false;
  isJjtAdmin = false;

  // ── Shared dropdown data ───────────────────────────────────────────────────
  children:  ChildDto[]              = [];
  sponsors:  CreateSponsorResponse[] = [];
  loadingDropdowns = false;

  // ── Create Child ────────────────────────────────────────────────────────
  childForm = {
    rollNumber: '', fullName: '', city: '', campusName: '',
    schoolName: '', educationAmount: '2000.00', educationCurrency: 'PKR'
  };

  // ── Create Sponsor ──────────────────────────────────────────────────────
  sponsorForm = { displayName: '', contactEmail: '', phone: '' };
  createdSponsorId: string | null = null;

  // ── Record Early Support ─────────────────────────────────────────────────
  earlySupportForm = {
    childId: '', month: '', educationAmount: '2000.00', educationCurrency: 'PKR'
  };
  pastMonths: string[] = this.generatePastMonths(24);

  // ── Add Progress ─────────────────────────────────────────────────────────
  progressForm = { childId: '', month: '', summary: '' };
  progressLedgerMonths: string[] = [];
  loadingProgressMonths = false;

  // ── Commit Sponsorship ───────────────────────────────────────────────────
  sponsorshipForm = {
    sponsorId: '', childId: '', startMonth: '', commitmentType: 'MONTHLY' as 'MONTHLY' | 'YEARLY'
  };
  futureMonths: string[] = this.generateFutureMonths(12);

  // ── Sponsorship lists ─────────────────────────────────────────────────────
  pendingSponsorships: SponsorshipSummaryResponse[] = [];
  activeSponsorships:  SponsorshipSummaryResponse[] = [];
  loadingList = false;

  // ── User management ───────────────────────────────────────────────────────
  users: UserResponse[] = [];
  loadingUsers = false;
  sponsorUserForm     = { sponsorId: '', email: '', password: '' };
  orgUserForm         = { email: '', password: '', orgId: '' };
  showSponsorUserForm = false;
  showOrgUserForm     = false;

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  get visibleTabs(): { id: TabId; label: string }[] {
    const base: { id: TabId; label: string }[] = [
      { id: 'child',        label: 'Add Child' },
      { id: 'sponsor',      label: 'Add Sponsor' },
      { id: 'earlySupport', label: 'Early Support' },
      { id: 'progress',     label: 'Add Progress' },
      { id: 'sponsorship',  label: 'Commit Sponsorship' },
      { id: 'pending',      label: 'Pending' },
      { id: 'active',       label: 'Active' },
      { id: 'docs',         label: 'How It Works' },
    ];
    if (this.isJjtAdmin) {
      base.push({ id: 'users', label: 'Users' });
    }
    return base;
  }

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.isJjtAdmin = user?.role === 'JJT_ADMIN';
    this.loadDropdowns();
  }

  private loadDropdowns(): void {
    this.loadingDropdowns = true;
    this.adminService.getOrgChildren().subscribe({
      next: (data) => { this.children = data; },
      error: () => {}
    });
    this.adminService.listSponsors().subscribe({
      next: (data) => { this.sponsors = data; this.loadingDropdowns = false; },
      error: () => { this.loadingDropdowns = false; }
    });
  }

  // ── Tab switching ─────────────────────────────────────────────────────────

  setTab(tab: TabId): void {
    this.activeTab = tab;
    this.clearMessages();
    if (tab === 'pending') this.loadList('PENDING');
    if (tab === 'active')  this.loadList('ACTIVE');
    if (tab === 'users')   this.loadUsers();
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  clearMessages(): void {
    this.successMessage = null;
    this.errorMessage   = null;
  }

  private handleError(err: any, fallback = 'An error occurred.'): void {
    this.isLoading = false;
    this.errorMessage = err?.error?.message ?? fallback;
  }

  private newUuid(): string {
    return crypto.randomUUID();
  }

  childName(childId: string): string {
    const c = this.children.find(x => x.id === childId);
    return c ? `${c.fullName} (${c.rollNumber})` : childId;
  }

  private generatePastMonths(count: number): string[] {
    const months: string[] = [];
    const now = new Date();
    for (let i = 0; i < count; i++) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      months.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`);
    }
    return months;
  }

  private generateFutureMonths(count: number): string[] {
    const months: string[] = [];
    const now = new Date();
    for (let i = 1; i <= count; i++) {
      const d = new Date(now.getFullYear(), now.getMonth() + i, 1);
      months.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`);
    }
    return months;
  }

  // ── Child ─────────────────────────────────────────────────────────────────

  createChild(): void {
    this.clearMessages();
    if (this.isLoading) return;
    this.isLoading = true;

    this.adminService.createChild({
      ...this.childForm,
      childId:  this.newUuid(),
      ledgerId: this.newUuid(),
      schoolName: this.childForm.schoolName || null,
    }).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.successMessage = `Child created. ID: ${res.childId}`;
        this.childForm = { rollNumber: '', fullName: '', city: '', campusName: '', schoolName: '', educationAmount: '2000.00', educationCurrency: 'PKR' };
        this.loadDropdowns();
      },
      error: (err) => this.handleError(err, 'Failed to create child.')
    });
  }

  // ── Sponsor ───────────────────────────────────────────────────────────────

  createSponsor(): void {
    this.clearMessages();
    if (this.isLoading) return;
    this.isLoading = true;
    this.createdSponsorId = null;

    this.adminService.createSponsor({
      ...this.sponsorForm,
      sponsorId: this.newUuid(),
      phone: this.sponsorForm.phone || null,
    }).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.createdSponsorId = res.sponsorId;
        this.successMessage = `Sponsor created. ID: ${res.sponsorId}`;
        this.sponsorForm = { displayName: '', contactEmail: '', phone: '' };
        this.loadDropdowns();
      },
      error: (err) => this.handleError(err, 'Failed to create sponsor.')
    });
  }

  async copySponsorId(): Promise<void> {
    if (!this.createdSponsorId) return;
    await navigator.clipboard.writeText(this.createdSponsorId);
  }

  // ── Early Support ──────────────────────────────────────────────────────────

  recordEarlySupport(): void {
    this.clearMessages();
    if (this.isLoading) return;
    this.isLoading = true;

    this.adminService.recordEarlySupport({ ...this.earlySupportForm }).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Early support recorded.';
        this.earlySupportForm = { childId: '', month: '', educationAmount: '2000.00', educationCurrency: 'PKR' };
      },
      error: (err) => this.handleError(err, 'Failed to record early support.')
    });
  }

  // ── Progress ───────────────────────────────────────────────────────────────

  onProgressChildChange(): void {
    this.progressForm.month = '';
    this.progressLedgerMonths = [];
    if (!this.progressForm.childId) return;
    this.loadingProgressMonths = true;
    this.adminService.getOrgLedger(this.progressForm.childId).subscribe({
      next: (ledger) => {
        this.progressLedgerMonths = ledger.entries.map(e => e.month);
        this.loadingProgressMonths = false;
      },
      error: () => { this.loadingProgressMonths = false; }
    });
  }

  addProgress(): void {
    this.clearMessages();
    if (this.isLoading) return;
    this.isLoading = true;

    const { childId, ...rest } = this.progressForm;
    this.adminService.addProgress(childId, rest).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Progress update added.';
        this.progressForm = { childId: '', month: '', summary: '' };
        this.progressLedgerMonths = [];
      },
      error: (err) => this.handleError(err, 'Failed to add progress update.')
    });
  }

  // ── Sponsorship ────────────────────────────────────────────────────────────

  commitSponsorship(): void {
    this.clearMessages();
    if (this.isLoading) return;
    this.isLoading = true;

    this.adminService.commitSponsorship({ ...this.sponsorshipForm }).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Sponsorship committed. Go to Pending tab to activate it.';
        this.sponsorshipForm = { sponsorId: '', childId: '', startMonth: '', commitmentType: 'MONTHLY' };
      },
      error: (err) => this.handleError(err, 'Failed to commit sponsorship.')
    });
  }

  loadList(status: SponsorshipStatus): void {
    this.loadingList = true;
    this.adminService.listSponsorships(status).subscribe({
      next: (data) => {
        this.loadingList = false;
        if (status === 'PENDING') this.pendingSponsorships = data;
        else this.activeSponsorships = data;
      },
      error: (err) => {
        this.loadingList = false;
        this.errorMessage = err?.error?.message ?? 'Failed to load sponsorships.';
      }
    });
  }

  activate(sponsorshipId: string): void {
    this.clearMessages();
    this.adminService.activateSponsorship(sponsorshipId).subscribe({
      next: () => {
        this.successMessage = 'Sponsorship activated.';
        this.loadList('PENDING');
        if (this.activeTab === 'active') this.loadList('ACTIVE');
      },
      error: (err) => this.handleError(err, 'Failed to activate sponsorship.')
    });
  }

  expire(sponsorshipId: string): void {
    this.clearMessages();
    this.adminService.expireSponsorship(sponsorshipId).subscribe({
      next: () => {
        this.successMessage = 'Sponsorship expired.';
        this.loadList(this.activeTab === 'active' ? 'ACTIVE' : 'PENDING');
      },
      error: (err) => this.handleError(err, 'Failed to expire sponsorship.')
    });
  }

  // ── User Management ────────────────────────────────────────────────────────

  loadUsers(): void {
    this.loadingUsers = true;
    this.adminService.listUsers().subscribe({
      next: (data) => { this.loadingUsers = false; this.users = data; },
      error: (err) => { this.loadingUsers = false; this.errorMessage = err?.error?.message ?? 'Failed to load users.'; }
    });
  }

  createSponsorUser(): void {
    this.clearMessages();
    if (this.isLoading) return;
    this.isLoading = true;

    this.adminService.createSponsorUser(this.sponsorUserForm).subscribe({
      next: (u) => {
        this.isLoading = false;
        this.successMessage = `Sponsor user created: ${u.email}`;
        this.sponsorUserForm = { sponsorId: '', email: '', password: '' };
        this.showSponsorUserForm = false;
        this.loadUsers();
      },
      error: (err) => this.handleError(err, 'Failed to create sponsor user.')
    });
  }

  createOrgUser(): void {
    this.clearMessages();
    if (this.isLoading) return;
    this.isLoading = true;

    this.adminService.createOrgAdminUser({
      ...this.orgUserForm,
      orgId: this.orgUserForm.orgId || null,
    }).subscribe({
      next: (u) => {
        this.isLoading = false;
        this.successMessage = `Org admin user created: ${u.email}`;
        this.orgUserForm = { email: '', password: '', orgId: '' };
        this.showOrgUserForm = false;
        this.loadUsers();
      },
      error: (err) => this.handleError(err, 'Failed to create org user.')
    });
  }

  setUserActive(userId: string, active: boolean): void {
    this.clearMessages();
    const call = active
      ? this.adminService.activateUser(userId)
      : this.adminService.deactivateUser(userId);

    call.subscribe({
      next: (u) => {
        this.successMessage = `User ${u.email} ${active ? 'activated' : 'deactivated'}.`;
        this.loadUsers();
      },
      error: (err) => this.handleError(err)
    });
  }
}
