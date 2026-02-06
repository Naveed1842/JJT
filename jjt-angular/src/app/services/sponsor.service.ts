import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type AvailabilityStatus = 'AVAILABLE' | 'RESERVED' | 'ALLOCATED';
export type CommitmentType = 'MONTHLY' | 'YEARLY';

export interface ChildDto {
  id: string;
  fullName: string;
  educationAmount: string;
  educationCurrency: string;
  availabilityStatus: AvailabilityStatus;
}

export interface LedgerEntryDto {
  id: string;
  month: string;
  educationAmount: string;
  educationCurrency: string;
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

  constructor(private http: HttpClient) {}

  getChildren(): Observable<ChildDto[]> {
    return this.http.get<ChildDto[]>(`${this.baseUrl}/api/org/children`);
  }

  getChild(childId: string): Observable<ChildDto> {
    return this.http.get<ChildDto>(`${this.baseUrl}/api/org/children/${childId}`);
  }

  getLedger(childId: string): Observable<LedgerDto> {
    return this.http.get<LedgerDto>(`${this.baseUrl}/api/org/children/${childId}/ledger`);
  }

  getProgress(childId: string): Observable<ProgressUpdateDto[]> {
    return this.http.get<ProgressUpdateDto[]>(`${this.baseUrl}/api/org/children/${childId}/progress`);
  }

  commitSponsorship(payload: SponsorshipCommitRequest): Observable<SponsorshipCommitResponse> {
    return this.http.post<SponsorshipCommitResponse>(
      `${this.baseUrl}/api/public/sponsorships`,
      payload
    );
  }
}
