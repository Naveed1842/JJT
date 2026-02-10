import { Component } from '@angular/core';
import { AdminLayoutComponent } from './admin-layout.component';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [AdminLayoutComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent {}
