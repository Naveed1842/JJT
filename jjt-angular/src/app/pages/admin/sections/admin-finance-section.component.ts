import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import {
  AccountCategoryResponse,
  BudgetResponse,
  BudgetVarianceRow,
  CreateBudgetRequest,
  CreateExpenseRequest,
  CreateFinancialPeriodRequest,
  CreateVendorRequest,
  ExpenseResponse,
  FinancialPeriodResponse,
  FinancialTransactionResponse,
  PayrollItemResponse,
  PayrollRunResponse,
  VendorResponse,
} from '../../../services/api.models';

type FinanceTab = 'expenses' | 'vendors' | 'periods' | 'budgets' | 'transactions' | 'payroll' | 'categories';

@Component({
  selector: 'app-admin-finance-section',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrl: '../admin.component.css',
  template: `
<div class="section-card">
  <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;">
    <h2 class="section-title">Finance</h2>
  </div>

  <!-- Tab bar -->
  <div class="tab-bar" style="margin-bottom:24px;">
    <button *ngFor="let t of tabs" class="tab-btn" [class.active]="activeTab===t.id" (click)="setTab(t.id)">{{t.label}}</button>
  </div>

  <!-- Expenses tab -->
  <ng-container *ngIf="activeTab==='expenses'">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <div class="filter-row">
        <button class="filter-chip" [class.active]="expenseFilter===''" (click)="setExpenseFilter('')">All</button>
        <button class="filter-chip" [class.active]="expenseFilter==='DRAFT'" (click)="setExpenseFilter('DRAFT')">Draft</button>
        <button class="filter-chip" [class.active]="expenseFilter==='SUBMITTED'" (click)="setExpenseFilter('SUBMITTED')">Submitted</button>
        <button class="filter-chip" [class.active]="expenseFilter==='APPROVED'" (click)="setExpenseFilter('APPROVED')">Approved</button>
        <button class="filter-chip" [class.active]="expenseFilter==='PAID'" (click)="setExpenseFilter('PAID')">Paid</button>
      </div>
      <button class="btn-primary" (click)="showExpenseForm=!showExpenseForm">+ New Expense</button>
    </div>

    <div *ngIf="showExpenseForm" class="inline-form" style="margin-bottom:20px;">
      <div class="form-row">
        <input class="form-input" [(ngModel)]="expenseForm.title" placeholder="Title *" />
        <input class="form-input" [(ngModel)]="expenseForm.amount" placeholder="Amount *" type="number" />
        <input class="form-input" [(ngModel)]="expenseForm.currency" placeholder="Currency" style="max-width:90px;" />
      </div>
      <div class="form-row">
        <input class="form-input" type="date" [(ngModel)]="expenseForm.expenseDate" />
        <input class="form-input" [(ngModel)]="expenseForm.vendorName" placeholder="Vendor name (or select below)" />
        <select class="form-select" [(ngModel)]="expenseForm.categoryId">
          <option value="">— Category —</option>
          <option *ngFor="let c of categories" [value]="c.id">{{c.code}} {{c.name}}</option>
        </select>
      </div>
      <div class="form-row">
        <input class="form-input" [(ngModel)]="expenseForm.description" placeholder="Description" style="flex:1;" />
      </div>
      <div class="form-actions">
        <button class="btn-primary" (click)="createExpense()" [disabled]="saving">Save Draft</button>
        <button class="btn-ghost" (click)="showExpenseForm=false">Cancel</button>
      </div>
      <div *ngIf="errorMsg" class="error-msg">{{errorMsg}}</div>
    </div>

    <div *ngIf="loadingExpenses" class="loading-row">Loading expenses…</div>
    <div *ngIf="!loadingExpenses && expenses.length===0" class="empty-state">No expenses found.</div>

    <div *ngIf="!loadingExpenses && expenses.length>0" class="data-table-wrap">
      <table class="data-table">
        <thead><tr>
          <th>Title</th><th>Vendor</th><th>Amount</th><th>Date</th><th>Status</th><th>Actions</th>
        </tr></thead>
        <tbody>
          <tr *ngFor="let e of expenses">
            <td>{{e.title}}</td>
            <td>{{e.vendorName ?? '—'}}</td>
            <td>{{e.currency}} {{e.amount}}</td>
            <td>{{e.expenseDate}}</td>
            <td><span class="badge" [ngClass]="expenseStatusClass(e.status)">{{e.status}}</span></td>
            <td class="action-cell">
              <button *ngIf="e.status==='DRAFT'" class="btn-xs" (click)="submitExpense(e.id)">Submit</button>
              <button *ngIf="e.status==='SUBMITTED'" class="btn-xs btn-green" (click)="approveExpense(e.id)">Approve</button>
              <button *ngIf="e.status==='SUBMITTED'" class="btn-xs btn-red" (click)="rejectExpense(e.id)">Reject</button>
              <button *ngIf="e.status==='APPROVED'" class="btn-xs btn-green" (click)="payExpense(e.id)">Mark Paid</button>
              <button *ngIf="e.status!=='PAID' && e.status!=='VOIDED'" class="btn-xs btn-red" (click)="voidExpense(e.id)">Void</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </ng-container>

  <!-- Vendors tab -->
  <ng-container *ngIf="activeTab==='vendors'">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h3 class="sub-title">Vendors</h3>
      <button class="btn-primary" (click)="showVendorForm=!showVendorForm">+ Add Vendor</button>
    </div>
    <div *ngIf="showVendorForm" class="inline-form" style="margin-bottom:20px;">
      <div class="form-row">
        <input class="form-input" [(ngModel)]="vendorForm.name" placeholder="Vendor name *" />
        <input class="form-input" [(ngModel)]="vendorForm.contactEmail" placeholder="Email" />
        <input class="form-input" [(ngModel)]="vendorForm.contactPhone" placeholder="Phone" />
      </div>
      <div class="form-row">
        <input class="form-input" [(ngModel)]="vendorForm.address" placeholder="Address" style="flex:1;" />
        <input class="form-input" [(ngModel)]="vendorForm.taxReference" placeholder="Tax Ref" />
      </div>
      <div class="form-actions">
        <button class="btn-primary" (click)="createVendor()" [disabled]="saving">Save</button>
        <button class="btn-ghost" (click)="showVendorForm=false">Cancel</button>
      </div>
      <div *ngIf="errorMsg" class="error-msg">{{errorMsg}}</div>
    </div>
    <div *ngIf="loadingVendors" class="loading-row">Loading vendors…</div>
    <div *ngIf="!loadingVendors && vendors.length===0" class="empty-state">No vendors yet.</div>
    <div *ngIf="!loadingVendors && vendors.length>0" class="data-table-wrap">
      <table class="data-table">
        <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Tax Ref</th><th>Status</th><th></th></tr></thead>
        <tbody>
          <tr *ngFor="let v of vendors">
            <td>{{v.name}}</td>
            <td>{{v.contactEmail ?? '—'}}</td>
            <td>{{v.contactPhone ?? '—'}}</td>
            <td>{{v.taxReference ?? '—'}}</td>
            <td><span class="badge" [ngClass]="v.active ? 'badge-green' : 'badge-grey'">{{v.active ? 'Active' : 'Inactive'}}</span></td>
            <td><button *ngIf="v.active" class="btn-xs btn-red" (click)="deactivateVendor(v.id)">Deactivate</button></td>
          </tr>
        </tbody>
      </table>
    </div>
  </ng-container>

  <!-- Periods tab -->
  <ng-container *ngIf="activeTab==='periods'">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h3 class="sub-title">Financial Periods</h3>
      <button class="btn-primary" (click)="showPeriodForm=!showPeriodForm">+ New Period</button>
    </div>
    <div *ngIf="showPeriodForm" class="inline-form" style="margin-bottom:20px;">
      <div class="form-row">
        <input class="form-input" [(ngModel)]="periodForm.label" placeholder="Label (e.g. Apr 2026)" />
        <select class="form-select" [(ngModel)]="periodForm.periodType">
          <option value="MONTHLY">Monthly</option>
          <option value="QUARTERLY">Quarterly</option>
          <option value="ANNUAL">Annual</option>
        </select>
      </div>
      <div class="form-row">
        <label style="font-size:13px;color:#54625b;">Start</label>
        <input class="form-input" type="date" [(ngModel)]="periodForm.startDate" />
        <label style="font-size:13px;color:#54625b;">End</label>
        <input class="form-input" type="date" [(ngModel)]="periodForm.endDate" />
      </div>
      <div class="form-actions">
        <button class="btn-primary" (click)="createPeriod()" [disabled]="saving">Create</button>
        <button class="btn-ghost" (click)="showPeriodForm=false">Cancel</button>
      </div>
      <div *ngIf="errorMsg" class="error-msg">{{errorMsg}}</div>
    </div>
    <div *ngIf="loadingPeriods" class="loading-row">Loading periods…</div>
    <div *ngIf="!loadingPeriods && periods.length===0" class="empty-state">No financial periods yet.</div>
    <div *ngIf="!loadingPeriods && periods.length>0" class="data-table-wrap">
      <table class="data-table">
        <thead><tr><th>Label</th><th>Type</th><th>Start</th><th>End</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>
          <tr *ngFor="let p of periods">
            <td>{{p.label}}</td>
            <td>{{p.periodType}}</td>
            <td>{{p.startDate}}</td>
            <td>{{p.endDate}}</td>
            <td><span class="badge" [ngClass]="periodStatusClass(p.status)">{{p.status}}</span></td>
            <td class="action-cell">
              <button *ngIf="p.status==='OPEN'" class="btn-xs" (click)="closePeriod(p.id)">Close</button>
              <button *ngIf="p.status==='CLOSED'" class="btn-xs btn-red" (click)="lockPeriod(p.id)">Lock</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </ng-container>

  <!-- Budgets tab -->
  <ng-container *ngIf="activeTab==='budgets'">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <div style="display:flex;gap:10px;align-items:center;">
        <h3 class="sub-title">Budgets</h3>
        <select class="form-select" style="width:auto;" [(ngModel)]="budgetPeriodId" (change)="loadBudgets()">
          <option value="">— All periods —</option>
          <option *ngFor="let p of periods" [value]="p.id">{{p.label}}</option>
        </select>
        <button class="btn-ghost" *ngIf="budgetPeriodId" (click)="loadBudgetVariance()">View Variance</button>
      </div>
      <button class="btn-primary" (click)="showBudgetForm=!showBudgetForm">+ Add Budget</button>
    </div>

    <div *ngIf="showBudgetForm" class="inline-form" style="margin-bottom:20px;">
      <div class="form-row">
        <select class="form-select" [(ngModel)]="budgetForm.periodId">
          <option value="">— Period —</option>
          <option *ngFor="let p of periods" [value]="p.id">{{p.label}}</option>
        </select>
        <select class="form-select" [(ngModel)]="budgetForm.categoryId">
          <option value="">— Category —</option>
          <option *ngFor="let c of categories" [value]="c.id">{{c.code}} {{c.name}}</option>
        </select>
      </div>
      <div class="form-row">
        <input class="form-input" [(ngModel)]="budgetForm.budgetedAmount" placeholder="Amount" type="number" />
        <input class="form-input" [(ngModel)]="budgetForm.currency" placeholder="Currency" style="max-width:90px;" />
        <input class="form-input" [(ngModel)]="budgetForm.notes" placeholder="Notes" style="flex:1;" />
      </div>
      <div class="form-actions">
        <button class="btn-primary" (click)="createBudget()" [disabled]="saving">Save</button>
        <button class="btn-ghost" (click)="showBudgetForm=false">Cancel</button>
      </div>
      <div *ngIf="errorMsg" class="error-msg">{{errorMsg}}</div>
    </div>

    <div *ngIf="budgetVariance.length>0" style="margin-bottom:24px;">
      <h4 style="font-size:14px;font-weight:600;color:#1c352c;margin-bottom:10px;">Budget vs Actual</h4>
      <div class="data-table-wrap">
        <table class="data-table">
          <thead><tr><th>Category</th><th>Budgeted</th><th>Actual</th><th>Variance</th></tr></thead>
          <tbody>
            <tr *ngFor="let r of budgetVariance">
              <td>{{r.categoryName ?? r.categoryId}}</td>
              <td class="num-cell">{{r.currency}} {{r.budgeted}}</td>
              <td class="num-cell">{{r.currency}} {{r.actual}}</td>
              <td class="num-cell" [style.color]="parseNum(r.variance)<0?'#b94a48':'#2f5d4f'">
                {{r.currency}} {{r.variance}}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div *ngIf="loadingBudgets" class="loading-row">Loading budgets…</div>
    <div *ngIf="!loadingBudgets && budgets.length===0" class="empty-state">No budgets yet.</div>
    <div *ngIf="!loadingBudgets && budgets.length>0" class="data-table-wrap">
      <table class="data-table">
        <thead><tr><th>Period</th><th>Category</th><th>Budgeted</th><th>Currency</th><th>Notes</th></tr></thead>
        <tbody>
          <tr *ngFor="let b of budgets">
            <td>{{periodLabel(b.periodId)}}</td>
            <td>{{b.categoryName ?? b.categoryId}}</td>
            <td class="num-cell">{{b.budgetedAmount}}</td>
            <td>{{b.currency}}</td>
            <td>{{b.notes ?? '—'}}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </ng-container>

  <!-- Transactions tab -->
  <ng-container *ngIf="activeTab==='transactions'">
    <div style="display:flex;gap:10px;align-items:center;margin-bottom:16px;">
      <h3 class="sub-title">Journal</h3>
      <select class="form-select" style="width:auto;" [(ngModel)]="txPeriodId" (change)="loadTransactions()">
        <option value="">— All periods —</option>
        <option *ngFor="let p of periods" [value]="p.id">{{p.label}}</option>
      </select>
    </div>
    <div *ngIf="loadingTx" class="loading-row">Loading transactions…</div>
    <div *ngIf="!loadingTx && transactions.length===0" class="empty-state">No transactions for this filter.</div>
    <div *ngIf="!loadingTx && transactions.length>0" class="data-table-wrap">
      <table class="data-table">
        <thead><tr><th>Date</th><th>Type</th><th>Source</th><th>Amount</th><th>Class</th><th>Description</th></tr></thead>
        <tbody>
          <tr *ngFor="let t of transactions">
            <td>{{t.txDate}}</td>
            <td><span class="badge" [ngClass]="t.txType==='INCOME'?'badge-green':'badge-grey'">{{t.txType}}</span></td>
            <td>{{t.sourceType}}</td>
            <td class="num-cell">{{t.currency}} {{t.amount}}</td>
            <td>{{t.reportingClass}}</td>
            <td>{{t.description}}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </ng-container>

  <!-- Payroll tab -->
  <ng-container *ngIf="activeTab==='payroll'">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h3 class="sub-title">Payroll Runs</h3>
      <button class="btn-primary" (click)="showPayrollForm=!showPayrollForm">+ New Run</button>
    </div>
    <div *ngIf="showPayrollForm" class="inline-form" style="margin-bottom:20px;">
      <div class="form-row">
        <input class="form-input" [(ngModel)]="payrollForm.periodLabel" placeholder="Period label (e.g. Jul 2026)" />
        <input class="form-input" [(ngModel)]="payrollForm.currency" placeholder="Currency" style="max-width:90px;" />
      </div>
      <div class="form-actions">
        <button class="btn-primary" (click)="createPayrollRun()" [disabled]="saving">Create</button>
        <button class="btn-ghost" (click)="showPayrollForm=false">Cancel</button>
      </div>
      <div *ngIf="errorMsg" class="error-msg">{{errorMsg}}</div>
    </div>
    <div *ngIf="loadingPayroll" class="loading-row">Loading payroll runs…</div>
    <div *ngIf="!loadingPayroll && payrollRuns.length===0" class="empty-state">No payroll runs yet.</div>
    <div *ngIf="!loadingPayroll && payrollRuns.length>0">
      <div class="data-table-wrap">
        <table class="data-table">
          <thead><tr><th>Period</th><th>Status</th><th>Total</th><th>Currency</th><th>Actions</th></tr></thead>
          <tbody>
            <tr *ngFor="let r of payrollRuns">
              <td>{{r.periodLabel}}</td>
              <td><span class="badge" [ngClass]="r.status==='PAID'?'badge-green':r.status==='CANCELLED'?'badge-red':'badge-amber'">{{r.status}}</span></td>
              <td class="num-cell">{{r.totalAmount}}</td>
              <td>{{r.currency}}</td>
              <td class="action-cell">
                <button class="btn-xs" (click)="viewPayrollItems(r.id)">Items</button>
                <button *ngIf="r.status==='DRAFT'" class="btn-xs btn-green" (click)="processPayrollRun(r.id)">Process</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div *ngIf="selectedRunItems.length>0" style="margin-top:20px;">
        <h4 style="font-size:14px;font-weight:600;margin-bottom:10px;">Payroll Items</h4>
        <div class="data-table-wrap">
          <table class="data-table">
            <thead><tr><th>Person</th><th>Gross</th><th>Currency</th><th>Notes</th></tr></thead>
            <tbody>
              <tr *ngFor="let i of selectedRunItems">
                <td>{{i.personName ?? i.personId}}</td>
                <td class="num-cell">{{i.grossAmount}}</td>
                <td>{{i.currency}}</td>
                <td>{{i.notes ?? '—'}}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </ng-container>

  <!-- Categories tab -->
  <ng-container *ngIf="activeTab==='categories'">
    <h3 class="sub-title" style="margin-bottom:16px;">Chart of Accounts</h3>
    <div *ngIf="loadingCategories" class="loading-row">Loading categories…</div>
    <div *ngIf="!loadingCategories && categories.length===0" class="empty-state">No categories loaded.</div>
    <div *ngIf="!loadingCategories && categories.length>0" class="data-table-wrap">
      <table class="data-table">
        <thead><tr><th>Code</th><th>Name</th><th>Reporting Class</th><th>Status</th></tr></thead>
        <tbody>
          <tr *ngFor="let c of categories">
            <td style="font-family:monospace;">{{c.code}}</td>
            <td>{{c.name}}</td>
            <td>{{c.reportingClass}}</td>
            <td><span class="badge" [ngClass]="c.active?'badge-green':'badge-grey'">{{c.active?'Active':'Inactive'}}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </ng-container>
</div>
  `,
})
export class AdminFinanceSectionComponent implements OnInit {
  private readonly svc = inject(AdminService);

