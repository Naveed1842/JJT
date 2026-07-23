import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import {
  CreateMissionNodeRequest,
  MissionFinancialsResponse,
  MissionNodeKind,
  MissionNodeResponse,
} from '../../../services/api.models';

@Component({
  selector: 'app-admin-missions-section',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrl: '../admin.component.css',
  template: `
<div class="section-card">
  <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;">
    <h2 class="section-title">Missions & Programmes</h2>
    <button class="btn-primary" (click)="showForm=!showForm">+ Add Node</button>
  </div>

  <div *ngIf="showForm" class="inline-form" style="margin-bottom:20px;">
    <div class="form-row">
      <select class="form-select" [(ngModel)]="form.kind">
        <option value="PROGRAMME">Programme</option>
        <option value="PROJECT">Project</option>
        <option value="ACTIVITY">Activity</option>
        <option value="OUTPUT">Output</option>
      </select>
      <input class="form-input" [(ngModel)]="form.name" placeholder="Name *" style="flex:1;" />
    </div>
    <div class="form-row">
      <select class="form-select" [(ngModel)]="form.parentId">
        <option value="">— No parent (root) —</option>
        <option *ngFor="let n of allNodes" [value]="n.id">{{nodePrefix(n.kind)}} {{n.name}}</option>
      </select>
      <input class="form-input" [(ngModel)]="form.targetAmount" placeholder="Target amount" type="number" />
    </div>
    <div class="form-row">
      <label style="font-size:13px;color:#54625b;">Start</label>
      <input class="form-input" type="date" [(ngModel)]="form.startDate" />
      <label style="font-size:13px;color:#54625b;">End</label>
      <input class="form-input" type="date" [(ngModel)]="form.endDate" />
    </div>
    <div class="form-row">
      <input class="form-input" [(ngModel)]="form.description" placeholder="Description" style="flex:1;" />
    </div>
    <div class="form-actions">
      <button class="btn-primary" (click)="create()" [disabled]="saving">Save</button>
      <button class="btn-ghost" (click)="showForm=false">Cancel</button>
    </div>
    <div *ngIf="errorMsg" class="error-msg">{{errorMsg}}</div>
  </div>

  <!-- Financials panel -->
  <div *ngIf="financials" class="inline-form" style="margin-bottom:20px;border-color:#2f5d4f;">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
      <strong style="font-size:14px;color:#1c352c;">Financials (YTD)</strong>
      <button class="btn-ghost" style="font-size:12px;" (click)="financials=null">Close</button>
    </div>
    <div class="kv-row"><span>Total Expense</span><span>{{financials.income | slice:0:0}}{{financials.income}} — see below</span></div>
    <div style="display:flex;gap:24px;flex-wrap:wrap;">
      <div class="stat-mini"><div class="stat-mini-label">Expense</div><div class="stat-mini-value">{{financials.totalExpense}}</div></div>
      <div class="stat-mini"><div class="stat-mini-label">Income</div><div class="stat-mini-value">{{financials.income}}</div></div>
      <div class="stat-mini"><div class="stat-mini-label">Surplus</div><div class="stat-mini-value" [style.color]="parseNum(financials.surplus)<0?'#b94a48':'#2f5d4f'">{{financials.surplus}}</div></div>
    </div>
  </div>

  <div *ngIf="loading" class="loading-row">Loading missions…</div>
  <div *ngIf="!loading && roots.length===0" class="empty-state">No programmes yet.</div>

  <div *ngIf="!loading && roots.length>0">
    <div *ngFor="let node of roots" class="mission-node" [attr.data-kind]="node.kind">
      <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:10px;">
        <div>
          <span class="badge badge-grey" style="font-size:11px;margin-right:8px;">{{node.kind}}</span>
          <strong style="font-size:15px;color:#1c352c;">{{node.name}}</strong>
          <div *ngIf="node.description" style="font-size:13px;color:#54625b;margin-top:4px;">{{node.description}}</div>
          <div style="font-size:12px;color:#8a958d;margin-top:4px;">
            {{node.startDate ?? '—'}} → {{node.endDate ?? '—'}}
            <span *ngIf="node.targetAmount" style="margin-left:10px;">Target: {{node.targetAmount}}</span>
          </div>
        </div>
        <div style="display:flex;gap:6px;flex-shrink:0;">
          <span class="badge" [ngClass]="statusClass(node.status)">{{node.status}}</span>
          <button class="btn-xs" (click)="loadFinancials(node.id)">Financials</button>
          <button class="btn-xs" (click)="loadChildren(node)">Children</button>
        </div>
      </div>

      <!-- Inline children -->
      <div *ngIf="childrenMap[node.id]?.length>0" style="margin-top:12px;margin-left:20px;display:flex;flex-direction:column;gap:8px;">
        <div *ngFor="let c of childrenMap[node.id]" class="mission-node" style="background:#f9f5ef;">
          <div style="display:flex;justify-content:space-between;align-items:center;gap:10px;">
            <div>
              <span class="badge badge-grey" style="font-size:11px;margin-right:8px;">{{c.kind}}</span>
              <strong style="font-size:14px;color:#1c352c;">{{c.name}}</strong>
            </div>
            <span class="badge" [ngClass]="statusClass(c.status)">{{c.status}}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
  `,
  styles: [`
    .mission-node { background:#fffdf9; border:1px solid #e3dccd; border-radius:10px; padding:14px 16px; margin-bottom:10px; }
    .stat-mini { background:#f3ede3; border-radius:8px; padding:12px 16px; }
    .stat-mini-label { font-size:11px; color:#8a958d; font-weight:600; text-transform:uppercase; letter-spacing:.06em; margin-bottom:4px; }
    .stat-mini-value { font-size:18px; font-weight:700; color:#1c352c; }
  `]
})
export class AdminMissionsSectionComponent implements OnInit {
  private readonly svc = inject(AdminService);

