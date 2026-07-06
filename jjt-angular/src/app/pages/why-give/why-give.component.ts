import { Component } from '@angular/core';

import { RouterLink } from '@angular/router';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';

@Component({
  selector: 'app-why-give',
  standalone: true,
  imports: [RouterLink, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './why-give.component.html',
})
export class WhyGiveComponent {}