  tabs = [
    { id: 'expenses' as FinanceTab, label: 'Expenses' },
    { id: 'vendors'  as FinanceTab, label: 'Vendors' },
    { id: 'periods'  as FinanceTab, label: 'Periods' },
    { id: 'budgets'  as FinanceTab, label: 'Budgets' },
    { id: 'transactions' as FinanceTab, label: 'Journal' },
    { id: 'payroll'  as FinanceTab, label: 'Payroll' },
    { id: 'categories' as FinanceTab, label: 'CoA' },
  ];

  activeTab: FinanceTab = 'expenses';

  // shared data
  categories: AccountCategoryResponse[] = [];
  periods: FinancialPeriodResponse[] = [];
  loadingCategories = false;
  loadingPeriods = false;

  // expenses
  expenses: ExpenseResponse[] = [];
  loadingExpenses = false;
  expenseFilter = '';
  showExpenseForm = false;
  expenseForm: CreateExpenseRequest & { vendorName?: string; categoryId?: any } = {
    title: '', amount: '', currency: 'PKR', expenseDate: new Date().toISOString().split('T')[0],
    vendorName: '', categoryId: '', description: ''
  };

  // vendors
  vendors: VendorResponse[] = [];
  loadingVendors = false;
  showVendorForm = false;
  vendorForm: CreateVendorRequest = { name: '', contactEmail: '', contactPhone: '', address: '', taxReference: '' };

