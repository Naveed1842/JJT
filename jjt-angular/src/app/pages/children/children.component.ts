import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface Child {
  childId: string;
  name: string;
  birthDate: string;
  currentGrade: string;
  sponsorshipStatus: string;
}

@Component({
  selector: 'app-children',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './children.component.html',
  styleUrl: './children.component.css'
})
export class ChildrenComponent implements OnInit {
  children: Child[] = [];
  loading = true;
  error: string | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadChildren();
  }

  loadChildren() {
    this.http.get<Child[]>('http://localhost:8080/api/org/children')
      .subscribe({
        next: (data) => {
          this.children = data;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to load children';
          this.loading = false;
          console.error('Error loading children:', err);
        }
      });
  }
}
