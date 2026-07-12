import { test as setup } from '@playwright/test';

const API = process.env['E2E_API_URL'] || 'http://localhost:8080';
const ADMIN_EMAIL = process.env['E2E_ADMIN_EMAIL'] || 'admin@jjt.org';
const ADMIN_PASS  = process.env['E2E_ADMIN_PASS']  || 'Admin@JJT2024!';
const AUTH_FILE   = 'e2e/.auth/admin.json';

setup('authenticate as admin', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel(/email/i).fill(ADMIN_EMAIL);
  await page.getByLabel(/password/i).fill(ADMIN_PASS);
  await page.getByRole('button', { name: /sign in|login/i }).click();

  await page.waitForURL(/\/admin/, { timeout: 10_000 });
  await page.context().storageState({ path: AUTH_FILE });
});