  // periods
  showPeriodForm = false;
  periodForm: CreateFinancialPeriodRequest = { periodType: 'MONTHLY', label: '', startDate: '', endDate: '' };

  // budgets
  budgets: BudgetResponse[] = [];
  budgetVariance: BudgetVarianceRow[] = [];
  loadingBudgets = false;
  showBudgetForm = false;
  budgetPeriodId = '';
  txPeriodId = '';
  budgetForm: CreateBudgetRequest & { categoryId?: any } = {
    periodId: '', categoryId: '', budgetedAmount: '', currency: 'PKR', notes: ''
  };

  // transactions
  transactions: FinancialTransactionResponse[] = [];
  loadingTx = false;

  // payroll
  payrollRuns: PayrollRunResponse[] = [];
  selectedRunItems: PayrollItemResponse[] = [];
  loadingPayroll = false;
  showPayrollForm = false;
  payrollForm = { periodLabel: '', currency: 'PKR' };

  saving = false;
  errorMsg: string | null = null;

  ngOnInit(): void {
    this.loadCategories();
    this.loadPeriods();
    this.loadExpenses();
  }

  setTab(t: FinanceTab): void {
    this.activeTab = t;
    this.errorMsg = null;
    if (t === 'expenses')     this.loadExpenses();
    if (t === 'vendors')      this.loadVendors();
    if (t === 'budgets')      this.loadBudgets();
    if (t === 'transactions') this.loadTransactions();
    if (t === 'payroll')      this.loadPayrollRuns();
  }

