import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AdminSponsor {
  id: string;
  name: string;
  email?: string;
  contactEmail?: string;
  phone?: string;
  committedChildren?: string[];
  children?: string[];
}

@Injectable({ providedIn: 'root' })
export class AdminSponsorsService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getSponsors(): Observable<AdminSponsor[]> {
    return this.http.get<AdminSponsor[]>(`${this.baseUrl}/api/admin/sponsors`).pipe(
      // Normalize potential contactEmail -> email so the UI stays simple.
      // This is a light client-side mapping; no contract change.
      map(list => list.map(s => ({
        ...s,
        email: s.email || s.contactEmail
      })))
    );
  }
}
