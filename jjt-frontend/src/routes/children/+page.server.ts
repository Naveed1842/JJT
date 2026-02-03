import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/api/client';
import type { ChildDto } from '$lib/types/api';

export const load: PageServerLoad = async () => {
	const children = await apiFetch<ChildDto[]>('/api/org/children');
	return { children };
};
