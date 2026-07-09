import { Component, Input } from '@angular/core';


@Component({
  selector: 'app-ramadan-loader',
  standalone: true,
  imports: [],
  templateUrl: './ramadan-loader.component.html'
})
export class RamadanLoaderComponent {
  @Input() message = 'Preparing your support options...';
}
