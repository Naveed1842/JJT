import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CommitmentType } from './sponsor.service';

export interface AdminSponsorship {
  id: string;
  childId: string;
  childName: string;
  sponsorId: string;
  sponsorName: string;
  startMonth: string;
  commitmentType?: CommitmentType;
  status?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminSponsorshipsService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getSponsorships(): Observable<AdminSponsorship[]> {
    return this.http.get<AdminSponsorship[]>(`${this.baseUrl}/api/admin/sponsorships`);
  }
}
