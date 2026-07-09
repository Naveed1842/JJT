import {
  ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, inject,
} from '@angular/core';
import { AdminService } from '../../../services/admin.service';
import { AuditEventResponse } from '../../../services/api.models';

@Component({
  selector: 'app-admin-audit-section',
  standalone: true,
  templateUrl: './admin-audit-section.component.html',
  styleUrl: '../admin.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminAuditSectionComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly cdr = inject(ChangeDetectorRef);

  auditEvents: AuditEventResponse[] = [];
  auditPage = 0;
  auditTotalPages = 1;
  loadingAudit = false;

  ngOnInit(): void {
    this.loadAuditLog();
  }

  loadAuditLog(page = 0): void {
    this.loadingAudit = true;
    this.auditPage = page;
    this.adminService.getAuditLog(page, 50).subscribe({
      next: (data) => {
        this.auditEvents = data.content;
        this.auditTotalPages = data.totalPages || 1;
        this.loadingAudit = false;
        this.cdr.markForCheck(); // OnPush: async field updates must mark the view dirty
      },
      error: () => { this.loadingAudit = false; this.cdr.markForCheck(); }
    });
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
