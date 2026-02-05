import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type UserRole = 'ADMIN' | 'ORG' | 'SPONSOR';

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private roleSubject = new BehaviorSubject<UserRole>(this.getStoredRole());
  role$ = this.roleSubject.asObservable();

  private getStoredRole(): UserRole {
    const stored = localStorage.getItem('userRole');
    return (stored as UserRole) || 'ORG';
  }

  setRole(role: UserRole) {
    localStorage.setItem('userRole', role);
    this.roleSubject.next(role);
  }

  getRole(): UserRole {
    return this.roleSubject.value;
  }
}
