import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { AdminDataService } from '../../services/admin-data.service';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, TableModule, ButtonModule, CardModule],
  templateUrl: './admin-reports.component.html',
  styleUrl: './admin-reports.component.css'
})
export class AdminReportsComponent {
  rows: Record<string, string>[] = [];
  headers: string[] = [];

  constructor(private dataService: AdminDataService) {
    this.dataService.getReportRows().subscribe(rows => {
      this.rows = rows;
      this.headers = rows.length ? Object.keys(rows[0]) : [];
    });
  }

  exportCsv(filename: string) {
    const headers = Object.keys(this.rows[0] || {});
    const csv = [
      headers.join(','),
      ...this.rows.map(row => headers.map(h => `"${(row[h] || '').replace(/"/g, '""')}"`).join(','))
    ].join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
  }
}
