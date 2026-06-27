import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  error: string | null = null;

  constructor(private auth: AuthService, private router: Router) {}

  async onSubmit(): Promise<void> {
    this.loading = true;
    this.error = null;
    try {
      await this.auth.login(this.email, this.password);
      const user = this.auth.getCurrentUser();
      await this.router.navigate(user?.role === 'SPONSOR' ? ['/'] : ['/admin']);
    } catch (err: any) {
      this.error = err?.error?.message ?? 'Invalid email or password.';
    } finally {
      this.loading = false;
    }
  }
}
