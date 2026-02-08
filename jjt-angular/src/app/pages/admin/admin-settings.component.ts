import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ButtonModule } from 'primeng/button';

interface AdminPreferences {
  showContactInfo: boolean;
  compactTables: boolean;
  autoRefresh: boolean;
}

@Component({
  selector: 'app-admin-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, InputSwitchModule, ButtonModule],
  templateUrl: './admin-settings.component.html',
  styleUrl: './admin-settings.component.css'
})
export class AdminSettingsComponent implements OnInit {
  preferences: AdminPreferences = {
    showContactInfo: true,
    compactTables: false,
    autoRefresh: false
  };

  savedMessage = '';

  ngOnInit(): void {
    const stored = localStorage.getItem('adminPreferences');
    if (stored) {
      this.preferences = { ...this.preferences, ...JSON.parse(stored) };
    }
  }

  save() {
    localStorage.setItem('adminPreferences', JSON.stringify(this.preferences));
    this.savedMessage = 'Saved locally (UI-only).';
    setTimeout(() => this.savedMessage = '', 2000);
  }
}
