import { Component, OnInit, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { PanelMenuModule } from 'primeng/panelmenu';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { MenuItem } from 'primeng/api';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RoleService, UserRole } from '../../services/role.service';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, PanelMenuModule, ButtonModule, TagModule],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
  role: UserRole = 'ORG_ADMIN';
  menuItems: MenuItem[] = [];
  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private roleService: RoleService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.role = this.roleService.getRole();
    this.buildMenu(this.role);

    this.roleService.role$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(role => {
        this.role = role;
        this.buildMenu(role);
      });
  }

  buildMenu(role: UserRole) {
    const isSponsor = role === 'SPONSOR';
    const canManage = role !== 'SPONSOR';
    this.menuItems = [
      {
        label: 'Overview',
        items: [
          { label: 'Dashboard', icon: 'pi pi-chart-line', routerLink: '/admin/dashboard' }
        ]
      },
      {
        label: 'Operations',
        visible: canManage,
        items: [
          { label: 'Children', icon: 'pi pi-users', routerLink: '/admin/children' },
          { label: 'Sponsorships', icon: 'pi pi-briefcase', routerLink: '/admin/sponsorships' },
          { label: 'Sponsors', icon: 'pi pi-id-card', routerLink: '/admin/sponsors' }
        ]
      },
      {
        label: 'Insights',
        items: [
          { label: 'Reports', icon: 'pi pi-download', routerLink: '/admin/reports' }
        ]
      },
      {
        label: 'Workspace',
        items: [
          { label: 'Settings', icon: 'pi pi-cog', routerLink: '/admin/settings', visible: !isSponsor }
        ]
      }
    ];
  }

  logout() {
    this.roleService.clearRole();
    this.router.navigate(['/admin/login']);
  }
}
