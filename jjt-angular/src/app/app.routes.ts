import { Routes } from '@angular/router';
import { adminAuthGuard } from './pages/admin/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'children',
    loadComponent: () => import('./pages/children/children.component').then(m => m.ChildrenComponent)
  },
  {
    path: 'children/:childId',
    loadComponent: () => import('./pages/child-detail/child-detail.component').then(m => m.ChildDetailComponent)
  },
  {
    path: 'children/:childId/sponsor',
    loadComponent: () => import('./pages/sponsor-commit/sponsor-commit.component').then(m => m.SponsorCommitComponent)
  },
  {
    path: 'sponsor/confirmation',
    loadComponent: () => import('./pages/sponsor-confirmation/sponsor-confirmation.component').then(m => m.SponsorConfirmationComponent)
  },
  {
    path: 'children/:childId/sponsor/confirmation',
    loadComponent: () => import('./pages/sponsor-confirmation/sponsor-confirmation.component').then(m => m.SponsorConfirmationComponent)
  },
  {
    path: 'admin',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./pages/admin/admin-login.component').then(m => m.AdminLoginComponent)
      },
      {
        path: '',
        loadComponent: () => import('./pages/admin/admin.component').then(m => m.AdminComponent),
        canActivateChild: [adminAuthGuard],
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./pages/admin/admin-dashboard.component').then(m => m.AdminDashboardComponent)
          },
          {
            path: 'children',
            loadComponent: () => import('./pages/admin/admin-children.component').then(m => m.AdminChildrenComponent)
          },
          {
            path: 'sponsors',
            loadComponent: () => import('./pages/admin/admin-sponsors.component').then(m => m.AdminSponsorsComponent)
          },
          {
            path: 'sponsorships',
            loadComponent: () => import('./pages/admin/admin-sponsorships.component').then(m => m.AdminSponsorshipsComponent)
          },
          {
            path: 'reports',
            loadComponent: () => import('./pages/admin/admin-reports.component').then(m => m.AdminReportsComponent)
          },
          {
            path: 'settings',
            loadComponent: () => import('./pages/admin/admin-settings.component').then(m => m.AdminSettingsComponent)
          },
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'dashboard'
          }
        ]
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
