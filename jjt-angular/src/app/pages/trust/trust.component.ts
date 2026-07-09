import { Component } from '@angular/core';

import { RouterLink } from '@angular/router';
import { SiteHeaderComponent } from '../../components/layout/site-header.component';
import { SiteFooterComponent } from '../../components/layout/site-footer.component';

@Component({
  selector: 'app-trust',
  standalone: true,
  imports: [RouterLink, SiteHeaderComponent, SiteFooterComponent],
  templateUrl: './trust.component.html',
})
export class TrustComponent {}
