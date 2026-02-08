import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ChartModule } from 'primeng/chart';
import { AdminDataService, DashboardSummary } from '../../services/admin-data.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, CardModule, TagModule, ChartModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  summary?: DashboardSummary;
  chartData: any;
  chartOptions: any;

  constructor(private dataService: AdminDataService) {}

  ngOnInit(): void {
    this.dataService.getDashboardSummary().subscribe(summary => this.summary = summary);
    this.dataService.getCoverageTrend().subscribe(trend => {
      this.chartData = {
        labels: trend.labels,
        datasets: [
          {
            label: 'Monthly coverage (%)',
            data: trend.data,
            fill: false,
            borderColor: '#f97316',
            tension: 0.3
          }
        ]
      };
      this.chartOptions = {
        plugins: { legend: { display: false } },
        scales: {
          y: { suggestedMin: 0, suggestedMax: 100, ticks: { stepSize: 20 } }
        }
      };
    });
  }
}
