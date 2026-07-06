import { Injectable, computed, inject, signal } from '@angular/core';
import { AdminService } from './admin.service';
import { AlertResponse } from './api.models';

/**
 * Shared admin-alert state. The sidebar badge, the dashboard's critical-alerts
 * card, and the Alerts section all read the same signals — dismissing an alert
 * anywhere updates everywhere, with one fetch instead of one per consumer.
 */
@Injectable({ providedIn: 'root' })
export class AlertsStore {
  private readonly adminService = inject(AdminService);

  private readonly _alerts = signal<AlertResponse[]>([]);
  private readonly _loading = signal(false);

  readonly alerts = this._alerts.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly critical = computed(
    () => this._alerts().filter(a => a.severity === 'HIGH' || a.severity === 'CRITICAL')
  );

  load(): void {
    this._loading.set(true);
    this.adminService.listAlerts().subscribe({
      next: (data) => { this._alerts.set(data); this._loading.set(false); },
      error: () => { this._loading.set(false); }
    });
  }

  dismiss(id: string): void {
    this.adminService.dismissAlert(id).subscribe({
      next: () => this._alerts.update(list => list.filter(a => a.id !== id)),
      error: () => {}
    });
  }
}
