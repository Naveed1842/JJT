<script lang="ts">
	import type { ChildDto, LedgerDto, ProgressUpdateDto } from '$lib/types/api';

	let { data } = $props<{
		data: { child: ChildDto; ledger: LedgerDto; progress: ProgressUpdateDto[] };
	}>();
</script>

<main class="mx-auto flex min-h-screen max-w-5xl flex-col gap-8 px-6 py-12">
	<a href="/children" class="text-sm font-semibold text-slate-700 underline">
		← Back to children
	</a>

	<section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
		<h1 class="text-2xl font-semibold text-slate-900">{data.child.fullName}</h1>
		<p class="mt-2 text-sm text-slate-600">Child ID: {data.child.id}</p>
		<div class="mt-4 flex items-center gap-3 text-sm text-slate-700">
			<span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold">
				Monthly Cost
			</span>
			<span>
				{data.child.educationAmount} {data.child.educationCurrency}
			</span>
		</div>
	</section>

	<section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
		<h2 class="text-lg font-semibold text-slate-900">Education Support Ledger</h2>
		<table class="mt-4 w-full text-left text-sm">
			<thead class="bg-slate-50 text-slate-600">
				<tr>
					<th class="px-4 py-2 font-medium">Month</th>
					<th class="px-4 py-2 font-medium">Amount</th>
				</tr>
			</thead>
			<tbody class="divide-y divide-slate-200">
				{#each data.ledger.entries as entry}
					<tr>
						<td class="px-4 py-2 text-slate-900">{entry.month}</td>
						<td class="px-4 py-2 text-slate-700">
							{entry.educationAmount} {entry.educationCurrency}
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</section>

	<section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
		<h2 class="text-lg font-semibold text-slate-900">Progress Updates</h2>
		<ul class="mt-4 space-y-3 text-sm">
			{#each data.progress as update}
				<li class="rounded-lg border border-slate-200 px-4 py-3">
					<p class="text-xs font-semibold uppercase tracking-wide text-slate-500">
						{update.month}
					</p>
					<p class="mt-2 text-slate-700">{update.summary}</p>
				</li>
			{/each}
		</ul>
	</section>
</main>
