import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type UserRole = 'JJT_ADMIN' | 'ORG_ADMIN' | 'SPONSOR';

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private roleSubject = new BehaviorSubject<UserRole>(this.getStoredRole());
  role$ = this.roleSubject.asObservable();

  private getStoredRole(): UserRole {
    const stored = localStorage.getItem('userRole');
    return (stored as UserRole) || 'ORG_ADMIN';
  }

  setRole(role: UserRole) {
    localStorage.setItem('userRole', role);
    this.roleSubject.next(role);
  }

  getRole(): UserRole {
    return this.roleSubject.value;
  }

  hasStoredRole(): boolean {
    return !!localStorage.getItem('userRole');
  }

  clearRole() {
    localStorage.removeItem('userRole');
    // Default back to ORG_ADMIN so existing flows keep a role header
    this.roleSubject.next('ORG_ADMIN');
  }
}