  private loadCategories(): void {
    this.loadingCategories = true;
    this.svc.listAccountCategories().subscribe({
      next: (d) => { this.categories = d; this.loadingCategories = false; },
      error: () => { this.loadingCategories = false; }
    });
  }

  private loadPeriods(): void {
    this.loadingPeriods = true;
    this.svc.listFinancialPeriods().subscribe({
      next: (d) => { this.periods = d; this.loadingPeriods = false; },
      error: () => { this.loadingPeriods = false; }
    });
  }

  loadExpenses(): void {
    this.loadingExpenses = true;
    this.svc.listExpenses(this.expenseFilter || undefined).subscribe({
      next: (d) => { this.expenses = d; this.loadingExpenses = false; },
      error: () => { this.loadingExpenses = false; }
    });
  }

  setExpenseFilter(f: string): void {
    this.expenseFilter = f;
    this.loadExpenses();
  }

  createExpense(): void {
    if (this.saving || !this.expenseForm.title || !this.expenseForm.amount) {
      this.errorMsg = 'Title and amount are required.';
      return;
    }
    this.saving = true;
    this.errorMsg = null;
    const req: CreateExpenseRequest = {
      title: this.expenseForm.title,
      amount: this.expenseForm.amount,
      currency: this.expenseForm.currency,
      expenseDate: this.expenseForm.expenseDate,
      description: this.expenseForm.description || null,
      vendorName: (this.expenseForm as any).vendorName || null,
      categoryId: this.expenseForm.categoryId ? Number(this.expenseForm.categoryId) : null,
    };
    this.svc.createExpense(req).subscribe({
      next: () => {
        this.saving = false;
        this.showExpenseForm = false;
        this.expenseForm = { title: '', amount: '', currency: 'PKR', expenseDate: new Date().toISOString().split('T')[0], vendorName: '', categoryId: '', description: '' };
        this.loadExpenses();
      },
      error: (e) => { this.saving = false; this.errorMsg = e?.error?.message ?? 'Failed to create expense.'; }
    });
  }

