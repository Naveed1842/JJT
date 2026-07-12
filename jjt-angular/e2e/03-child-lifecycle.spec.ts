import { test, expect } from '@playwright/test';
import { adminToken, createChild } from './helpers/api';

test.describe('Child Management', () => {
  test('child created via API appears in the children list', async ({ page, request }) => {
    const token = await adminToken(request);
    const child = await createChild(request, token, { fullName: 'Playwright Test Child' });

    await page.goto('/admin/children');
    await expect(page.getByText('Playwright Test Child')).toBeVisible({ timeout: 10_000 });

    expect(child.childId).toBeTruthy();
  });

  test('children list shows availability status', async ({ page }) => {
    await page.goto('/admin/children');

    const statusChip = page.getByText(/available|reserved|allocated/i).first();
    await expect(statusChip).toBeVisible({ timeout: 8_000 });
  });

  test('child detail panel opens on selection', async ({ page, request }) => {
    const token = await adminToken(request);
    await createChild(request, token, { fullName: 'Detail Panel Child' });

    await page.goto('/admin/children');
    await page.getByText('Detail Panel Child').click();

    // Detail panel should show child info
    await expect(page.getByText(/roll number|campus|ledger/i).first()).toBeVisible({ timeout: 8_000 });
  });
});
