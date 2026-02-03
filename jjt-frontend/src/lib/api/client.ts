import { API_BASE_URL } from '$lib/config';

const DEFAULT_HEADERS: Record<string, string> = {
	'Content-Type': 'application/json',
	'X-ROLE': 'ORG_ADMIN'
};

export async function apiFetch<T>(
	path: string,
	options: RequestInit = {}
): Promise<T> {
	const headers = {
		...DEFAULT_HEADERS,
		...(options.headers as Record<string, string> | undefined)
	};

	const res = await fetch(`${API_BASE_URL}${path}`, {
		...options,
		headers
	});

	if (!res.ok) {
		throw new Error(`API error ${res.status}`);
	}

	return res.json() as Promise<T>;
}
