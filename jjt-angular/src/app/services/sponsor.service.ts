import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type AvailabilityStatus = 'AVAILABLE' | 'RESERVED' | 'ALLOCATED';
export type CommitmentType = 'MONTHLY' | 'YEARLY';

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

export interface LedgerEntryDto {
  id: string;
  month: string;
  educationAmount: string;
  educationCurrency: string;
  coverageType: 'EARLY_SUPPORT' | 'SPONSOR';
}

export interface LedgerDto {
  childId: string;
  entries: LedgerEntryDto[];
}

export interface ProgressUpdateDto {
  id: string;
  month: string;
  summary: string;
}

export interface SponsorshipCommitRequest {
  childId: string;
  commitmentType: CommitmentType;
  sponsor: PublicSponsorInfo;
}

export interface PublicSponsorInfo {
  name: string;
  email: string;
  phone?: string | null;
}

export interface SponsorshipCommitResponse {
  childId: string;
  startMonth: string;
}

@Injectable({ providedIn: 'root' })
export class SponsorService {
  private readonly baseUrl = environment.apiBaseUrl;
  private readonly orgHeaders = new HttpHeaders({
    'X-ROLE': 'ORG_ADMIN'
  });

  constructor(private http: HttpClient) {}

  getChildren(): Observable<ChildDto[]> {
    return this.http.get<ChildDto[]>(`${this.baseUrl}/api/org/children`, {
      headers: this.orgHeaders
    });
  }

  getChild(childId: string): Observable<ChildDto> {
    return this.http.get<ChildDto>(`${this.baseUrl}/api/org/children/${childId}`, {
      headers: this.orgHeaders
    });
  }

  getLedger(childId: string): Observable<LedgerDto> {
    return this.http.get<LedgerDto>(`${this.baseUrl}/api/org/children/${childId}/ledger`, {
      headers: this.orgHeaders
    });
  }

  getProgress(childId: string): Observable<ProgressUpdateDto[]> {
    return this.http.get<ProgressUpdateDto[]>(`${this.baseUrl}/api/org/children/${childId}/progress`, {
      headers: this.orgHeaders
    });
  }

  commitSponsorship(payload: SponsorshipCommitRequest): Observable<SponsorshipCommitResponse> {
    return this.http.post<SponsorshipCommitResponse>(
      `${this.baseUrl}/api/public/sponsorships`,
      payload
    );
  }
}
