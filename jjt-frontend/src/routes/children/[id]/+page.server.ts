import type { PageServerLoad } from './$types';
import { apiFetch } from '$lib/api/client';
import type { ChildDto, LedgerDto, ProgressUpdateDto } from '$lib/types/api';

export const load: PageServerLoad = async ({ params }) => {
	const childId = params.id;
	const [child, ledger, progress] = await Promise.all([
		apiFetch<ChildDto>(`/api/org/children/${childId}`),
		apiFetch<LedgerDto>(`/api/org/children/${childId}/ledger`),
		apiFetch<ProgressUpdateDto[]>(`/api/org/children/${childId}/progress`)
	]);

	return { child, ledger, progress };
};
