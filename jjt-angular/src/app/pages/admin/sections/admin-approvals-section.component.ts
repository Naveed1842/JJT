import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import { ApprovalRequestResponse, ApprovalStatus } from '../../../services/api.models';

@Component({
  selector: 'app-admin-approvals-section',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrl: '../admin.component.css',
  template: `
<div class="section-card">
  <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;">
    <h2 class="section-title">Approvals</h2>
    <div class="filter-row">
      <button class="filter-chip" [class.active]="filter===''" (click)="setFilter('')">All Pending</button>
      <button class="filter-chip" [class.active]="filter==='RECOMMENDED_APPROVE'" (click)="setFilter('RECOMMENDED_APPROVE')">AI: Approve</button>
      <button class="filter-chip" [class.active]="filter==='RECOMMENDED_REVIEW'" (click)="setFilter('RECOMMENDED_REVIEW')">AI: Review</button>
      <button class="filter-chip" [class.active]="filter==='RECOMMENDED_REJECT'" (click)="setFilter('RECOMMENDED_REJECT')">AI: Reject</button>
      <button class="filter-chip" [class.active]="filter==='APPROVED'" (click)="setFilter('APPROVED')">Approved</button>
      <button class="filter-chip" [class.active]="filter==='REJECTED'" (click)="setFilter('REJECTED')">Rejected</button>
    </div>
  </div>

  <div *ngIf="loading" class="loading-row">Loading approvals…</div>
  <div *ngIf="!loading && approvals.length===0" class="empty-state">No approval requests.</div>

  <div *ngIf="!loading && approvals.length>0" style="display:flex;flex-direction:column;gap:16px;">
    <div *ngFor="let a of approvals" class="approval-card">
      <div class="approval-header">
        <div>
          <span class="badge" [ngClass]="approvalStatusClass(a.status)">{{a.status}}</span>
          <span style="font-size:14px;font-weight:600;margin-left:10px;">{{a.entityType}} — {{a.entityId | slice:0:8}}…</span>
        </div>
        <span style="font-size:12px;color:#8a958d;">{{a.createdAt | date:'d MMM yyyy'}}</span>
      </div>

      <!-- AI checks -->
      <div *ngIf="a.checks?.length>0" style="margin:10px 0;">
        <div *ngFor="let c of a.checks" style="display:flex;gap:8px;align-items:center;margin-bottom:6px;">
          <span class="badge" [ngClass]="checkVerdictClass(c.verdict)" style="font-size:11px;">{{c.verdict}}</span>
          <span style="font-size:13px;color:#54625b;">{{c.checkType}}</span>
        </div>
      </div>

      <!-- AI recommendation -->
      <div *ngIf="a.aiRecommendation" style="font-size:13px;color:#3c4a43;background:#f3ede3;border-radius:8px;padding:10px;margin:8px 0;">
        <strong>AI: </strong>{{a.aiRecommendation}}
        <span *ngIf="a.aiConfidence" style="color:#8a958d;margin-left:6px;">({{a.aiConfidence}} confidence)</span>
      </div>

      <!-- Resolution (for pending) -->
      <div *ngIf="isPending(a)" style="display:flex;gap:10px;align-items:center;margin-top:12px;">
        <input class="form-input" [(ngModel)]="resolutionNotes[a.id]" placeholder="Notes (optional)" style="flex:1;" />
        <button class="btn-xs btn-green" (click)="resolve(a.id, 'APPROVED')">Approve</button>
        <button class="btn-xs btn-red" (click)="resolve(a.id, 'REJECTED')">Reject</button>
      </div>
    </div>
  </div>
</div>
  `,
  styles: [`
    .approval-card { background:#fffdf9; border:1px solid #e3dccd; border-radius:10px; padding:16px; }
    .approval-header { display:flex; justify-content:space-between; align-items:center; }
  `]
})
export class AdminApprovalsSectionComponent implements OnInit {
  private readonly svc = inject(AdminService);

  approvals: ApprovalRequestResponse[] = [];
  loading = false;
  filter = '';
  resolutionNotes: Record<string, string> = {};

  ngOnInit(): void { this.load(); }

  setFilter(f: string): void {
    this.filter = f;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.svc.listApprovals(this.filter as ApprovalStatus || undefined).subscribe({
      next: (d) => { this.approvals = d; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  isPending(a: ApprovalRequestResponse): boolean {
    return ['SUBMITTED', 'RECOMMENDED_APPROVE', 'RECOMMENDED_REVIEW', 'RECOMMENDED_REJECT'].includes(a.status);
  }

  resolve(id: string, decision: 'APPROVED' | 'REJECTED'): void {
    this.svc.resolveApproval(id, decision, this.resolutionNotes[id]).subscribe({
      next: () => { delete this.resolutionNotes[id]; this.load(); }
    });
  }

  approvalStatusClass(s: string): string {
    if (s === 'APPROVED')             return 'badge-green';
    if (s === 'REJECTED')             return 'badge-red';
    if (s === 'RECOMMENDED_APPROVE')  return 'badge-green';
    if (s === 'RECOMMENDED_REJECT')   return 'badge-red';
    if (s === 'RECOMMENDED_REVIEW')   return 'badge-amber';
    return 'badge-grey';
  }

  checkVerdictClass(v: string): string {
    if (v === 'PASS') return 'badge-green';
    if (v === 'FAIL') return 'badge-red';
    return 'badge-amber';
  }
}
