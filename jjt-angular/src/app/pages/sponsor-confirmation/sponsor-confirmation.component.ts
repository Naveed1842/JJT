import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-sponsor-confirmation',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './sponsor-confirmation.component.html'
})
export class SponsorConfirmationComponent {}
