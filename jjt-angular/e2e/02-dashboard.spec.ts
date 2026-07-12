import { test, expect } from '@playwright/test';

// Uses the stored admin auth state from auth.setup.ts
test.describe('Admin Dashboard', () => {
  test('renders dashboard with fund summary cards', async ({ page }) => {
    await page.goto('/admin/dashboard');

    await expect(page.getByText(/fund|balance/i).first()).toBeVisible({ timeout: 8_000 });
  });

  test('navigates to children section', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.getByRole('link', { name: /children/i }).first().click();

    await expect(page).toHaveURL(/\/admin\/children|\/admin#children/);
    await expect(page.getByText(/child|roll/i).first()).toBeVisible({ timeout: 8_000 });
  });

  test('navigates to sponsors section', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.getByRole('link', { name: /sponsor/i }).first().click();

    await expect(page.getByText(/sponsor/i).first()).toBeVisible({ timeout: 8_000 });
  });

  test('navigates to reconciliation section', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.getByRole('link', { name: /reconciliation|payment/i }).first().click();

    await expect(page.getByText(/reconciliation|payment/i).first()).toBeVisible({ timeout: 8_000 });
  });
});
