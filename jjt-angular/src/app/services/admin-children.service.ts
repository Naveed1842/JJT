import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ChildDto } from './sponsor.service';

@Injectable({ providedIn: 'root' })
export class AdminChildrenService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getChildren(): Observable<ChildDto[]> {
    return this.http.get<ChildDto[]>(`${this.baseUrl}/api/org/children`);
  }
}
