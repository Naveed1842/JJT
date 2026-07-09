import { Component, Input } from '@angular/core';


@Component({
  selector: 'app-reassuring-loader',
  standalone: true,
  imports: [],
  templateUrl: './reassuring-loader.component.html'
})
export class ReassuringLoaderComponent {
  @Input() message = 'Loading...';
  @Input() subMessage = '';
  @Input() showProgress = false;
}
