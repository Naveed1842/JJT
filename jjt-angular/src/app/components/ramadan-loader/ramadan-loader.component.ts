import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ramadan-loader',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ramadan-loader.component.html'
})
export class RamadanLoaderComponent {
  @Input() message = 'Preparing your support options...';
}
