import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AccountCategoryResponse,
  AddProgressRequest,
  AddProgressResponse,
  AdminChildDetailResponse,
  AdminChildSummaryResponse,
  AlertResponse,
  ApprovalRequestResponse,
  ApprovalStatus,
  AuditEventResponse,
  BudgetResponse,
  BudgetVarianceRow,
  CampaignResponse,
  CashFlowReport,
  ChildDto,
  CostCentreResponse,
  CreateBudgetRequest,
  CreateCampaignRequest,
  CreateExpenseRequest,
  CreateFinancialPeriodRequest,
  CreateMissionNodeRequest,
  CreatePayrollRunRequest,
  CreatePersonRequest,
  CreateVendorRequest,
  DashboardResponse,
  ExecutiveSummaryResponse,
  ExpenseResponse,
  FinancialPeriodResponse,
  FinancialTransactionResponse,
  ImportChildrenResponse,
  MissionFinancialsResponse,
  MissionNodeResponse,
  PayrollItemResponse,
  PayrollRunResponse,
  PersonResponse,
  PortfolioReport,
  CommitSponsorshipRequest,
  CommitSponsorshipResponse,
  CreateChildRequest,
  CreateChildResponse,
  CreateDonorRequest,
  CreateOrgAdminUserRequest,
  CreateRecurringDonationRequest,
  CreateSponsorRequest,
  CreateSponsorResponse,
  CreateSponsorUserRequest,
  CreditFundRequest,
  DonationReceiptResponse,
  DonationResponse,
  DonationType,
  DonorResponse,
  FundAccountResponse,
  FundTransactionResponse,
  LedgerDto,
  MonthlyReconciliationResponse,
  OrgConfigResponse,
  PageResponse,
  PayrollProfileResponse,
  ProgressUpdateDto,
  RecordDonationRequest,
  RecordEarlySupportRequest,
  RecordEarlySupportResponse,
  RecordPaymentRequest,
  RecurringDonationResponse,
  SponsorPaymentResponse,
  SponsorshipSummaryResponse,
  SponsorshipStatus,
  TransparencyResponse,
  UpdateOrgConfigRequest,
  UpsertPayrollProfileRequest,
  UserResponse,
  VendorResponse,
  WaivePaymentRequest,
  ZakatStats,
} from './api.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  // ── Children ──────────────────────────────────────────────────────────────

  createChild(req: CreateChildRequest): Observable<CreateChildResponse> {
    return this.http.post<CreateChildResponse>(`${this.base}/api/admin/children`, req);
  }

  listAdminChildren(): Observable<AdminChildSummaryResponse[]> {
    return this.http.get<AdminChildSummaryResponse[]>(`${this.base}/api/admin/children/list`);
  }

  getAdminChildDetail(id: string): Observable<AdminChildDetailResponse> {
    return this.http.get<AdminChildDetailResponse>(`${this.base}/api/admin/children/${id}/detail`);
  }

  // ── Import ────────────────────────────────────────────────────────────────

  importChildren(file: File): Observable<ImportChildrenResponse> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ImportChildrenResponse>(`${this.base}/api/admin/children/import`, form);
  }

  downloadImportTemplate(): string {
    return `${this.base}/api/admin/children/import/template`;
  }

  // ── Export (authenticated blob downloads) ─────────────────────────────────

  exportChildren(): Observable<Blob> {
    return this.http.get(`${this.base}/api/admin/export/children.xlsx`, { responseType: 'blob' });
  }

  exportDonations(): Observable<Blob> {
    return this.http.get(`${this.base}/api/admin/export/donations.xlsx`, { responseType: 'blob' });
  }

  exportReconciliation(year: number, month: number): Observable<Blob> {
    return this.http.get(`${this.base}/api/admin/export/reconciliation/${year}/${month}.xlsx`, { responseType: 'blob' });
  }

  exportChildReport(childId: string): Observable<Blob> {
    return this.http.get(`${this.base}/api/admin/export/children/${childId}/report.pdf`, { responseType: 'blob' });
  }

  // ── Sponsors ──────────────────────────────────────────────────────────────

  createSponsor(req: CreateSponsorRequest): Observable<CreateSponsorResponse> {
    return this.http.post<CreateSponsorResponse>(`${this.base}/api/admin/sponsors`, req);
  }

  // ── Ledger: Early Support ─────────────────────────────────────────────────

  recordEarlySupport(req: RecordEarlySupportRequest): Observable<RecordEarlySupportResponse> {
    return this.http.post<RecordEarlySupportResponse>(`${this.base}/api/admin/early-support`, req);
  }

  // ── Progress updates ──────────────────────────────────────────────────────

  addProgress(childId: string, req: AddProgressRequest): Observable<AddProgressResponse> {
    return this.http.post<AddProgressResponse>(
      `${this.base}/api/admin/children/${childId}/progress`,
      req
    );
  }

  // ── Sponsorships ──────────────────────────────────────────────────────────

  commitSponsorship(req: CommitSponsorshipRequest): Observable<CommitSponsorshipResponse> {
    return this.http.post<CommitSponsorshipResponse>(`${this.base}/api/admin/sponsorships`, req);
  }

  listSponsorships(status: SponsorshipStatus = 'PENDING'): Observable<SponsorshipSummaryResponse[]> {
    return this.http.get<SponsorshipSummaryResponse[]>(
      `${this.base}/api/admin/sponsorships`, { params: { status } }
    );
  }

  activateSponsorship(sponsorshipId: string): Observable<CommitSponsorshipResponse> {
    return this.http.post<CommitSponsorshipResponse>(
      `${this.base}/api/admin/sponsorships/${sponsorshipId}/activate`,
      {}
    );
  }

  expireSponsorship(sponsorshipId: string): Observable<CommitSponsorshipResponse> {
    return this.http.post<CommitSponsorshipResponse>(
      `${this.base}/api/admin/sponsorships/${sponsorshipId}/expire`,
      {}
    );
  }

  listSponsorshipsByChild(childId: string): Observable<SponsorshipSummaryResponse[]> {
    return this.http.get<SponsorshipSummaryResponse[]>(
      `${this.base}/api/admin/children/${childId}/sponsorships`
    );
  }

  hasActiveSponsorship(childId: string): Observable<{ active: boolean }> {
    return this.http.get<{ active: boolean }>(
      `${this.base}/api/admin/children/${childId}/sponsorships/active`
    );
  }

  listSponsors(): Observable<CreateSponsorResponse[]> {
    return this.http.get<CreateSponsorResponse[]>(`${this.base}/api/admin/sponsors`);
  }

  // ── Org read ──────────────────────────────────────────────────────────────

  getOrgChildren(): Observable<ChildDto[]> {
    return this.http.get<ChildDto[]>(`${this.base}/api/org/children`);
  }

  getOrgChild(childId: string): Observable<ChildDto> {
    return this.http.get<ChildDto>(`${this.base}/api/org/children/${childId}`);
  }

  getOrgLedger(childId: string): Observable<LedgerDto> {
    return this.http.get<LedgerDto>(`${this.base}/api/org/children/${childId}/ledger`);
  }

  getOrgProgress(childId: string): Observable<ProgressUpdateDto[]> {
    return this.http.get<ProgressUpdateDto[]>(`${this.base}/api/org/children/${childId}/progress`);
  }

  // ── User management (JJT_ADMIN only) ─────────────────────────────────────

  listUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.base}/api/admin/users`);
  }

  createSponsorUser(req: CreateSponsorUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.base}/api/admin/users/sponsor`, req);
  }

  createOrgAdminUser(req: CreateOrgAdminUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.base}/api/admin/users/org`, req);
  }

  activateUser(userId: string): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.base}/api/admin/users/${userId}/activate`, {});
  }

  deactivateUser(userId: string): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.base}/api/admin/users/${userId}/deactivate`, {});
  }

  // ── Fund accounts ──────────────────────────────────────────────────────────

  listFundAccounts(): Observable<FundAccountResponse[]> {
    return this.http.get<FundAccountResponse[]>(`${this.base}/api/admin/funds`);
  }

  getFundBalance(fundId: string): Observable<FundAccountResponse> {
    return this.http.get<FundAccountResponse>(`${this.base}/api/admin/funds/${fundId}/balance`);
  }

  getFundTransactions(fundId: string, page = 0): Observable<PageResponse<FundTransactionResponse>> {
    return this.http.get<PageResponse<FundTransactionResponse>>(
      `${this.base}/api/admin/funds/${fundId}/transactions`, { params: { page, size: 20 } }
    );
  }

  creditFund(fundId: string, req: CreditFundRequest): Observable<FundTransactionResponse> {
    return this.http.post<FundTransactionResponse>(`${this.base}/api/admin/funds/${fundId}/credit`, req);
  }

  // ── Payments & reconciliation ──────────────────────────────────────────────

  getMonthlyReconciliation(year: number, month: number): Observable<MonthlyReconciliationResponse> {
    return this.http.get<MonthlyReconciliationResponse>(
      `${this.base}/api/admin/reconciliation/monthly`, { params: { year, month } }
    );
  }

  receivePayment(paymentId: string, req: RecordPaymentRequest): Observable<SponsorPaymentResponse> {
    return this.http.post<SponsorPaymentResponse>(
      `${this.base}/api/admin/payments/${paymentId}/receive`,
      req
    );
  }

  waivePayment(paymentId: string, req: WaivePaymentRequest): Observable<SponsorPaymentResponse> {
    return this.http.post<SponsorPaymentResponse>(
      `${this.base}/api/admin/payments/${paymentId}/waive`,
      req
    );
  }

  getPaymentsBySponsorship(sponsorshipId: string): Observable<SponsorPaymentResponse[]> {
    return this.http.get<SponsorPaymentResponse[]>(
      `${this.base}/api/admin/sponsorships/${sponsorshipId}/payments`
    );
  }

  generatePayments(month?: string): Observable<{ month: string; created: number }> {
    return this.http.post<{ month: string; created: number }>(
      `${this.base}/api/admin/payments/generate`,
      {},
      { params: month ? { month } : {} }
    );
  }

  // ── Alerts ─────────────────────────────────────────────────────────────────

  listAlerts(): Observable<AlertResponse[]> {
    return this.http.get<AlertResponse[]>(`${this.base}/api/admin/alerts`);
  }

  dismissAlert(id: string): Observable<AlertResponse> {
    return this.http.post<AlertResponse>(`${this.base}/api/admin/alerts/${id}/dismiss`, {});
  }

  // ── Org config ─────────────────────────────────────────────────────────────

  getOrgConfig(): Observable<OrgConfigResponse> {
    return this.http.get<OrgConfigResponse>(`${this.base}/api/admin/org/config`);
  }

  updateOrgConfig(req: UpdateOrgConfigRequest): Observable<OrgConfigResponse> {
    return this.http.patch<OrgConfigResponse>(`${this.base}/api/admin/org/config`, req);
  }

  // ── Change password (via auth endpoint) ────────────────────────────────────

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.put<void>(`${this.base}/api/auth/change-password`, { currentPassword, newPassword });
  }

  // ── Donors ─────────────────────────────────────────────────────────────────

  createDonor(req: CreateDonorRequest): Observable<DonorResponse> {
    return this.http.post<DonorResponse>(`${this.base}/api/admin/donors`, req);
  }

  listDonors(): Observable<DonorResponse[]> {
    return this.http.get<DonorResponse[]>(`${this.base}/api/admin/donors`);
  }

  getDonor(id: string): Observable<DonorResponse> {
    return this.http.get<DonorResponse>(`${this.base}/api/admin/donors/${id}`);
  }

  // ── Donations ──────────────────────────────────────────────────────────────

  recordDonation(req: RecordDonationRequest): Observable<DonationResponse> {
    return this.http.post<DonationResponse>(`${this.base}/api/admin/donations`, req);
  }

  listDonations(page = 0, type?: DonationType): Observable<PageResponse<DonationResponse>> {
    return this.http.get<PageResponse<DonationResponse>>(
      `${this.base}/api/admin/donations`,
      { params: type ? { page, size: 20, type } : { page, size: 20 } }
    );
  }

  getZakatStats(): Observable<ZakatStats> {
    return this.http.get<ZakatStats>(`${this.base}/api/admin/donations/zakat-stats`);
  }

  getDonation(id: string): Observable<DonationResponse> {
    return this.http.get<DonationResponse>(`${this.base}/api/admin/donations/${id}`);
  }

  getDonationReceipt(id: string): Observable<DonationReceiptResponse> {
    return this.http.get<DonationReceiptResponse>(`${this.base}/api/admin/donations/${id}/receipt`);
  }

  receiveDonation(id: string, actualAmount?: string): Observable<DonationResponse> {
    return this.http.post<DonationResponse>(
      `${this.base}/api/admin/donations/${id}/receive`,
      {},
      { params: actualAmount ? { actualAmount } : {} }
    );
  }

  reverseDonation(id: string): Observable<DonationResponse> {
    return this.http.post<DonationResponse>(`${this.base}/api/admin/donations/${id}/reverse`, {});
  }

  // ── Recurring donations ────────────────────────────────────────────────────

  createRecurringDonation(req: CreateRecurringDonationRequest): Observable<RecurringDonationResponse> {
    return this.http.post<RecurringDonationResponse>(`${this.base}/api/admin/donations/recurring`, req);
  }

  listRecurringDonations(): Observable<RecurringDonationResponse[]> {
    return this.http.get<RecurringDonationResponse[]>(`${this.base}/api/admin/donations/recurring`);
  }

  pauseRecurring(id: string): Observable<RecurringDonationResponse> {
    return this.http.patch<RecurringDonationResponse>(
      `${this.base}/api/admin/donations/recurring/${id}/pause`, {}
    );
  }

  cancelRecurring(id: string): Observable<RecurringDonationResponse> {
    return this.http.patch<RecurringDonationResponse>(
      `${this.base}/api/admin/donations/recurring/${id}/cancel`, {}
    );
  }

  generateRecurringDonations(): Observable<number> {
    return this.http.post<number>(`${this.base}/api/admin/donations/recurring/generate`, {});
  }

  // ── Dashboard ──────────────────────────────────────────────────────────────

  getDashboard(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${this.base}/api/admin/dashboard`);
  }

  // ── Reports ────────────────────────────────────────────────────────────────

  getCashFlow(months = 6): Observable<CashFlowReport> {
    return this.http.get<CashFlowReport>(
      `${this.base}/api/admin/reports/cash-flow`, { params: { months } }
    );
  }

  getPortfolioReport(): Observable<PortfolioReport> {
    return this.http.get<PortfolioReport>(`${this.base}/api/admin/reports/portfolio`);
  }

  // ── Campaigns ──────────────────────────────────────────────────────────────

  listCampaigns(): Observable<CampaignResponse[]> {
    return this.http.get<CampaignResponse[]>(`${this.base}/api/admin/campaigns`);
  }

  createCampaign(req: CreateCampaignRequest): Observable<CampaignResponse> {
    return this.http.post<CampaignResponse>(`${this.base}/api/admin/campaigns`, req);
  }

  openCampaign(id: string): Observable<CampaignResponse> {
    return this.http.post<CampaignResponse>(`${this.base}/api/admin/campaigns/${id}/open`, {});
  }

  closeCampaign(id: string): Observable<CampaignResponse> {
    return this.http.post<CampaignResponse>(`${this.base}/api/admin/campaigns/${id}/close`, {});
  }

  // ── Audit log ──────────────────────────────────────────────────────────────

  getAuditLog(page = 0, size = 50): Observable<PageResponse<AuditEventResponse>> {
    return this.http.get<PageResponse<AuditEventResponse>>(
      `${this.base}/api/admin/audit-log`, { params: { page, size } }
    );
  }

  // ── R5: Account categories ─────────────────────────────────────────────────

  listAccountCategories(): Observable<AccountCategoryResponse[]> {
    return this.http.get<AccountCategoryResponse[]>(`${this.base}/api/admin/finance/categories`);
  }

  // ── R5: Cost centres ───────────────────────────────────────────────────────

  listCostCentres(): Observable<CostCentreResponse[]> {
    return this.http.get<CostCentreResponse[]>(`${this.base}/api/admin/finance/cost-centres`);
  }

  // ── R5: Financial periods ──────────────────────────────────────────────────

  listFinancialPeriods(): Observable<FinancialPeriodResponse[]> {
    return this.http.get<FinancialPeriodResponse[]>(`${this.base}/api/admin/finance/periods`);
  }

  createFinancialPeriod(req: CreateFinancialPeriodRequest): Observable<FinancialPeriodResponse> {
    return this.http.post<FinancialPeriodResponse>(`${this.base}/api/admin/finance/periods`, req);
  }

  closePeriod(id: string): Observable<FinancialPeriodResponse> {
    return this.http.post<FinancialPeriodResponse>(`${this.base}/api/admin/finance/periods/${id}/close`, {});
  }

  lockPeriod(id: string): Observable<FinancialPeriodResponse> {
    return this.http.post<FinancialPeriodResponse>(`${this.base}/api/admin/finance/periods/${id}/lock`, {});
  }

  // ── R5: Vendors ────────────────────────────────────────────────────────────

  listVendors(): Observable<VendorResponse[]> {
    return this.http.get<VendorResponse[]>(`${this.base}/api/admin/finance/vendors`);
  }

  createVendor(req: CreateVendorRequest): Observable<VendorResponse> {
    return this.http.post<VendorResponse>(`${this.base}/api/admin/finance/vendors`, req);
  }

  deactivateVendor(id: string): Observable<VendorResponse> {
    return this.http.post<VendorResponse>(`${this.base}/api/admin/finance/vendors/${id}/deactivate`, {});
  }

  // ── R5: Expenses ───────────────────────────────────────────────────────────

  listExpenses(status?: string): Observable<ExpenseResponse[]> {
    const params: any = {};
    if (status) params['status'] = status;
    return this.http.get<ExpenseResponse[]>(`${this.base}/api/admin/finance/expenses`, { params });
  }

  createExpense(req: CreateExpenseRequest): Observable<ExpenseResponse> {
    return this.http.post<ExpenseResponse>(`${this.base}/api/admin/finance/expenses`, req);
  }

  submitExpense(id: string): Observable<ExpenseResponse> {
    return this.http.post<ExpenseResponse>(`${this.base}/api/admin/finance/expenses/${id}/submit`, {});
  }

  approveExpense(id: string, notes?: string): Observable<ExpenseResponse> {
    return this.http.post<ExpenseResponse>(`${this.base}/api/admin/finance/expenses/${id}/approve`, { notes });
  }

  rejectExpense(id: string, notes?: string): Observable<ExpenseResponse> {
    return this.http.post<ExpenseResponse>(`${this.base}/api/admin/finance/expenses/${id}/reject`, { notes });
  }

  payExpense(id: string, paymentMethod: string): Observable<ExpenseResponse> {
    return this.http.post<ExpenseResponse>(`${this.base}/api/admin/finance/expenses/${id}/pay`, { paymentMethod });
  }

  voidExpense(id: string): Observable<ExpenseResponse> {
    return this.http.post<ExpenseResponse>(`${this.base}/api/admin/finance/expenses/${id}/void`, {});
  }

  // ── R5: Approvals ──────────────────────────────────────────────────────────

  listApprovals(status?: ApprovalStatus): Observable<ApprovalRequestResponse[]> {
    const params: any = {};
    if (status) params['status'] = status;
    return this.http.get<ApprovalRequestResponse[]>(`${this.base}/api/admin/approvals`, { params });
  }

  getApproval(id: string): Observable<ApprovalRequestResponse> {
    return this.http.get<ApprovalRequestResponse>(`${this.base}/api/admin/approvals/${id}`);
  }

  resolveApproval(id: string, decision: 'APPROVED' | 'REJECTED', notes?: string): Observable<ApprovalRequestResponse> {
    return this.http.post<ApprovalRequestResponse>(`${this.base}/api/admin/approvals/${id}/resolve`, { decision, notes });
  }

  // ── R5: Financial transactions ─────────────────────────────────────────────

  listFinancialTransactions(periodId?: string): Observable<FinancialTransactionResponse[]> {
    const params: any = {};
    if (periodId) params['periodId'] = periodId;
    return this.http.get<FinancialTransactionResponse[]>(`${this.base}/api/admin/finance/transactions`, { params });
  }

  // ── R5: Budgets ────────────────────────────────────────────────────────────

  listBudgets(periodId?: string): Observable<BudgetResponse[]> {
    const params: any = {};
    if (periodId) params['periodId'] = periodId;
    return this.http.get<BudgetResponse[]>(`${this.base}/api/admin/finance/budgets`, { params });
  }

  createBudget(req: CreateBudgetRequest): Observable<BudgetResponse> {
    return this.http.post<BudgetResponse>(`${this.base}/api/admin/finance/budgets`, req);
  }

  getBudgetVariance(periodId: string): Observable<BudgetVarianceRow[]> {
    return this.http.get<BudgetVarianceRow[]>(
      `${this.base}/api/admin/finance/budgets/variance`, { params: { periodId } }
    );
  }

  // ── R5: People ─────────────────────────────────────────────────────────────

  listPeople(): Observable<PersonResponse[]> {
    return this.http.get<PersonResponse[]>(`${this.base}/api/admin/people`);
  }

  createPerson(req: CreatePersonRequest): Observable<PersonResponse> {
    return this.http.post<PersonResponse>(`${this.base}/api/admin/people`, req);
  }

  getPersonPayrollProfile(personId: string): Observable<PayrollProfileResponse> {
    return this.http.get<PayrollProfileResponse>(`${this.base}/api/admin/people/${personId}/payroll`);
  }

  upsertPayrollProfile(personId: string, req: UpsertPayrollProfileRequest): Observable<PayrollProfileResponse> {
    return this.http.put<PayrollProfileResponse>(`${this.base}/api/admin/people/${personId}/payroll`, req);
  }

  // ── R5: Payroll runs ───────────────────────────────────────────────────────

  listPayrollRuns(): Observable<PayrollRunResponse[]> {
    return this.http.get<PayrollRunResponse[]>(`${this.base}/api/admin/finance/payroll/runs`);
  }

  createPayrollRun(req: CreatePayrollRunRequest): Observable<PayrollRunResponse> {
    return this.http.post<PayrollRunResponse>(`${this.base}/api/admin/finance/payroll/runs`, req);
  }

  getPayrollItems(runId: string): Observable<PayrollItemResponse[]> {
    return this.http.get<PayrollItemResponse[]>(`${this.base}/api/admin/finance/payroll/runs/${runId}/items`);
  }

  processPayrollRun(runId: string): Observable<PayrollRunResponse> {
    return this.http.post<PayrollRunResponse>(`${this.base}/api/admin/finance/payroll/runs/${runId}/process`, {});
  }

  // ── R5: Mission nodes ──────────────────────────────────────────────────────

  listMissionRoots(): Observable<MissionNodeResponse[]> {
    return this.http.get<MissionNodeResponse[]>(`${this.base}/api/admin/missions`);
  }

  getMissionNode(id: string): Observable<MissionNodeResponse> {
    return this.http.get<MissionNodeResponse>(`${this.base}/api/admin/missions/${id}`);
  }

  getMissionChildren(id: string): Observable<MissionNodeResponse[]> {
    return this.http.get<MissionNodeResponse[]>(`${this.base}/api/admin/missions/${id}/children`);
  }

  createMissionNode(req: CreateMissionNodeRequest): Observable<MissionNodeResponse> {
    return this.http.post<MissionNodeResponse>(`${this.base}/api/admin/missions`, req);
  }

  getMissionFinancials(id: string): Observable<MissionFinancialsResponse> {
    return this.http.get<MissionFinancialsResponse>(`${this.base}/api/admin/missions/${id}/financials`);
  }

  // ── R5: Executive summary ──────────────────────────────────────────────────

  getExecutiveSummary(): Observable<ExecutiveSummaryResponse> {
    return this.http.get<ExecutiveSummaryResponse>(`${this.base}/api/admin/finance/executive/summary`);
  }

  // ── R5: Public transparency ────────────────────────────────────────────────

  getPublicTransparency(): Observable<TransparencyResponse> {
    return this.http.get<TransparencyResponse>(`${this.base}/api/public/transparency`);
  }
}