  submitExpense(id: string): void {
    this.svc.submitExpense(id).subscribe({ next: () => this.loadExpenses() });
  }

  approveExpense(id: string): void {
    this.svc.approveExpense(id).subscribe({ next: () => this.loadExpenses() });
  }

  rejectExpense(id: string): void {
    this.svc.rejectExpense(id).subscribe({ next: () => this.loadExpenses() });
  }

  payExpense(id: string): void {
    this.svc.payExpense(id, 'BANK_TRANSFER').subscribe({ next: () => this.loadExpenses() });
  }

  voidExpense(id: string): void {
    this.svc.voidExpense(id).subscribe({ next: () => this.loadExpenses() });
  }

  loadVendors(): void {
    this.loadingVendors = true;
    this.svc.listVendors().subscribe({
      next: (d) => { this.vendors = d; this.loadingVendors = false; },
      error: () => { this.loadingVendors = false; }
    });
  }

  createVendor(): void {
    if (this.saving || !this.vendorForm.name?.trim()) {
      this.errorMsg = 'Vendor name is required.';
      return;
    }
    this.saving = true;
    this.errorMsg = null;
    this.svc.createVendor({
      name: this.vendorForm.name.trim(),
      contactEmail: this.vendorForm.contactEmail || null,
      contactPhone: this.vendorForm.contactPhone || null,
      address: this.vendorForm.address || null,
      taxReference: this.vendorForm.taxReference || null,
    }).subscribe({
      next: () => {
        this.saving = false;
        this.showVendorForm = false;
        this.vendorForm = { name: '', contactEmail: '', contactPhone: '', address: '', taxReference: '' };
        this.loadVendors();
      },
      error: (e) => { this.saving = false; this.errorMsg = e?.error?.message ?? 'Failed to create vendor.'; }
    });
  }

