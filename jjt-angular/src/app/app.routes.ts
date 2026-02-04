import { Routes } from '@angular/router';

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
    path: 'children/:id',
    loadComponent: () => import('./pages/child-detail/child-detail.component').then(m => m.ChildDetailComponent)
  },
  {
    path: 'admin',
    loadComponent: () => import('./pages/admin/admin.component').then(m => m.AdminComponent)
  },
  {
    path: 'sponsor',
    loadComponent: () => import('./pages/sponsor/sponsor.component').then(m => m.SponsorComponent)
  }
];
