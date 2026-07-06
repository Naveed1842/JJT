import { Routes } from '@angular/router';
import { adminAuthGuard } from './guards/admin-auth.guard';
import { sponsorAuthGuard } from './guards/sponsor-auth.guard';

const SITE = 'Junior Jinnah Trust';

export const routes: Routes = [
  {
    path: '',
    title: `${SITE} — Sponsor a Child's Education`,
    loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'login',
    title: `Sign in · ${SITE}`,
    loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'children',
    title: `Children seeking sponsors · ${SITE}`,
    loadComponent: () =>
      import('./pages/one-child-at-a-time/one-child-at-a-time.component').then(
        m => m.OneChildAtATimeComponent
      )
  },
  {
    path: 'children/:childId',
    title: `Child profile · ${SITE}`,
    loadComponent: () => import('./pages/child-detail/child-detail.component').then(m => m.ChildDetailComponent)
  },
  {
    path: 'children/:childId/sponsor',
    title: `Sponsor a child · ${SITE}`,
    loadComponent: () => import('./pages/sponsor-commit/sponsor-commit.component').then(m => m.SponsorCommitComponent)
  },
  {
    path: 'sponsor/confirmation',
    title: `Sponsorship confirmed · ${SITE}`,
    loadComponent: () => import('./pages/sponsor-confirmation/sponsor-confirmation.component').then(m => m.SponsorConfirmationComponent)
  },
  {
    path: 'trust',
    title: `Trust & accountability · ${SITE}`,
    loadComponent: () => import('./pages/trust/trust.component').then(m => m.TrustComponent)
  },
  {
    path: 'why-give',
    title: `Why give · ${SITE}`,
    loadComponent: () => import('./pages/why-give/why-give.component').then(m => m.WhyGiveComponent)
  },
  {
    path: 'admin',
    canActivate: [adminAuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: ':section',
        title: `Admin · ${SITE}`,
        loadComponent: () => import('./pages/admin/admin.component').then(m => m.AdminComponent)
      }
    ]
  },
  {
    path: 'sponsor/portal',
    title: `My sponsorships · ${SITE}`,
    canActivate: [sponsorAuthGuard],
    loadComponent: () =>
      import('./pages/sponsor-portal/sponsor-portal.component').then(m => m.SponsorPortalComponent)
  },
  {
    path: '**',
    title: `Page not found · ${SITE}`,
    loadComponent: () => import('./pages/not-found/not-found.component').then(m => m.NotFoundComponent)
  }
];
