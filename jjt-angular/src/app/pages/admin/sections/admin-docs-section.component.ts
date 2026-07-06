import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-admin-docs-section',
  standalone: true,
  templateUrl: './admin-docs-section.component.html',
  styleUrl: '../admin.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminDocsSectionComponent {}