  roots: MissionNodeResponse[] = [];
  allNodes: MissionNodeResponse[] = [];
  childrenMap: Record<string, MissionNodeResponse[]> = {};
  financials: MissionFinancialsResponse | null = null;
  loading = false;
  showForm = false;
  saving = false;
  errorMsg: string | null = null;

  form: CreateMissionNodeRequest & { parentId?: any; targetAmount?: any; startDate?: any; endDate?: any } = {
    kind: 'PROGRAMME', name: '', description: '', parentId: '', targetAmount: '', startDate: '', endDate: ''
  };

  readonly kinds: MissionNodeKind[] = ['PROGRAMME', 'PROJECT', 'ACTIVITY', 'OUTPUT'];

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.svc.listMissionRoots().subscribe({
      next: (d) => { this.roots = d; this.allNodes = d; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  loadChildren(node: MissionNodeResponse): void {
    if (this.childrenMap[node.id]) {
      delete this.childrenMap[node.id];
      return;
    }
    this.svc.getMissionChildren(node.id).subscribe({
      next: (c) => { this.childrenMap = { ...this.childrenMap, [node.id]: c }; }
    });
  }

  loadFinancials(id: string): void {
    this.svc.getMissionFinancials(id).subscribe({ next: (f) => { this.financials = f; } });
  }

  create(): void {
    if (this.saving || !this.form.name?.trim()) {
      this.errorMsg = 'Name is required.';
      return;
    }
    this.saving = true;
    this.errorMsg = null;
    this.svc.createMissionNode({
      kind: this.form.kind,
      name: this.form.name.trim(),
      description: (this.form as any).description?.trim() || null,
      parentId: (this.form as any).parentId || null,
      targetAmount: (this.form as any).targetAmount || null,
      startDate: (this.form as any).startDate || null,
      endDate: (this.form as any).endDate || null,
    }).subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.form = { kind: 'PROGRAMME', name: '', description: '', parentId: '', targetAmount: '', startDate: '', endDate: '' };
        this.load();
      },
      error: (e) => { this.saving = false; this.errorMsg = e?.error?.message ?? 'Failed to create mission node.'; }
    });
  }

  nodePrefix(kind: string): string {
    if (kind === 'PROGRAMME') return '◆';
    if (kind === 'PROJECT')   return '◇';
    if (kind === 'ACTIVITY')  return '›';
    return '·';
  }

  parseNum(v: string): number { return parseFloat(v) || 0; }

  statusClass(s: string): string {
    if (s === 'ACTIVE')     return 'badge-green';
    if (s === 'COMPLETED')  return 'badge-grey';
    if (s === 'CANCELLED')  return 'badge-red';
    return 'badge-amber';
  }
}
