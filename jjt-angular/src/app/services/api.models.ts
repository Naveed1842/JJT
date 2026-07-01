// ---------------------------------------------------------------------------
// API Models — TypeScript interfaces that match the Spring Boot DTOs exactly.
// Field names mirror the JSON property names returned by the backend.
// ---------------------------------------------------------------------------

export type AvailabilityStatus = 'AVAILABLE' | 'RESERVED' | 'ALLOCATED';
export type CommitmentType    = 'MONTHLY' | 'YEARLY';
export type SponsorshipStatus = 'PENDING' | 'ACTIVE' | 'EXPIRED';
export type CoverageType      = 'EARLY_SUPPORT' | 'SPONSOR';
export type UserRole          = 'JJT_ADMIN' | 'ORG_ADMIN' | 'SPONSOR';

// ---- Children ---------------------------------------------------------------

export interface ChildDto {
  id: string;
  rollNumber: string;
  fullName: string;
  city: string;
  campusName: string;
  schoolName: string | null;
  educationAmount: string;
  educationCurrency: string;
  availabilityStatus: AvailabilityStatus;
}

// ---- Ledger -----------------------------------------------------------------

export interface LedgerEntryDto {
  id: string;
  month: string;           // YYYY-MM
  educationAmount: string;
  educationCurrency: string;
  coverageType: CoverageType;
}

export interface LedgerDto {
  childId: string;
  entries: LedgerEntryDto[];
}

// ---- Progress ---------------------------------------------------------------

export interface ProgressUpdateDto {
  id: string;
  month: string;   // YYYY-MM
  summary: string;
}

// ---- Sponsorships -----------------------------------------------------------

export interface SponsorshipSummaryResponse {
  sponsorshipId: string;
  childId: string;
  sponsorId: string;
  sponsorName: string;
  sponsorEmail: string;
  sponsorPhone: string | null;
  commitmentType: CommitmentType;
  startMonth: string;   // YYYY-MM
  status: SponsorshipStatus;
  createdAt: string;    // ISO-8601 instant
}

// ---- Admin requests ---------------------------------------------------------

export interface CreateChildRequest {
  rollNumber: string;
  fullName: string;
  city: string;
  campusName: string;
  schoolName: string | null;
  educationAmount: string;
  educationCurrency: string;
  childId: string;
  ledgerId: string;
}

export interface CreateChildResponse {
  childId: string;
  ledgerId: string;
}

export interface CreateSponsorRequest {
  sponsorId: string;
  displayName: string;
  contactEmail: string;
  phone?: string | null;
}

export interface CreateSponsorResponse {
  sponsorId: string;
  displayName: string;
  contactEmail: string;
}

export interface RecordEarlySupportRequest {
  childId: string;
  month: string;            // YYYY-MM
  educationAmount: string;
  educationCurrency: string;
  ledgerEntryId?: string | null;
}

export interface RecordEarlySupportResponse {
  ledgerEntryId: string;
  ledgerId: string;
  month: string;
}

export interface AddProgressRequest {
  month: string;    // YYYY-MM
  summary: string;
  progressUpdateId?: string | null;
}

export interface AddProgressResponse {
  progressUpdateId: string;
  childId: string;
  month: string;
}

export interface CommitSponsorshipRequest {
  sponsorId: string;
  childId: string;
  startMonth: string;   // YYYY-MM
  commitmentType: CommitmentType;
  sponsorshipId?: string | null;
}

export interface CommitSponsorshipResponse {
  sponsorshipId: string;
  sponsorId: string;
  childId: string;
  startMonth: string;
}

// ---- Public endpoints -------------------------------------------------------

export interface PublicSponsorInfo {
  name: string;
  email: string;
  phone?: string | null;
}

export interface PublicSponsorshipRequest {
  childId: string;
  commitmentType: CommitmentType;
  sponsor: PublicSponsorInfo;
}

export interface PublicSponsorshipResponse {
  childId: string;
  startMonth: string;   // YYYY-MM
}

// ---- User management --------------------------------------------------------

export interface UserResponse {
  id: string;
  email: string;
  role: UserRole;
  sponsorId: string | null;
  orgId: string | null;
  active: boolean;
  createdAt: string;
  lastLoginAt: string | null;
}

export interface CreateSponsorUserRequest {
  sponsorId: string;
  email: string;
  password: string;
}

export interface CreateOrgAdminUserRequest {
  email: string;
  password: string;
  orgId?: string | null;
}

// ---- Auth -------------------------------------------------------------------

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

export interface CurrentUserResponse {
  id: string;
  email: string;
  role: UserRole;
  sponsorId: string | null;
  orgId: string | null;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

// ---- Error ------------------------------------------------------------------

export interface ApiError {
  code: string;
  message: string;
}

// ---- Fund accounts ----------------------------------------------------------

export interface FundAccountResponse {
  id: string;
  name: string;
  currency: string;
  balance: string;
  minReserve: string;
  isBelowMinReserve: boolean;
}

export interface FundTransactionResponse {
  id: string;
  fundAccountId: string;
  transactionType: 'CREDIT' | 'DEBIT';
  amount: string;
  currency: string;
  reason: string;
  description: string | null;
  externalReference: string | null;
  ledgerEntryId: string | null;
  createdBy: string;
  createdAt: string;
}

export interface CreditFundRequest {
  amount: string;
  currency: string;
  description: string;
  externalReference?: string | null;
}

// ---- Payments & Reconciliation -----------------------------------------------

export interface SponsorPaymentResponse {
  id: string;
  sponsorshipId: string;
  sponsorId: string;
  childId: string;
  paymentMonth: string;
  status: 'PENDING' | 'RECEIVED' | 'OVERDUE' | 'WAIVED' | 'PARTIAL';
  expectedAmount: string;
  expectedCurrency: string;
  receivedAmount: string | null;
  receivedCurrency: string | null;
  bankReference: string | null;
  receivedDate: string | null;
  waiverReason: string | null;
  fundTransactionId: string | null;
  ledgerEntryId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ReconciliationSummary {
  expected: number;
  received: number;
  partial: number;
  overdue: number;
  waived: number;
  total: number;
}

export interface AtRiskSponsorship {
  sponsorshipId: string;
  sponsorId: string;
  sponsorName: string;
  childId: string;
  childName: string;
  consecutiveOverdueMonths: number;
  requiresEscalation: boolean;
}

export interface MonthlyReconciliationResponse {
  year: number;
  month: number;
  summary: ReconciliationSummary;
  atRisk: AtRiskSponsorship[];
  payments: SponsorPaymentResponse[];
}

export interface RecordPaymentRequest {
  receivedAmount: string;
  currency: string;
  bankReference: string;
  receivedDate: string;
}

export interface WaivePaymentRequest {
  reason: string;
}

// ---- Alerts -----------------------------------------------------------------

export interface AlertResponse {
  id: string;
  alertType: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  title: string;
  message: string;
  relatedEntityId: string | null;
  relatedEntityType: string | null;
  dismissed: boolean;
  dismissedAt: string | null;
  createdAt: string;
}

// ---- Org config -------------------------------------------------------------

export interface OrgConfigResponse {
  id: string;
  name: string;
  slug: string;
  baseCurrency: string;
  paymentDueDay: number;
  minReserve: string;
  active: boolean;
  createdAt: string;
}

export interface UpdateOrgConfigRequest {
  name?: string | null;
  baseCurrency?: string | null;
  paymentDueDay?: number | null;
  minFundReserve?: string | null;
}

// ---- Spring Page wrapper ----------------------------------------------------

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
