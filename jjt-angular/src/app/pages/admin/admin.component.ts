import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import {
  ChildDto,
  CreateSponsorResponse,
  SponsorshipSummaryResponse,
  SponsorshipStatus,
  UserResponse,
} from '../../services/api.models';

type SectionId =
  | 'dashboard' | 'children' | 'sponsors' | 'commitments'
  | 'earlySupport' | 'progress' | 'users' | 'reports' | 'docs';

type ModalType =
  | 'addChild' | 'addSponsor' | 'addCommitment'
  | 'addSponsorUser' | 'addOrgUser' | null;

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly authService  = inject(AuthService);

  // ── Navigation ────────────────────────────────────────────────────
  activeSection: SectionId = 'dashboard';
  isJjtAdmin = false;
  currentUserEmail = '';
  currentUserInitials = 'JA';

  // ── Modal & Toast ─────────────────────────────────────────────────
  modalType: ModalType = null;
  toastMessage: string | null = null;
  private toastTimer: ReturnType<typeof setTimeout> | null = null;

  // ── Loading / Error ───────────────────────────────────────────────
  isLoading = false;
  errorMessage: string | null = null;

  // ── Shared dropdown data ──────────────────────────────────────────
  children: ChildDto[] = [];
  sponsors: CreateSponsorResponse[] = [];

  // ── Table search / filter ─────────────────────────────────────────
  childSearch = '';
  sponsorSearch = '';
  commitmentFilter: 'ALL' | 'PENDING' | 'ACTIVE' = 'ALL';
  userSearch = '';

  // ── Create Child ──────────────────────────────────────────────────
  childForm = {
    rollNumber: '', fullName: '', city: '', campusName: '',
    schoolName: '', educationAmount: '2000.00', educationCurrency: 'PKR'
  };

  // ── Create Sponsor ────────────────────────────────────────────────
  sponsorForm = { displayName: '', contactEmail: '', phone: '' };
  createdSponsorId: string | null = null;

  // ── Early Support ─────────────────────────────────────────────────
  earlySupportForm = {
    childId: '', month: '', educationAmount: '2000.00', educationCurrency: 'PKR'
  };
  pastMonths: string[] = this.generatePastMonths(24);

  // ── Progress ──────────────────────────────────────────────────────
  progressForm = { childId: '', month: '', summary: '' };
  progressLedgerMonths: string[] = [];
  loadingProgressMonths = false;

  // ── Commit Sponsorship ────────────────────────────────────────────
  sponsorshipForm = {
    sponsorId: '', childId: '', startMonth: '', commitmentType: 'MONTHLY' as 'MONTHLY' | 'YEARLY'
  };
  futureMonths: string[] = this.generateFutureMonths(12);

  // ── Sponsorship lists ─────────────────────────────────────────────
  pendingSponsorships: SponsorshipSummaryResponse[] = [];
  activeSponsorships:  SponsorshipSummaryResponse[] = [];
  loadingList = false;

  // ── Users ─────────────────────────────────────────────────────────
  users: UserResponse[] = [];
  loadingUsers = false;
  sponsorUserForm     = { sponsorId: '', email: '', password: '' };
  orgUserForm         = { email: '', password: '', orgId: '' };

  // ── Lifecycle ─────────────────────────────────────────────────────

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.isJjtAdmin = user?.role === 'JJT_ADMIN';
    this.currentUserEmail = user?.email ?? '';
    this.currentUserInitials = this.toInitials(user?.email ?? 'ja');
    this.loadDropdowns();
    this.loadList('PENDING');
    this.loadList('ACTIVE');
  }

  // ── Navigation ────────────────────────────────────────────────────

  setSection(s: SectionId): void {
    this.activeSection = s;
    this.errorMessage = null;
    if (s === 'users') this.loadUsers();
  }

  openModal(type: ModalType): void {
    this.modalType = type;
    this.errorMessage = null;
  }

  closeModal(): void {
    this.modalType = null;
    this.errorMessage = null;
  }

  stopProp(e: Event): void { e.stopPropagation(); }

  showToast(msg: string): void {
    this.toastMessage = msg;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => { this.toastMessage = null; }, 3000);
  }

  // ── Derived / computed ────────────────────────────────────────────

  get sponsoredCount(): number {
    return this.children.filter(c => c.availabilityStatus === 'ALLOCATED').length;
  }

  get seekingCount(): number {
    return this.children.filter(c => c.availabilityStatus === 'AVAILABLE').length;
  }

  get allCommitments(): SponsorshipSummaryResponse[] {
    if (this.commitmentFilter === 'PENDING') return this.pendingSponsorships;
    if (this.commitmentFilter === 'ACTIVE')  return this.activeSponsorships;
    return [...this.pendingSponsorships, ...this.activeSponsorships];
  }

  get filteredChildren(): ChildDto[] {
    const q = this.childSearch.toLowerCase().trim();
    if (!q) return this.children;
    return this.children.filter(c =>
      c.fullName.toLowerCase().includes(q) || c.rollNumber.toLowerCase().includes(q)
    );
  }

  get filteredSponsors(): CreateSponsorResponse[] {
    const q = this.sponsorSearch.toLowerCase().trim();
    if (!q) return this.sponsors;
    return this.sponsors.filter(s =>
      s.displayName.toLowerCase().includes(q) || s.contactEmail.toLowerCase().includes(q)
    );
  }

  get filteredUsers(): UserResponse[] {
    const q = this.userSearch.toLowerCase().trim();
    if (!q) return this.users;
    return this.users.filter(u => u.email.toLowerCase().includes(q));
  }

  // ── Helpers ───────────────────────────────────────────────────────

  private toInitials(s: string): string {
    const parts = s.split('@')[0].split(/[._-]/);
    return parts.slice(0, 2).map(p => p.charAt(0).toUpperCase()).join('') || 'JA';
  }

  childName(childId: string): string {
    const c = this.children.find(x => x.id === childId);
    return c ? `${c.fullName} (${c.rollNumber})` : childId.substring(0, 8) + '…';
  }

  initials(name: string): string {
    return (name || '?').split(' ').slice(0, 2).map(w => w.charAt(0)).join('').toUpperCase();
  }

  availabilityLabel(status: string): string {
    if (status === 'ALLOCATED') return 'Sponsored';
    if (status === 'RESERVED')  return 'Reserved';
    return 'Seeking';
  }

  availabilityClass(status: string): string {
    if (status === 'ALLOCATED') return 'badge-green';
    if (status === 'RESERVED')  return 'badge-amber';
    return 'badge-grey';
  }

  private handleError(err: any, fallback = 'An error occurred.'): void {
    this.isLoading = false;
    this.errorMessage = err?.error?.message ?? fallback;
  }

  private newUuid(): string { return crypto.randomUUID(); }

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

  // ── Data loading ──────────────────────────────────────────────────

  private loadDropdowns(): void {
    this.adminService.getOrgChildren().subscribe({ next: (data) => { this.children = data; } });
    this.adminService.listSponsors().subscribe({ next: (data) => { this.sponsors = data; } });
  }

  // ── Child ─────────────────────────────────────────────────────────

  createChild(): void {
    if (this.isLoading) return;
    this.isLoading = true;
    this.errorMessage = null;

    this.adminService.createChild({
      ...this.childForm,
      childId:  this.newUuid(),
      ledgerId: this.newUuid(),
      schoolName: this.childForm.schoolName || null,
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.closeModal();
        this.showToast('Child created successfully.');
        this.childForm = { rollNumber: '', fullName: '', city: '', campusName: '', schoolName: '', educationAmount: '2000.00', educationCurrency: 'PKR' };
        this.loadDropdowns();
      },
      error: (err) => this.handleError(err, 'Failed to create child.')
    });
  }

  // ── Sponsor ───────────────────────────────────────────────────────

  createSponsor(): void {
    if (this.isLoading) return;
    this.isLoading = true;
    this.errorMessage = null;
    this.createdSponsorId = null;

    this.adminService.createSponsor({
      ...this.sponsorForm,
      sponsorId: this.newUuid(),
      phone: this.sponsorForm.phone || null,
    }).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.createdSponsorId = res.sponsorId;
        this.closeModal();
        this.showToast(`Sponsor created. ID: ${res.sponsorId}`);
        this.sponsorForm = { displayName: '', contactEmail: '', phone: '' };
        this.loadDropdowns();
      },
      error: (err) => this.handleError(err, 'Failed to create sponsor.')
    });
  }

  async copySponsorId(): Promise<void> {
    if (!this.createdSponsorId) return;
    await navigator.clipboard.writeText(this.createdSponsorId);
    this.showToast('Sponsor ID copied to clipboard.');
  }

  // ── Early Support ─────────────────────────────────────────────────

  recordEarlySupport(): void {
    if (this.isLoading) return;
    this.isLoading = true;
    this.errorMessage = null;

    this.adminService.recordEarlySupport({ ...this.earlySupportForm }).subscribe({
      next: () => {
        this.isLoading = false;
        this.showToast('Early support recorded.');
        this.earlySupportForm = { childId: '', month: '', educationAmount: '2000.00', educationCurrency: 'PKR' };
      },
      error: (err) => this.handleError(err, 'Failed to record early support.')
    });
  }

  // ── Progress ──────────────────────────────────────────────────────

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
    if (this.isLoading) return;
    this.isLoading = true;
    this.errorMessage = null;

    const { childId, ...rest } = this.progressForm;
    this.adminService.addProgress(childId, rest).subscribe({
      next: () => {
        this.isLoading = false;
        this.showToast('Progress update added.');
        this.progressForm = { childId: '', month: '', summary: '' };
        this.progressLedgerMonths = [];
      },
      error: (err) => this.handleError(err, 'Failed to add progress update.')
    });
  }

  // ── Sponsorship ───────────────────────────────────────────────────

  commitSponsorship(): void {
    if (this.isLoading) return;
    this.isLoading = true;
    this.errorMessage = null;

    this.adminService.commitSponsorship({ ...this.sponsorshipForm }).subscribe({
      next: () => {
        this.isLoading = false;
        this.closeModal();
        this.showToast('Sponsorship committed — activate it from the Commitments view.');
        this.sponsorshipForm = { sponsorId: '', childId: '', startMonth: '', commitmentType: 'MONTHLY' };
        this.loadList('PENDING');
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
      error: () => { this.loadingList = false; }
    });
  }

  activate(sponsorshipId: string): void {
    this.adminService.activateSponsorship(sponsorshipId).subscribe({
      next: () => {
        this.showToast('Sponsorship activated.');
        this.loadList('PENDING');
        this.loadList('ACTIVE');
      },
      error: (err) => this.handleError(err, 'Failed to activate.')
    });
  }

  expire(sponsorshipId: string): void {
    this.adminService.expireSponsorship(sponsorshipId).subscribe({
      next: () => {
        this.showToast('Sponsorship expired.');
        this.loadList('PENDING');
        this.loadList('ACTIVE');
      },
      error: (err) => this.handleError(err, 'Failed to expire.')
    });
  }

  // ── Users ─────────────────────────────────────────────────────────

  loadUsers(): void {
    this.loadingUsers = true;
    this.adminService.listUsers().subscribe({
      next: (data) => { this.loadingUsers = false; this.users = data; },
      error: () => { this.loadingUsers = false; }
    });
  }

  createSponsorUser(): void {
    if (this.isLoading) return;
    this.isLoading = true;
    this.errorMessage = null;

    this.adminService.createSponsorUser(this.sponsorUserForm).subscribe({
      next: (u) => {
        this.isLoading = false;
        this.closeModal();
        this.showToast(`Sponsor user created: ${u.email}`);
        this.sponsorUserForm = { sponsorId: '', email: '', password: '' };
        this.loadUsers();
      },
      error: (err) => this.handleError(err, 'Failed to create sponsor user.')
    });
  }

  createOrgUser(): void {
    if (this.isLoading) return;
    this.isLoading = true;
    this.errorMessage = null;

    this.adminService.createOrgAdminUser({
      ...this.orgUserForm,
      orgId: this.orgUserForm.orgId || null,
    }).subscribe({
      next: (u) => {
        this.isLoading = false;
        this.closeModal();
        this.showToast(`Org admin created: ${u.email}`);
        this.orgUserForm = { email: '', password: '', orgId: '' };
        this.loadUsers();
      },
      error: (err) => this.handleError(err, 'Failed to create org admin.')
    });
  }

  setUserActive(userId: string, active: boolean): void {
    const call = active ? this.adminService.activateUser(userId) : this.adminService.deactivateUser(userId);
    call.subscribe({
      next: (u) => {
        this.showToast(`User ${u.email} ${active ? 'activated' : 'deactivated'}.`);
        this.loadUsers();
      },
      error: (err) => this.handleError(err)
    });
  }
}
