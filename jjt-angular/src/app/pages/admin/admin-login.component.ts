import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { CardModule } from 'primeng/card';
import { DropdownModule } from 'primeng/dropdown';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { RoleService, UserRole } from '../../services/role.service';

interface RoleOption {
  label: string;
  value: UserRole;
  description: string;
}

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, CardModule, DropdownModule, ButtonModule, TagModule],
  templateUrl: './admin-login.component.html',
  styleUrl: './admin-login.component.css'
})
export class AdminLoginComponent {
  roleOptions: RoleOption[] = [
    { label: 'JJT Admin', value: 'JJT_ADMIN', description: 'Platform-wide oversight, reports, and configuration.' },
    { label: 'Org Admin', value: 'ORG_ADMIN', description: 'Manage children, sponsors, and sponsorship intents for one org.' },
    { label: 'Sponsor (Read)', value: 'SPONSOR', description: 'Read-only visibility into committed children.' }
  ];

  selectedRole: UserRole = this.roleOptions[1].value;
  note = 'Mock login: role selection is stored in localStorage; no backend calls or auth.';

  constructor(
    private roleService: RoleService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  submit() {
    this.roleService.setRole(this.selectedRole);
    const redirect = this.route.snapshot.queryParamMap.get('redirect');
    this.router.navigate([redirect || '/admin/dashboard']);
  }
}
