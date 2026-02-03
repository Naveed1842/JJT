<svelte:head>
	<link
		href="https://fonts.googleapis.com/css2?family=Crimson+Pro:wght@400;600;700&family=Inter:wght@300;400;500&family=Lora:ital,wght@0,400;0,700;1,400&family=Open+Sans:wght@300;400;600&display=swap"
		rel="stylesheet"
	/>
</svelte:head>

<main class="min-h-screen bg-[#f4f1ea] text-gray-800 font-['Open Sans']">
	<!-- Hero Section -->
	<section class="relative flex h-[600px] items-center px-6 text-white md:px-20">
		<div
			class="absolute inset-0 bg-[url('/assets/hero/hero-background.jpg')] bg-cover bg-center"
			aria-hidden="true"
		></div>
		<div class="absolute inset-0 bg-black/40" aria-hidden="true"></div>
		<div class="relative z-10 max-w-2xl">
			<h1 class="mb-4 font-['Crimson_Pro'] text-4xl font-bold leading-tight md:text-6xl">
				Some children don't go to school.
				<br />
				They go to work.
			</h1>
			<p class="mb-8 font-['Inter'] text-lg font-light opacity-90 md:text-xl">
				In Pakistan, many orphaned children are forced to collect garbage to survive.
				Education is their only way out.
			</p>
			<div class="flex flex-wrap gap-4">
				<button class="rounded-sm bg-[#d95f23] px-8 py-3 text-lg font-semibold shadow-lg transition-colors hover:bg-[#b84d1a]">
					Sponsor a Child's Education
				</button>
				<button
					class="rounded-sm border border-white px-8 py-3 text-lg font-semibold transition-colors hover:bg-white hover:text-black"
				>
					Learn How It Works
				</button>
			</div>
		</div>
	</section>

	<!-- What Happens Section -->
	<section
		class="-mt-10 relative z-10 bg-[#fdfbf7] px-4 py-16 [clip-path:polygon(0_5%,5%_0,10%_5%,15%_2%,20%_7%,25%_3%,30%_8%,35%_4%,40%_9%,45%_5%,50%_10%,55%_6%,60%_11%,65%_7%,70%_12%,75%_8%,80%_13%,85%_9%,90%_14%,95%_10%,100%_15%,100%_100%,0_100%)] [mask-image:url('https://www.transparenttextures.com/patterns/asfalt-dark.png')] bg-[url('https://www.transparenttextures.com/patterns/paper-fibers.png')]"
	>
		<div class="mx-auto max-w-5xl text-center">
			<div class="mb-12 flex items-center justify-center gap-4">
				<div class="h-px w-full bg-gradient-to-r from-transparent via-[#ccc] to-transparent"></div>
				<h2 class="whitespace-nowrap font-['Lora'] text-2xl font-bold text-gray-700 md:text-3xl">
					What happens when education stops?
				</h2>
				<div class="h-px w-full bg-gradient-to-r from-transparent via-[#ccc] to-transparent"></div>
			</div>
			<div class="grid grid-cols-2 gap-8 md:grid-cols-4">
				<div class="flex flex-col items-center">
					<div class="mb-4 h-[150px] w-[200px] rounded-lg bg-gray-200"></div>
					<p class="text-sm font-semibold uppercase tracking-wider">School fees unpaid</p>
				</div>
				<div class="flex flex-col items-center">
					<div class="mb-4 h-[150px] w-[200px] rounded-lg bg-gray-200"></div>
					<p class="text-sm font-semibold uppercase tracking-wider">Children leave classrooms</p>
				</div>
				<div class="flex flex-col items-center">
					<div class="mb-4 h-[150px] w-[200px] rounded-lg bg-gray-200"></div>
					<p class="text-sm font-semibold uppercase tracking-wider">Forced into labor</p>
				</div>
				<div class="flex flex-col items-center">
					<div class="mb-4 h-[150px] w-[200px] rounded-lg bg-gray-200"></div>
					<p class="text-sm font-semibold uppercase tracking-wider">Childhood ends early</p>
				</div>
			</div>
		</div>
	</section>

	<!-- Education Can't Wait Section -->
	<section class="border-y border-gray-300 bg-[#e9e4d9] px-4 py-16">
		<div class="mx-auto max-w-4xl text-center">
			<div class="mb-6 flex items-center justify-center gap-4">
				<div class="h-px w-full bg-gradient-to-r from-transparent via-[#ccc] to-transparent"></div>
				<h2 class="font-['Lora'] text-3xl font-bold text-gray-800 md:text-4xl">
					Education can't wait for sponsorship.
				</h2>
				<div class="h-px w-full bg-gradient-to-r from-transparent via-[#ccc] to-transparent"></div>
			</div>
			<p class="mb-8 text-lg text-gray-600">
				Kids can't pause their lives. That's why we keep them in school while
				<br class="hidden md:block" />
				they wait for sponsors to commit.
			</p>
			<div class="flex items-center justify-center gap-4">
				<div class="h-[1px] w-12 bg-orange-600"></div>
				<p class="text-xl font-bold italic text-orange-700">Every month of school matters.</p>
				<div class="h-[1px] w-12 bg-orange-600"></div>
			</div>
		</div>
	</section>

	<!-- How We Ensure Section -->
	<section class="relative overflow-hidden bg-[#4a453e] px-4 py-16 text-white">
		<div
			class="pointer-events-none absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/paper-fibers.png')] opacity-10"
		></div>
		<div class="relative z-10 mx-auto max-w-6xl">
			<div class="mb-12 text-center">
				<div class="mb-4 flex items-center justify-center gap-4">
					<div class="h-[1px] flex-1 bg-gray-500"></div>
					<h2 class="font-['Lora'] text-3xl font-bold">How We Ensure Education Continues</h2>
					<div class="h-[1px] flex-1 bg-gray-500"></div>
				</div>
				<p class="text-sm uppercase tracking-widest text-gray-300">
					Children Stay in School
					<span class="mx-2">•</span>
					Monthly Support Tracked
					<span class="mx-2">•</span>
					<span class="text-yellow-500">Transparent Progress updates</span>
				</p>
			</div>
			<div class="mb-12 grid grid-cols-1 gap-8 md:grid-cols-3">
				<div class="overflow-hidden rounded bg-white text-gray-900 shadow-xl">
					<div class="h-64 w-full bg-gray-200"></div>
					<div class="p-6">
						<h3 class="font-['Lora'] text-2xl font-bold">Ayesha</h3>
						<p class="mb-4 text-gray-600">Age 8</p>
						<span
							class="mb-4 inline-block rounded bg-orange-700 px-3 py-1 text-xs font-bold uppercase text-white"
						>
							Needs Sponsor
						</span>
						<p class="border-t pt-4 text-sm font-semibold">Education: $30 / month</p>
					</div>
				</div>
				<div class="overflow-hidden rounded bg-white text-gray-900 shadow-xl">
					<div class="h-64 w-full bg-gray-200"></div>
					<div class="p-6">
						<h3 class="font-['Lora'] text-2xl font-bold">Bilal</h3>
						<p class="mb-4 text-gray-600">Age 10</p>
						<span
							class="mb-4 inline-block rounded bg-green-800 px-3 py-1 text-xs font-bold uppercase text-white"
						>
							Early Supported
						</span>
						<p class="border-t pt-4 text-sm font-semibold">Education: $25 / month</p>
					</div>
				</div>
				<div class="overflow-hidden rounded bg-white text-gray-900 shadow-xl">
					<div class="h-64 w-full bg-gray-200"></div>
					<div class="p-6">
						<h3 class="font-['Lora'] text-2xl font-bold">Sara</h3>
						<p class="mb-4 text-gray-600">Age 7</p>
						<span
							class="mb-4 inline-block rounded bg-orange-700 px-3 py-1 text-xs font-bold uppercase text-white"
						>
							Needs Sponsor
						</span>
						<p class="border-t pt-4 text-sm font-semibold">Education: $35 / month</p>
					</div>
				</div>
			</div>
			<div class="text-center">
				<button
					class="rounded bg-[#d35400] px-12 py-3 text-lg font-bold text-white shadow-[0_4px_0_#a04000] transition-colors hover:bg-[#e67e22]"
				>
					View All Children
				</button>
			</div>
		</div>
	</section>

	<!-- Call to Action Section -->
	<section
		class="bg-[#f4f1ea] px-4 py-20 bg-[url('https://www.transparenttextures.com/patterns/paper-fibers.png')]"
	>
		<div class="mx-auto max-w-4xl text-center">
			<div class="mb-8 flex items-center justify-center gap-4">
				<div class="h-px w-full bg-gradient-to-r from-transparent via-[#ccc] to-transparent"></div>
				<h2 class="font-['Lora'] text-2xl font-bold text-gray-800 md:text-3xl">
					You can keep a child in school and out of child labor.
				</h2>
				<div class="h-px w-full bg-gradient-to-r from-transparent via-[#ccc] to-transparent"></div>
			</div>
			<button
				class="mb-8 rounded bg-[#d35400] px-16 py-5 text-2xl font-bold text-white shadow-lg transition-colors hover:bg-[#e67e22]"
			>
				Sponsor a Child Today
			</button>
			<p class="text-xl italic text-gray-700 font-['Lora']">
				Make a difference. Change a future.
			</p>
		</div>
	</section>

	<!-- Footer -->
	<footer class="bg-[#333] px-4 py-10 text-center text-sm text-gray-400">
		<div class="mb-6 flex justify-center gap-8">
			<a class="transition-colors hover:text-white" href="/">About</a>
			<a class="transition-colors hover:text-white" href="/">How It Works</a>
			<a class="transition-colors hover:text-white" href="/">FAQs</a>
			<a class="transition-colors hover:text-white" href="/">Contact</a>
		</div>
		<p>© 2022 J17 Foundation. All rights reserved.</p>
	</footer>
</main>
