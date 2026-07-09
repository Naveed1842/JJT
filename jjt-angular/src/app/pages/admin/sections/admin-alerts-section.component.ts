import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { NgClass, SlicePipe } from '@angular/common';
import { AlertsStore } from '../../../services/alerts.store';
import { AlertResponse } from '../../../services/api.models';

@Component({
  selector: 'app-admin-alerts-section',
  standalone: true,
  imports: [NgClass, SlicePipe],
  templateUrl: './admin-alerts-section.component.html',
  styleUrl: '../admin.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminAlertsSectionComponent implements OnInit {
  private readonly store = inject(AlertsStore);

  ngOnInit(): void {
    this.store.load();
  }

  get alertList(): AlertResponse[] { return this.store.alerts(); }
  get loadingAlerts(): boolean     { return this.store.loading(); }

  loadAlerts(): void { this.store.load(); }
  dismissAlert(id: string): void { this.store.dismiss(id); }

  alertSeverityClass(sev: string): string {
    if (sev === 'CRITICAL' || sev === 'HIGH') return 'badge-red';
    if (sev === 'MEDIUM')  return 'badge-amber';
    return 'badge-grey';
  }

  timeAgo(iso: string): string {
    const diff = Date.now() - new Date(iso).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h ago`;
    return `${Math.floor(hrs / 24)}d ago`;
  }
}
