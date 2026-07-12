import { APIRequestContext } from '@playwright/test';

const API = process.env['E2E_API_URL'] || 'http://localhost:8080';

export async function adminToken(request: APIRequestContext): Promise<string> {
  const res = await request.post(`${API}/api/auth/login`, {
    data: {
      email: process.env['E2E_ADMIN_EMAIL'] || 'admin@jjt.org',
      password: process.env['E2E_ADMIN_PASS'] || 'Admin@JJT2024!',
    },
  });
  const body = await res.json();
  return body.accessToken as string;
}

export async function createChild(
  request: APIRequestContext,
  token: string,
  overrides: Partial<{
    rollNumber: string; fullName: string; city: string;
    campusName: string; educationAmount: string; educationCurrency: string;
  }> = {}
) {
  const payload = {
    rollNumber: `E2E-${Date.now()}`,
    fullName: 'E2E Test Child',
    city: 'Karachi',
    campusName: 'Test Campus',
    schoolName: null,
    educationAmount: '2000.00',
    educationCurrency: 'PKR',
    ...overrides,
  };
  const res = await request.post(`${API}/api/admin/children`, {
    data: payload,
    headers: { Authorization: `Bearer ${token}` },
  });
  return res.json();
}
