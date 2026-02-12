import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export type AvailabilityStatus = 'AVAILABLE' | 'RESERVED' | 'ALLOCATED';
export type CommitmentType = 'MONTHLY' | 'YEARLY';

export interface PublicEducationCost {
  amount: number;
  currency: string;
}

export interface PublicChildDto {
  id: string;
  fullName: string;
  city: string;
  educationCost: PublicEducationCost;
  availabilityStatus: AvailabilityStatus;
}

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
  private cachedPublicChildren: PublicChildDto[] | null = null;
  private readonly orgHeaders = new HttpHeaders({
    'X-ROLE': 'ORG_ADMIN'
  });

  constructor(private http: HttpClient) {}

  // Public landing page children list (backend contract)
  getPublicChildren(): Observable<PublicChildDto[]> {
    return this.http.get<PublicChildDto[]>(`${this.baseUrl}/api/public/children`).pipe(
      tap((children) => {
        this.cachedPublicChildren = children;
      })
    );
  }

  // Reuse cached list if available, otherwise call public child endpoint
  getPublicChild(childId: string): Observable<PublicChildDto> {
    const cached = this.cachedPublicChildren?.find((child) => child.id === childId);
    if (cached) {
      return of(cached);
    }
    return this.http.get<PublicChildDto>(`${this.baseUrl}/api/public/children/${childId}`);
  }

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
