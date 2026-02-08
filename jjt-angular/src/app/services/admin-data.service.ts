import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { CommitmentType } from './sponsor.service';

export type SponsorshipStatus = 'PENDING' | 'ACTIVE' | 'PAUSED';

export interface AdminChild {
  id: string;
  name: string;
  city: string;
  campus: string;
  school: string;
  educationAmount: number;
  currency: string;
  status: string;
  sponsor?: string;
  lastProgress?: string;
}

export interface AdminSponsor {
  id: string;
  name: string;
  email: string;
  phone: string;
  committedChildren: string[];
}

export interface AdminSponsorship {
  id: string;
  childId: string;
  childName: string;
  sponsorId: string;
  sponsorName: string;
  startMonth: string;
  commitmentType: CommitmentType;
  status: SponsorshipStatus;
}

export interface DashboardSummary {
  totalChildren: number;
  totalSponsors: number;
  activeSponsorships: number;
  pendingSponsorships: number;
  monthlyCoveragePct: number;
}

@Injectable({ providedIn: 'root' })
export class AdminDataService {
  private children: AdminChild[] = [
    { id: 'c-001', name: 'Amina Khan', city: 'Karachi', campus: 'Gulshan', school: 'Future Stars School', educationAmount: 2000, currency: 'PKR', status: 'ENROLLED', sponsor: 'Bright Futures Trust', lastProgress: 'Nov 2025' },
    { id: 'c-002', name: 'Rohan Ali', city: 'Lahore', campus: 'Johar Town', school: 'Iqra Academy', educationAmount: 1800, currency: 'PKR', status: 'PENDING', sponsor: undefined, lastProgress: 'Oct 2025' },
    { id: 'c-003', name: 'Sara Malik', city: 'Islamabad', campus: 'F-10', school: 'Capital School', educationAmount: 2200, currency: 'PKR', status: 'ENROLLED', sponsor: 'Elm Volunteers', lastProgress: 'Dec 2025' },
    { id: 'c-004', name: 'Hamza Tariq', city: 'Rawalpindi', campus: 'Saddar', school: 'Unity Public', educationAmount: 2100, currency: 'PKR', status: 'SUPPORT_PAUSED', sponsor: 'Bright Futures Trust', lastProgress: 'Aug 2025' }
  ];

  private sponsors: AdminSponsor[] = [
    { id: 's-001', name: 'Bright Futures Trust', email: 'contact@bft.org', phone: '+92 300 1112233', committedChildren: ['Amina Khan', 'Hamza Tariq'] },
    { id: 's-002', name: 'Elm Volunteers', email: 'hello@elm.org', phone: '+92 300 7778899', committedChildren: ['Sara Malik'] },
    { id: 's-003', name: 'Community Circle', email: 'circle@example.com', phone: '+92 301 4455667', committedChildren: [] }
  ];

  private sponsorships: AdminSponsorship[] = [
    { id: 'sp-001', childId: 'c-001', childName: 'Amina Khan', sponsorId: 's-001', sponsorName: 'Bright Futures Trust', startMonth: '2025-09', commitmentType: 'MONTHLY', status: 'ACTIVE' },
    { id: 'sp-002', childId: 'c-003', childName: 'Sara Malik', sponsorId: 's-002', sponsorName: 'Elm Volunteers', startMonth: '2025-10', commitmentType: 'YEARLY', status: 'ACTIVE' },
    { id: 'sp-003', childId: 'c-002', childName: 'Rohan Ali', sponsorId: 's-003', sponsorName: 'Community Circle', startMonth: '2026-02', commitmentType: 'MONTHLY', status: 'PENDING' },
    { id: 'sp-004', childId: 'c-004', childName: 'Hamza Tariq', sponsorId: 's-001', sponsorName: 'Bright Futures Trust', startMonth: '2025-07', commitmentType: 'MONTHLY', status: 'PAUSED' }
  ];

  getDashboardSummary(): Observable<DashboardSummary> {
    const active = this.sponsorships.filter(s => s.status === 'ACTIVE').length;
    const pending = this.sponsorships.filter(s => s.status === 'PENDING').length;
    const monthlyCoverage = Math.min(100, Math.round((active / Math.max(this.children.length, 1)) * 100));
    return of({
      totalChildren: this.children.length,
      totalSponsors: this.sponsors.length,
      activeSponsorships: active,
      pendingSponsorships: pending,
      monthlyCoveragePct: monthlyCoverage
    });
  }

  getCoverageTrend(): Observable<{ labels: string[]; data: number[] }> {
    return of({
      labels: ['Sep', 'Oct', 'Nov', 'Dec', 'Jan', 'Feb'],
      data: [35, 42, 50, 61, 63, 68]
    });
  }

  getChildren(): Observable<AdminChild[]> {
    return of(this.children);
  }

  getSponsors(): Observable<AdminSponsor[]> {
    return of(this.sponsors);
  }

  getSponsorships(): Observable<AdminSponsorship[]> {
    return of(this.sponsorships);
  }

  getReportRows(): Observable<Record<string, string>[]> {
    const rows = this.sponsorships.map(s => ({
      child: s.childName,
      sponsor: s.sponsorName,
      startMonth: s.startMonth,
      commitmentType: s.commitmentType,
      status: s.status
    }));
    return of(rows);
  }
}
