<script lang="ts">
	import type { ChildDto } from '$lib/types/api';

	let { data } = $props<{ data: { children: ChildDto[] } }>();
</script>

<main class="mx-auto flex min-h-screen max-w-5xl flex-col gap-6 px-6 py-12">
	<header>
		<h1 class="text-2xl font-semibold text-slate-900">Children</h1>
		<p class="mt-2 text-sm text-slate-600">
			Read-only list of registered children and monthly education cost.
		</p>
	</header>

	<section class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
		<table class="w-full text-left text-sm">
			<thead class="bg-slate-50 text-slate-600">
				<tr>
					<th class="px-6 py-3 font-medium">Child</th>
					<th class="px-6 py-3 font-medium">Monthly Cost</th>
					<th class="px-6 py-3 font-medium">Actions</th>
				</tr>
			</thead>
			<tbody class="divide-y divide-slate-200">
				{#each data.children as child}
					<tr>
						<td class="px-6 py-4 text-slate-900">{child.fullName}</td>
						<td class="px-6 py-4 text-slate-700">
							{child.educationAmount} {child.educationCurrency}
						</td>
						<td class="px-6 py-4">
							<a
								href={`/children/${child.id}`}
								class="text-sm font-semibold text-slate-900 underline"
							>
								View details
							</a>
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</section>
</main>