  deactivateVendor(id: string): void {
    this.svc.deactivateVendor(id).subscribe({ next: () => this.loadVendors() });
  }

  createPeriod(): void {
    if (this.saving || !this.periodForm.label || !this.periodForm.startDate || !this.periodForm.endDate) {
      this.errorMsg = 'Label, start date, and end date are required.';
      return;
    }
    this.saving = true;
    this.errorMsg = null;
    this.svc.createFinancialPeriod(this.periodForm).subscribe({
      next: () => {
        this.saving = false;
        this.showPeriodForm = false;
        this.periodForm = { periodType: 'MONTHLY', label: '', startDate: '', endDate: '' };
        this.loadPeriods();
      },
      error: (e) => { this.saving = false; this.errorMsg = e?.error?.message ?? 'Failed to create period.'; }
    });
  }

  closePeriod(id: string): void {
    this.svc.closePeriod(id).subscribe({ next: () => this.loadPeriods() });
  }

  lockPeriod(id: string): void {
    this.svc.lockPeriod(id).subscribe({ next: () => this.loadPeriods() });
  }

  loadBudgets(): void {
    this.loadingBudgets = true;
    this.budgetVariance = [];
    this.svc.listBudgets(this.budgetPeriodId || undefined).subscribe({
      next: (d) => { this.budgets = d; this.loadingBudgets = false; },
      error: () => { this.loadingBudgets = false; }
    });
  }

