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
