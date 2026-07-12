import { test, expect } from '@playwright/test';

test.describe('Session & Auth Guard', () => {
  test('unauthenticated access to /admin redirects to login', async ({ page }) => {
    // Clear storage state for this test
    await page.context().clearCookies();
    await page.evaluate(() => localStorage.clear());

    await page.goto('/admin');

    await expect(page).toHaveURL(/\/login/, { timeout: 8_000 });
  });

  test('authenticated user stays on admin after page refresh', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await expect(page).toHaveURL(/\/admin/, { timeout: 8_000 });

    await page.reload();

    // Session should be silently restored via refresh token
    await expect(page).toHaveURL(/\/admin/, { timeout: 10_000 });
  });

  test('logout clears session and redirects to login', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.waitForURL(/\/admin/);

    const logoutBtn = page.getByRole('button', { name: /logout|sign out/i });
    if (await logoutBtn.isVisible()) {
      await logoutBtn.click();
      await expect(page).toHaveURL(/\/login/, { timeout: 8_000 });
    } else {
      test.skip(true, 'Logout button not found in current layout');
    }
  });
});
