import { test, expect } from '@playwright/test';

// Runs without auth state — tests the login page itself
test.use({ storageState: { cookies: [], origins: [] } });

test.describe('Login', () => {
  test('valid credentials redirect to admin dashboard', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel(/email/i).fill('admin@jjt.org');
    await page.getByLabel(/password/i).fill('Admin@JJT2024!');
    await page.getByRole('button', { name: /sign in|login/i }).click();

    await expect(page).toHaveURL(/\/admin/, { timeout: 10_000 });
  });

  test('wrong password shows error message', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel(/email/i).fill('admin@jjt.org');
    await page.getByLabel(/password/i).fill('wrongpassword');
    await page.getByRole('button', { name: /sign in|login/i }).click();

    await expect(page.getByText(/invalid|incorrect|unauthorized/i)).toBeVisible({ timeout: 5_000 });
    await expect(page).not.toHaveURL(/\/admin/);
  });

  test('empty form shows validation feedback', async ({ page }) => {
    await page.goto('/login');
    await page.getByRole('button', { name: /sign in|login/i }).click();

    // Should stay on login — not navigate away
    await expect(page).toHaveURL(/\/login/);
  });
});
