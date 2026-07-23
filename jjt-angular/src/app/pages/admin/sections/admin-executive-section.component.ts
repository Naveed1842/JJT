import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../services/admin.service';
import { ExecutiveSummaryResponse } from '../../../services/api.models';

@Component({
  selector: 'app-admin-executive-section',
  standalone: true,
  imports: [CommonModule],
  styleUrl: '../admin.component.css',
  template: `
<div class="section-card">
  <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;">
    <h2 class="section-title">Executive Summary</h2>
    <button class="btn-ghost" (click)="load()">Refresh</button>
  </div>

  <div *ngIf="loading" class="loading-row">Loading summary…</div>

  <ng-container *ngIf="!loading && summary">
    <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:16px;margin-bottom:28px;">
      <div class="exec-card">
        <div class="exec-label">MTD Income</div>
        <div class="exec-value">{{summary.mtdIncome}}</div>
      </div>
      <div class="exec-card">
        <div class="exec-label">MTD Expense</div>
        <div class="exec-value">{{summary.mtdExpense}}</div>
      </div>
      <div class="exec-card">
        <div class="exec-label">Pending Approvals</div>
        <div class="exec-value" [style.color]="summary.pendingApprovalCount>0?'#b07d3a':undefined">
          {{summary.pendingApprovalCount}}
        </div>
      </div>
      <div class="exec-card">
        <div class="exec-label">Programme %</div>
        <div class="exec-value">{{pct(summary.programmePct)}}%</div>
        <div style="height:6px;background:#e0d8c8;border-radius:3px;margin-top:10px;">
          <div style="height:100%;border-radius:3px;background:#2f5d4f;"
               [style.width]="pct(summary.programmePct)+'%'"></div>
        </div>
      </div>
    </div>

    <div *ngIf="summary.transparencySnapshotDate" style="font-size:13px;color:#8a958d;margin-bottom:20px;">
      Transparency snapshot: {{summary.transparencySnapshotDate}}
    </div>

    <div *ngIf="(summary.aiRecommendations?.length ?? 0) > 0">
      <h3 style="font-size:15px;font-weight:600;color:#1c352c;margin-bottom:12px;">AI Recommendations</h3>
      <div *ngFor="let r of summary.aiRecommendations" class="ai-rec">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
          <span class="badge badge-amber">{{r.entityType}}</span>
          <span style="font-size:12px;color:#8a958d;">Confidence: {{r.confidence}}</span>
        </div>
        <div style="font-size:14px;color:#1c352c;font-weight:500;margin-bottom:4px;">{{r.recommendation}}</div>
        <div style="font-size:13px;color:#54625b;">{{r.summary}}</div>
      </div>
    </div>

    <div *ngIf="!summary.aiRecommendations?.length" class="empty-state" style="margin-top:16px;">
      No AI recommendations pending.
    </div>
  </ng-container>

  <div *ngIf="!loading && !summary" class="empty-state">Failed to load executive summary.</div>
</div>
  `,
  styles: [`
    .exec-card { background:#f3ede3; border-radius:10px; padding:18px 20px; }
    .exec-label { font-size:11px; color:#8a958d; font-weight:600; text-transform:uppercase; letter-spacing:.07em; margin-bottom:6px; }
    .exec-value { font-size:22px; font-weight:700; color:#1c352c; }
    .ai-rec { background:#fffdf9; border:1px solid #e3dccd; border-radius:10px; padding:14px 16px; margin-bottom:10px; }
  `]
})
export class AdminExecutiveSectionComponent implements OnInit {
  private readonly svc = inject(AdminService);

  summary: ExecutiveSummaryResponse | null = null;
  loading = false;

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.svc.getExecutiveSummary().subscribe({
      next: (d) => { this.summary = d; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  pct(v: string): string {
    const n = parseFloat(v) || 0;
    return Math.min(100, Math.round(n)).toString();
  }
}
