import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reassuring-loader',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reassuring-loader.component.html'
})
export class ReassuringLoaderComponent {
  @Input() message = 'Loading...';
  @Input() subMessage = '';
  @Input() showProgress = false;
}
