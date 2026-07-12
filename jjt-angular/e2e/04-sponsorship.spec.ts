import { test, expect } from '@playwright/test';
import { adminToken, createChild } from './helpers/api';

const API = process.env['E2E_API_URL'] || 'http://localhost:8080';

test.describe('Sponsorship Lifecycle', () => {
  test('committing a sponsorship changes child status to RESERVED', async ({ page, request }) => {
    const token = await adminToken(request);

    // Create a child
    const child = await createChild(request, token);

    // Create a sponsor
    const sponsorRes = await request.post(`${API}/api/admin/sponsors`, {
      data: {
        displayName: 'E2E Sponsor',
        contactEmail: `e2e-${Date.now()}@test.com`,
        phone: null,
      },
      headers: { Authorization: `Bearer ${token}` },
    });
    const sponsor = await sponsorRes.json();

    // Commit sponsorship (next month)
    const nextMonth = new Date();
    nextMonth.setMonth(nextMonth.getMonth() + 1);
    const startMonth = `${nextMonth.getFullYear()}-${String(nextMonth.getMonth() + 1).padStart(2, '0')}`;

    const commitRes = await request.post(`${API}/api/admin/sponsorships`, {
      data: {
        sponsorId: sponsor.sponsorId,
        childId: child.childId,
        startMonth,
        commitmentType: 'MONTHLY',
      },
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(commitRes.status()).toBe(200);

    // Verify status change in the UI
    await page.goto('/admin/children');
    await page.getByText(child.childId ? /E2E-/ : /E2E/).first().click();

    await expect(page.getByText(/reserved/i)).toBeVisible({ timeout: 8_000 });
  });
});
