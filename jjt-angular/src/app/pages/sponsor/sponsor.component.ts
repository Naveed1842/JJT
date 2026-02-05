import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface SponsoredChild {
  childId: string;
  name: string;
  birthDate: string;
  currentGrade: string;
  sponsorshipStatus: string;
}

@Component({
  selector: 'app-sponsor',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './sponsor.component.html',
  styleUrl: './sponsor.component.css'
})
export class SponsorComponent implements OnInit {
  children: SponsoredChild[] = [];
  loading = true;
  error: string | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadSponsoredChildren();
  }

  loadSponsoredChildren() {
    this.http.get<SponsoredChild[]>('http://localhost:8080/api/sponsors/children')
      .subscribe({
        next: (data) => {
          this.children = data;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to load sponsored children';
          this.loading = false;
          console.error('Error loading sponsored children:', err);
        }
      });
  }
}
