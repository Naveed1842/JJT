import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ChildDto,
  LedgerDto,
  ProgressUpdateDto,
  PublicSponsorshipRequest,
  PublicSponsorshipResponse,
} from './api.models';

// Re-export types still imported by existing page components
export type { AvailabilityStatus, CommitmentType } from './api.models';
export type { ChildDto, LedgerDto, LedgerEntryDto, ProgressUpdateDto,
              PublicSponsorInfo, PublicSponsorshipRequest, PublicSponsorshipResponse
            } from './api.models';

@Injectable({ providedIn: 'root' })
export class SponsorService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  // ── Org read-only endpoints (public after SecurityConfig permit) ───────────
  // Used by the public browsing pages: /children, /children/:id, /children/:id/sponsor

  getChildren(): Observable<ChildDto[]> {
    return this.http.get<ChildDto[]>(`${this.base}/api/org/children`);
  }

  getChild(childId: string): Observable<ChildDto> {
    return this.http.get<ChildDto>(`${this.base}/api/org/children/${childId}`);
  }

  getLedger(childId: string): Observable<LedgerDto> {
    return this.http.get<LedgerDto>(`${this.base}/api/org/children/${childId}/ledger`);
  }

  getProgress(childId: string): Observable<ProgressUpdateDto[]> {
    return this.http.get<ProgressUpdateDto[]>(`${this.base}/api/org/children/${childId}/progress`);
  }

  // ── Sponsor portal endpoints (SPONSOR role, JWT required) ─────────────────
  // Used by /sponsor/portal — data scoped server-side to sponsorId from JWT

  getSponsorChildren(): Observable<ChildDto[]> {
    return this.http.get<ChildDto[]>(`${this.base}/api/sponsor/children`);
  }

  getSponsorChild(childId: string): Observable<ChildDto> {
    return this.http.get<ChildDto>(`${this.base}/api/sponsor/children/${childId}`);
  }

  getSponsorLedger(childId: string): Observable<LedgerDto> {
    return this.http.get<LedgerDto>(`${this.base}/api/sponsor/children/${childId}/ledger`);
  }

  getSponsorProgress(childId: string): Observable<ProgressUpdateDto[]> {
    return this.http.get<ProgressUpdateDto[]>(`${this.base}/api/sponsor/children/${childId}/progress`);
  }

  // ── Public endpoint (no auth) ─────────────────────────────────────────────

  commitSponsorship(payload: PublicSponsorshipRequest): Observable<PublicSponsorshipResponse> {
    return this.http.post<PublicSponsorshipResponse>(
      `${this.base}/api/public/sponsorships`,
      payload
    );
  }
}
