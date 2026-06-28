import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AddProgressRequest,
  AddProgressResponse,
  ChildDto,
  CommitSponsorshipRequest,
  CommitSponsorshipResponse,
  CreateChildRequest,
  CreateChildResponse,
  CreateOrgAdminUserRequest,
  CreateSponsorRequest,
  CreateSponsorResponse,
  CreateSponsorUserRequest,
  LedgerDto,
  ProgressUpdateDto,
  RecordEarlySupportRequest,
  RecordEarlySupportResponse,
  SponsorshipSummaryResponse,
  SponsorshipStatus,
  UserResponse,
} from './api.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  // ── Children ──────────────────────────────────────────────────────────────

  createChild(req: CreateChildRequest): Observable<CreateChildResponse> {
    return this.http.post<CreateChildResponse>(`${this.base}/api/admin/children`, req);
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
      `${this.base}/api/admin/sponsorships?status=${status}`
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
}
