import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';
import {
  AlertResponse,
  ChildDto,
  CreateSponsorResponse,
  DonationResponse,
  DonorResponse,
  DonorType,
  DonationType,
  DonationFrequency,
  FundAccountResponse,
  MonthlyReconciliationResponse,
  OrgConfigResponse,
  RecurringDonationResponse,
  SponsorPaymentResponse,
  SponsorshipSummaryResponse,
  SponsorshipStatus,
  UserResponse,
} from '../../services/api.models';

type SectionId =
  | 'dashboard' | 'children' | 'sponsors' | 'commitments'
  | 'earlySupport' | 'progress' | 'users'
  | 'funds' | 'reconciliation' | 'alerts' | 'reports' | 'settings' | 'docs'
  | 'donors' | 'donations' | 'campaigns' | 'audit' | 'import';

type ModalType =
  | 'addChild' | 'addSponsor' | 'addCommitment'
  | 'addSponsorUser' | 'addOrgUser' | 'recordPayment'
  | 'addDonor' | 'addDonation' | 'addRecurring'
  | 'addCampaign' | null;

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

  // ── Fund accounts ─────────────────────────────────────────────────
  fundAccounts: FundAccountResponse[] = [];
  loadingFunds = false;

  // ── Reconciliation ────────────────────────────────────────────────
  reconYear  = new Date().getFullYear();
  reconMonth = new Date().getMonth() + 1;
  reconData: MonthlyReconciliationResponse | null = null;
  loadingRecon = false;

  // ── Record payment modal ──────────────────────────────────────────
  recordPaymentId: string | null = null;
  recordPaymentForm = { receivedAmount: '', currency: 'PKR', bankReference: '', receivedDate: '' };

  // ── Alerts ────────────────────────────────────────────────────────
  alertList: AlertResponse[] = [];
  loadingAlerts = false;

  // ── Donors ────────────────────────────────────────────────────────
  donors: DonorResponse[] = [];
  donorSearch = '';
  loadingDonors = false;
  donorForm: { displayName: string; email: string; phone: string; donorType: DonorType; notes: string } = {
    displayName: '', email: '', phone: '', donorType: 'INDIVIDUAL', notes: ''
  };
  readonly donorTypes: DonorType[] = ['INDIVIDUAL', 'CORPORATE', 'TRUST', 'ANONYMOUS'];

  // ── Donations ─────────────────────────────────────────────────────
  donations: DonationResponse[] = [];
  donationsPage = 0;
  donationsTotalPages = 1;
  loadingDonations = false;
  donationsTab: 'one-time' | 'recurring' = 'one-time';
  donationForm: { donorId: string; donationType: DonationType; amount: string; currency: string; donationDate: string; notes: string; fundAccountId: string } = {
    donorId: '', donationType: 'GENERAL', amount: '', currency: 'PKR',
    donationDate: new Date().toISOString().split('T')[0], notes: '', fundAccountId: ''
  };
  recurringDonations: RecurringDonationResponse[] = [];
  loadingRecurring = false;
  recurringForm: { donorId: string; donationType: DonationType; amount: string; currency: string; frequency: DonationFrequency; startDate: string; endDate: string; fundAccountId: string; notes: string } = {
    donorId: '', donationType: 'GENERAL', amount: '', currency: 'PKR',
    frequency: 'MONTHLY', startDate: new Date().toISOString().split('T')[0],
    endDate: '', fundAccountId: '', notes: ''
  };
  readonly donationTypes: DonationType[] = ['GENERAL', 'ZAKAT', 'SADAQAH', 'SPONSORSHIP_TOP_UP', 'CORPORATE', 'IN_KIND'];
  readonly frequencies: DonationFrequency[] = ['MONTHLY', 'QUARTERLY', 'ANNUAL'];

  // ── Campaigns ─────────────────────────────────────────────────────
  campaigns: any[] = [];
  loadingCampaigns = false;
  campaignForm = {
    name: '', description: '', targetAmount: '', targetCurrency: 'PKR',
    startDate: '', endDate: '', fundAccountId: ''
  };

  // ── Audit log ─────────────────────────────────────────────────────
  auditEvents: any[] = [];
  auditTotalPages = 1;
  auditPage = 0;
  loadingAudit = false;

  // ── Reports ───────────────────────────────────────────────────────
  cashFlowData: any = null;
  portfolioData: any = null;
  loadingReports = false;

  // ── Admin Children list (rich detail view) ────────────────────────
  adminChildren: any[] = [];
  loadingAdminChildren = false;
  selectedChildDetail: any = null;
  loadingChildDetail = false;
  adminChildSearch = '';

  // ── Bulk import ───────────────────────────────────────────────────
  importFile: File | null = null;
  importResult: any = null;
  importLoading = false;
  importError: string | null = null;

  // ── Settings ──────────────────────────────────────────────────────
  settingsTab: 'profile' | 'org' | 'security' = 'profile';
  orgConfig: OrgConfigResponse | null = null;
  loadingOrgConfig = false;
  orgConfigForm = { name: '', baseCurrency: 'PKR', paymentDueDay: 5 };
  passwordForm  = { currentPassword: '', newPassword: '', confirmPassword: '' };
  passwordError: string | null = null;
  passwordSuccess = false;

  // ── Lifecycle ─────────────────────────────────────────────────────

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.isJjtAdmin = user?.role === 'JJT_ADMIN';
    this.currentUserEmail = user?.email ?? '';
    this.currentUserInitials = this.toInitials(user?.email ?? 'ja');
    this.loadDropdowns();
    this.loadList('PENDING');
    this.loadList('ACTIVE');
    this.loadFundAccounts();
    this.loadAlerts();
    this.loadRecon();
  }

  // ── Navigation ────────────────────────────────────────────────────

  setSection(s: SectionId): void {
    this.activeSection = s;
    this.errorMessage = null;
    if (s === 'users')          this.loadUsers();
    if (s === 'funds')          this.loadFundAccounts();
    if (s === 'reconciliation') this.loadRecon();
    if (s === 'alerts')         this.loadAlerts();
    if (s === 'settings')       this.loadOrgConfig();
    if (s === 'donors')         this.loadDonors();
    if (s === 'donations')      { this.loadDonations(); this.loadRecurring(); }
    if (s === 'campaigns')      this.loadCampaigns();
    if (s === 'audit')          this.loadAuditLog();
    if (s === 'reports')        this.loadReportData();
    if (s === 'children')       this.loadAdminChildren();
  }

  openModal(type: ModalType): void {
    this.modalType = type;
    this.errorMessage = null;
  }

  closeModal(): void {
    this.modalType = null;
    this.errorMessage = null;
    this.recordPaymentId = null;
  }

  stopProp(e: Event): void { e.stopPropagation(); }

  showToast(msg: string): void {
    this.toastMessage = msg;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => { this.toastMessage = null; }, 3500);
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

  get totalFundBalance(): string {
    if (!this.fundAccounts.length) return '—';
    const total = this.fundAccounts.reduce((sum, f) => sum + parseFloat(f.balance || '0'), 0);
    return total.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  get reconMonthLabel(): string {
    const d = new Date(this.reconYear, this.reconMonth - 1, 1);
    return d.toLocaleString('en-US', { month: 'long', year: 'numeric' });
  }

  get reconPendingPayments(): SponsorPaymentResponse[] {
    return (this.reconData?.payments ?? []).filter(p =>
      p.status === 'PENDING' || p.status === 'OVERDUE' || p.status === 'PARTIAL'
    );
  }

  get citySummary(): { city: string; count: number }[] {
    const counts = new Map<string, number>();
    for (const c of this.children) {
      counts.set(c.city, (counts.get(c.city) ?? 0) + 1);
    }
    return Array.from(counts.entries())
      .map(([city, count]) => ({ city, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 6);
  }

  get criticalAlerts(): AlertResponse[] {
    return this.alertList.filter(a => a.severity === 'HIGH' || a.severity === 'CRITICAL');
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

  paymentStatusClass(status: string): string {
    if (status === 'RECEIVED') return 'badge-green';
    if (status === 'OVERDUE')  return 'badge-red';
    if (status === 'PARTIAL')  return 'badge-amber';
    if (status === 'WAIVED')   return 'badge-grey';
    return 'badge-grey';
  }

  alertSeverityClass(sev: string): string {
    if (sev === 'CRITICAL' || sev === 'HIGH') return 'badge-red';
    if (sev === 'MEDIUM')  return 'badge-amber';
    return 'badge-grey';
  }

  formatAmount(val: string | null, currency = 'PKR'): string {
    if (!val) return '—';
    const n = parseFloat(val);
    return `${currency} ${n.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 2 })}`;
  }

  timeAgo(iso: string): string {
    const diff = Date.now() - new Date(iso).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h ago`;
    const days = Math.floor(hrs / 24);
    return `${days}d ago`;
  }

  fundReservedPct(fund: FundAccountResponse): number {
    const bal = parseFloat(fund.balance || '0');
    const reserve = parseFloat(fund.minReserve || '0');
    if (bal === 0) return 0;
    return Math.min(100, Math.round((reserve / bal) * 100));
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
        this.loadDropdowns();
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
        this.loadDropdowns();
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

  // ── Fund accounts ─────────────────────────────────────────────────

  loadFundAccounts(): void {
    this.loadingFunds = true;
    this.adminService.listFundAccounts().subscribe({
      next: (data) => { this.fundAccounts = data; this.loadingFunds = false; },
      error: () => { this.loadingFunds = false; }
    });
  }

  // ── Reconciliation ────────────────────────────────────────────────

  loadRecon(): void {
    this.loadingRecon = true;
    this.adminService.getMonthlyReconciliation(this.reconYear, this.reconMonth).subscribe({
      next: (data) => { this.reconData = data; this.loadingRecon = false; },
      error: () => { this.loadingRecon = false; }
    });
  }

  prevReconMonth(): void {
    if (this.reconMonth === 1) { this.reconMonth = 12; this.reconYear--; }
    else this.reconMonth--;
    this.loadRecon();
  }

  nextReconMonth(): void {
    const now = new Date();
    if (this.reconYear === now.getFullYear() && this.reconMonth === now.getMonth() + 1) return;
    if (this.reconMonth === 12) { this.reconMonth = 1; this.reconYear++; }
    else this.reconMonth++;
    this.loadRecon();
  }

  openRecordPayment(payment: SponsorPaymentResponse): void {
    this.recordPaymentId = payment.id;
    this.recordPaymentForm = {
      receivedAmount: payment.expectedAmount,
      currency: payment.expectedCurrency,
      bankReference: '',
      receivedDate: new Date().toISOString().split('T')[0]
    };
    this.openModal('recordPayment');
  }

  submitRecordPayment(): void {
    if (!this.recordPaymentId || this.isLoading) return;
    this.isLoading = true;
    this.errorMessage = null;

    this.adminService.receivePayment(this.recordPaymentId, this.recordPaymentForm).subscribe({
      next: () => {
        this.isLoading = false;
        this.closeModal();
        this.showToast('Payment recorded successfully.');
        this.loadRecon();
      },
      error: (err) => this.handleError(err, 'Failed to record payment.')
    });
  }

  submitWaivePayment(paymentId: string): void {
    this.adminService.waivePayment(paymentId, { reason: 'Waived by admin' }).subscribe({
      next: () => { this.showToast('Payment waived.'); this.loadRecon(); },
      error: (err) => this.handleError(err, 'Failed to waive payment.')
    });
  }

  // ── Alerts ────────────────────────────────────────────────────────

  loadAlerts(): void {
    this.loadingAlerts = true;
    this.adminService.listAlerts().subscribe({
      next: (data) => { this.alertList = data; this.loadingAlerts = false; },
      error: () => { this.loadingAlerts = false; }
    });
  }

  dismissAlert(id: string): void {
    this.adminService.dismissAlert(id).subscribe({
      next: () => {
        this.alertList = this.alertList.filter(a => a.id !== id);
        this.showToast('Alert dismissed.');
      },
      error: (err) => this.handleError(err, 'Failed to dismiss alert.')
    });
  }

  // ── Settings ──────────────────────────────────────────────────────

  loadOrgConfig(): void {
    if (this.orgConfig) return;
    this.loadingOrgConfig = true;
    this.adminService.getOrgConfig().subscribe({
      next: (data) => {
        this.orgConfig = data;
        this.orgConfigForm = { name: data.name, baseCurrency: data.baseCurrency, paymentDueDay: data.paymentDueDay };
        this.loadingOrgConfig = false;
      },
      error: () => { this.loadingOrgConfig = false; }
    });
  }

  saveOrgConfig(): void {
    if (this.isLoading || !this.isJjtAdmin) return;
    this.isLoading = true;
    this.adminService.updateOrgConfig({
      name: this.orgConfigForm.name,
      baseCurrency: this.orgConfigForm.baseCurrency,
      paymentDueDay: this.orgConfigForm.paymentDueDay,
    }).subscribe({
      next: (data) => {
        this.isLoading = false;
        this.orgConfig = data;
        this.showToast('Organisation settings saved.');
      },
      error: (err) => this.handleError(err, 'Failed to save settings.')
    });
  }

  changePassword(): void {
    this.passwordError = null;
    this.passwordSuccess = false;

    if (!this.passwordForm.currentPassword || !this.passwordForm.newPassword) {
      this.passwordError = 'All fields are required.';
      return;
    }
    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      this.passwordError = 'New passwords do not match.';
      return;
    }
    if (this.passwordForm.newPassword.length < 8) {
      this.passwordError = 'New password must be at least 8 characters.';
      return;
    }
    if (this.isLoading) return;
    this.isLoading = true;

    this.adminService.changePassword(this.passwordForm.currentPassword, this.passwordForm.newPassword).subscribe({
      next: () => {
        this.isLoading = false;
        this.passwordSuccess = true;
        this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
        this.showToast('Password changed successfully.');
      },
      error: (err) => {
        this.isLoading = false;
        this.passwordError = err?.error?.message ?? 'Failed to change password.';
      }
    });
  }

  // ── Generate payments (JJT_ADMIN only) ───────────────────────────

  generatePayments(): void {
    if (!this.isJjtAdmin || this.isLoading) return;
    this.isLoading = true;
    this.adminService.generatePayments().subscribe({
      next: (res) => {
        this.isLoading = false;
        this.showToast(`Generated ${res.created} payment record(s) for ${res.month}.`);
        this.loadRecon();
      },
      error: (err) => this.handleError(err, 'Failed to generate payments.')
    });
  }

  // ── Donors ────────────────────────────────────────────────────────

  get filteredDonors(): DonorResponse[] {
    const q = this.donorSearch.toLowerCase().trim();
    if (!q) return this.donors;
    return this.donors.filter(d =>
      d.displayName.toLowerCase().includes(q) ||
      (d.email ?? '').toLowerCase().includes(q)
    );
  }

  loadDonors(): void {
    this.loadingDonors = true;
    this.adminService.listDonors().subscribe({
      next: (data) => { this.donors = data; this.loadingDonors = false; },
      error: () => { this.loadingDonors = false; }
    });
  }

  createDonor(): void {
    if (this.isLoading || !this.donorForm.displayName.trim()) {
      this.errorMessage = 'Display name is required.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;
    this.adminService.createDonor({
      displayName: this.donorForm.displayName.trim(),
      email:       this.donorForm.email.trim() || null,
      phone:       this.donorForm.phone.trim() || null,
      donorType:   this.donorForm.donorType,
      notes:       this.donorForm.notes.trim() || null,
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.closeModal();
        this.showToast('Donor created.');
        this.donorForm = { displayName: '', email: '', phone: '', donorType: 'INDIVIDUAL', notes: '' };
        this.loadDonors();
      },
      error: (err) => this.handleError(err, 'Failed to create donor.')
    });
  }

  // ── Donations ─────────────────────────────────────────────────────

  loadDonations(page = 0): void {
    this.loadingDonations = true;
    this.adminService.listDonations(page).subscribe({
      next: (res) => {
        this.donations = res.content;
        this.donationsPage = res.number;
        this.donationsTotalPages = res.totalPages;
        this.loadingDonations = false;
      },
      error: () => { this.loadingDonations = false; }
    });
  }

  recordDonation(): void {
    if (this.isLoading || !this.donationForm.amount.trim() || !this.donationForm.donationDate) {
      this.errorMessage = 'Amount and date are required.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;
    this.adminService.recordDonation({
      donorId:       this.donationForm.donorId || null,
      donationType:  this.donationForm.donationType,
      amount:        this.donationForm.amount.trim(),
      currency:      this.donationForm.currency,
      donationDate:  this.donationForm.donationDate,
      notes:         this.donationForm.notes.trim() || null,
      fundAccountId: this.donationForm.fundAccountId || null,
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.closeModal();
        this.showToast('Donation recorded.');
        this.donationForm = { donorId: '', donationType: 'GENERAL', amount: '', currency: 'PKR', donationDate: new Date().toISOString().split('T')[0], notes: '', fundAccountId: '' };
        this.loadDonations();
      },
      error: (err) => this.handleError(err, 'Failed to record donation.')
    });
  }

  receiveDonation(id: string): void {
    this.adminService.receiveDonation(id).subscribe({
      next: () => { this.showToast('Donation marked as received.'); this.loadDonations(this.donationsPage); },
      error: (err) => this.handleError(err, 'Failed to receive donation.')
    });
  }

  reverseDonation(id: string): void {
    this.adminService.reverseDonation(id).subscribe({
      next: () => { this.showToast('Donation reversed.'); this.loadDonations(this.donationsPage); },
      error: (err) => this.handleError(err, 'Failed to reverse donation.')
    });
  }

  viewReceipt(id: string): void {
    this.adminService.getDonationReceipt(id).subscribe({
      next: (receipt) => {
        const lines = [
          `Receipt: ${receipt.receiptNumber}`,
          `Donor: ${receipt.donorName}`,
          `Amount: ${receipt.currency} ${receipt.amount}`,
          `Date: ${receipt.donationDate}`,
          `Type: ${receipt.donationType}`,
          `Organisation: ${receipt.organisationName}`,
          `Issued: ${receipt.issuedDate}`,
        ].join('\n');
        alert(lines);
      },
      error: (err) => this.handleError(err, 'Failed to load receipt.')
    });
  }

  donationStatusClass(status: string): string {
    if (status === 'RECEIPTED') return 'badge-green';
    if (status === 'REVERSED')  return 'badge-red';
    return 'badge-grey';
  }

  recurringStatusClass(status: string): string {
    if (status === 'ACTIVE')    return 'badge-green';
    if (status === 'PAUSED')    return 'badge-amber';
    if (status === 'CANCELLED') return 'badge-red';
    return 'badge-grey';
  }

  // ── Recurring donations ───────────────────────────────────────────

  loadRecurring(): void {
    this.loadingRecurring = true;
    this.adminService.listRecurringDonations().subscribe({
      next: (data) => { this.recurringDonations = data; this.loadingRecurring = false; },
      error: () => { this.loadingRecurring = false; }
    });
  }

  createRecurring(): void {
    if (this.isLoading || !this.recurringForm.donorId || !this.recurringForm.amount.trim()) {
      this.errorMessage = 'Donor and amount are required.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;
    this.adminService.createRecurringDonation({
      donorId:       this.recurringForm.donorId,
      donationType:  this.recurringForm.donationType,
      amount:        this.recurringForm.amount.trim(),
      currency:      this.recurringForm.currency,
      frequency:     this.recurringForm.frequency,
      startDate:     this.recurringForm.startDate,
      endDate:       this.recurringForm.endDate || null,
      fundAccountId: this.recurringForm.fundAccountId || null,
      notes:         this.recurringForm.notes.trim() || null,
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.closeModal();
        this.showToast('Recurring schedule created.');
        this.recurringForm = { donorId: '', donationType: 'GENERAL', amount: '', currency: 'PKR', frequency: 'MONTHLY', startDate: new Date().toISOString().split('T')[0], endDate: '', fundAccountId: '', notes: '' };
        this.loadRecurring();
      },
      error: (err) => this.handleError(err, 'Failed to create recurring donation.')
    });
  }

  pauseRecurring(id: string): void {
    this.adminService.pauseRecurring(id).subscribe({
      next: () => { this.showToast('Schedule paused.'); this.loadRecurring(); },
      error: (err) => this.handleError(err, 'Failed to pause.')
    });
  }

  cancelRecurring(id: string): void {
    this.adminService.cancelRecurring(id).subscribe({
      next: () => { this.showToast('Schedule cancelled.'); this.loadRecurring(); },
      error: (err) => this.handleError(err, 'Failed to cancel.')
    });
  }

  generateRecurringDonations(): void {
    if (!this.isJjtAdmin || this.isLoading) return;
    this.isLoading = true;
    this.adminService.generateRecurringDonations().subscribe({
      next: (count) => {
        this.isLoading = false;
        this.showToast(`${count} expected donation(s) generated from recurring schedules.`);
        this.loadDonations();
      },
      error: (err) => this.handleError(err, 'Failed to generate recurring donations.')
    });
  }

  // ── Campaigns ─────────────────────────────────────────────────────

  loadCampaigns(): void {
    this.loadingCampaigns = true;
    this.adminService.listCampaigns().subscribe({
      next: (data) => { this.campaigns = data; this.loadingCampaigns = false; },
      error: () => { this.loadingCampaigns = false; }
    });
  }

  createCampaign(): void {
    if (this.isLoading || !this.campaignForm.name.trim()) {
      this.errorMessage = 'Campaign name is required.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;
    this.adminService.createCampaign({
      name:           this.campaignForm.name.trim(),
      description:    this.campaignForm.description.trim() || null,
      targetAmount:   this.campaignForm.targetAmount.trim() || null,
      targetCurrency: this.campaignForm.targetCurrency,
      startDate:      this.campaignForm.startDate || null,
      endDate:        this.campaignForm.endDate || null,
      fundAccountId:  this.campaignForm.fundAccountId || null,
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.closeModal();
        this.showToast('Campaign created.');
        this.campaignForm = { name: '', description: '', targetAmount: '', targetCurrency: 'PKR', startDate: '', endDate: '', fundAccountId: '' };
        this.loadCampaigns();
      },
      error: (err) => this.handleError(err, 'Failed to create campaign.')
    });
  }

  openCampaign(id: string): void {
    this.adminService.openCampaign(id).subscribe({
      next: () => { this.showToast('Campaign opened.'); this.loadCampaigns(); },
      error: (err) => this.handleError(err, 'Failed to open campaign.')
    });
  }

  closeCampaign(id: string): void {
    this.adminService.closeCampaign(id).subscribe({
      next: () => { this.showToast('Campaign closed.'); this.loadCampaigns(); },
      error: (err) => this.handleError(err, 'Failed to close campaign.')
    });
  }

  campaignStatusClass(status: string): string {
    if (status === 'ACTIVE')  return 'badge-green';
    if (status === 'FUNDED')  return 'badge-green';
    if (status === 'CLOSED')  return 'badge-grey';
    if (status === 'DRAFT')   return 'badge-amber';
    return 'badge-grey';
  }

  // ── Audit log ─────────────────────────────────────────────────────

  loadAuditLog(page = 0): void {
    this.loadingAudit = true;
    this.auditPage = page;
    this.adminService.getAuditLog(page, 50).subscribe({
      next: (data) => {
        this.auditEvents = data.content ?? data;
        this.auditTotalPages = data.totalPages ?? 1;
        this.loadingAudit = false;
      },
      error: () => { this.loadingAudit = false; }
    });
  }

  // ── Reports ───────────────────────────────────────────────────────

  loadReportData(): void {
    this.loadingReports = true;
    this.adminService.getCashFlow(6).subscribe({
      next: (data) => { this.cashFlowData = data; },
      error: () => {}
    });
    this.adminService.getPortfolioReport().subscribe({
      next: (data) => { this.portfolioData = data; this.loadingReports = false; },
      error: () => { this.loadingReports = false; }
    });
  }

  // ── Admin Children list ───────────────────────────────────────────

  loadAdminChildren(): void {
    this.loadingAdminChildren = true;
    this.selectedChildDetail = null;
    this.adminService.listAdminChildren().subscribe({
      next: (data) => { this.adminChildren = data; this.loadingAdminChildren = false; },
      error: () => { this.loadingAdminChildren = false; }
    });
  }

  get filteredAdminChildren(): any[] {
    const q = this.adminChildSearch.toLowerCase().trim();
    if (!q) return this.adminChildren;
    return this.adminChildren.filter((c: any) =>
      c.fullName.toLowerCase().includes(q) || c.rollNumber.toLowerCase().includes(q)
    );
  }

  openChildDetail(id: string): void {
    this.loadingChildDetail = true;
    this.selectedChildDetail = null;
    this.adminService.getAdminChildDetail(id).subscribe({
      next: (data) => { this.selectedChildDetail = data; this.loadingChildDetail = false; },
      error: () => { this.loadingChildDetail = false; }
    });
  }

  closeChildDetail(): void { this.selectedChildDetail = null; }

  downloadChildPdf(childId: string): void {
    this.adminService.exportChildReport(childId).subscribe({
      next: (blob) => this.triggerDownload(blob, 'student-report.pdf'),
      error: () => this.showToast('Failed to generate PDF report.')
    });
  }

  // ── Bulk import ───────────────────────────────────────────────────

  onImportFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.importFile = input?.files?.[0] ?? null;
    this.importResult = null;
    this.importError = null;
  }

  submitImport(): void {
    if (!this.importFile || this.importLoading) return;
    this.importLoading = true;
    this.importError = null;
    this.importResult = null;
    this.adminService.importChildren(this.importFile).subscribe({
      next: (result) => {
        this.importResult = result;
        this.importLoading = false;
        this.importFile = null;
        if (result.importedRows > 0) this.loadDropdowns();
        this.showToast(`Import complete: ${result.importedRows} imported, ${result.failedRows + result.skippedMissingName + result.skippedMissingCampus + result.skippedDuplicateRollNumber} skipped.`);
      },
      error: (err) => {
        this.importLoading = false;
        this.importError = err?.error?.message ?? 'Import failed.';
      }
    });
  }

  downloadImportTemplate(): void {
    window.location.href = this.adminService.downloadImportTemplate();
  }

  // ── Exports ───────────────────────────────────────────────────────

  exportChildren(): void {
    this.adminService.exportChildren().subscribe({
      next: (blob) => this.triggerDownload(blob, 'children.xlsx'),
      error: () => this.showToast('Export failed.')
    });
  }

  exportDonations(): void {
    this.adminService.exportDonations().subscribe({
      next: (blob) => this.triggerDownload(blob, 'donations.xlsx'),
      error: () => this.showToast('Export failed.')
    });
  }

  exportReconciliation(): void {
    const filename = `reconciliation-${this.reconYear}-${String(this.reconMonth).padStart(2, '0')}.xlsx`;
    this.adminService.exportReconciliation(this.reconYear, this.reconMonth).subscribe({
      next: (blob) => this.triggerDownload(blob, filename),
      error: () => this.showToast('Export failed.')
    });
  }

  private triggerDownload(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }
}