  loadBudgetVariance(): void {
    if (!this.budgetPeriodId) return;
    this.svc.getBudgetVariance(this.budgetPeriodId).subscribe({
      next: (d) => { this.budgetVariance = d; }
    });
  }

  createBudget(): void {
    if (this.saving || !this.budgetForm.periodId || !this.budgetForm.categoryId || !this.budgetForm.budgetedAmount) {
      this.errorMsg = 'Period, category, and amount are required.';
      return;
    }
    this.saving = true;
    this.errorMsg = null;
    this.svc.createBudget({
      periodId: this.budgetForm.periodId,
      categoryId: Number(this.budgetForm.categoryId),
      budgetedAmount: this.budgetForm.budgetedAmount,
      currency: this.budgetForm.currency,
      notes: this.budgetForm.notes || null,
    }).subscribe({
      next: () => {
        this.saving = false;
        this.showBudgetForm = false;
        this.budgetForm = { periodId: '', categoryId: '', budgetedAmount: '', currency: 'PKR', notes: '' };
        this.loadBudgets();
      },
      error: (e) => { this.saving = false; this.errorMsg = e?.error?.message ?? 'Failed to create budget.'; }
    });
  }

  loadTransactions(): void {
    this.loadingTx = true;
    this.svc.listFinancialTransactions(this.txPeriodId || undefined).subscribe({
      next: (d) => { this.transactions = d; this.loadingTx = false; },
      error: () => { this.loadingTx = false; }
    });
  }

  loadPayrollRuns(): void {
    this.loadingPayroll = true;
    this.svc.listPayrollRuns().subscribe({
      next: (d) => { this.payrollRuns = d; this.loadingPayroll = false; },
      error: () => { this.loadingPayroll = false; }
    });
  }

  viewPayrollItems(runId: string): void {
    this.svc.getPayrollItems(runId).subscribe({ next: (d) => { this.selectedRunItems = d; } });
  }

  createPayrollRun(): void {
    if (this.saving || !this.payrollForm.periodLabel) {
      this.errorMsg = 'Period label is required.';
      return;
    }
    this.saving = true;
    this.errorMsg = null;
    this.svc.createPayrollRun(this.payrollForm).subscribe({
      next: () => {
        this.saving = false;
        this.showPayrollForm = false;
        this.payrollForm = { periodLabel: '', currency: 'PKR' };
        this.loadPayrollRuns();
      },
      error: (e) => { this.saving = false; this.errorMsg = e?.error?.message ?? 'Failed to create payroll run.'; }
    });
  }

  processPayrollRun(id: string): void {
    this.svc.processPayrollRun(id).subscribe({ next: () => this.loadPayrollRuns() });
  }

  periodLabel(periodId: string): string {
    return this.periods.find(p => p.id === periodId)?.label ?? periodId.substring(0, 8);
  }

  parseNum(v: string): number { return parseFloat(v) || 0; }

  expenseStatusClass(s: string): string {
    if (s === 'PAID')     return 'badge-green';
    if (s === 'APPROVED') return 'badge-green';
    if (s === 'REJECTED' || s === 'VOIDED') return 'badge-red';
    if (s === 'SUBMITTED') return 'badge-amber';
    return 'badge-grey';
  }

  periodStatusClass(s: string): string {
    if (s === 'OPEN')   return 'badge-green';
    if (s === 'LOCKED') return 'badge-red';
    return 'badge-grey';
  }
}
