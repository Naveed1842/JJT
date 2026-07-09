import { Component } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService, CurrentUser } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  error: string | null = null;

  constructor(
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  async onSubmit(): Promise<void> {
    this.loading = true;
    this.error = null;
    try {
      await this.auth.login(this.email, this.password);
      await this.router.navigateByUrl(this.postLoginUrl());
    } catch (err: any) {
      this.error = err?.error?.message ?? 'Invalid email or password.';
    } finally {
      this.loading = false;
    }
  }

  /**
   * Where to land after login: the returnUrl the guard attached (internal paths
   * only — never external redirects), otherwise the role's home page.
   */
  private postLoginUrl(): string {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    if (returnUrl && returnUrl.startsWith('/') && !returnUrl.startsWith('//')) {
      return returnUrl;
    }
    return this.defaultRouteFor(this.auth.getCurrentUser());
  }

  private defaultRouteFor(user: CurrentUser | null): string {
    return user?.role === 'SPONSOR' ? '/sponsor/portal' : '/admin';
  }
}
