/**
 * Editable landing page content.
 * Update these values to change the text on the page.
 */
export const landingContent = {
	hero: {
		headline: "Some children don't go to school. They go to work.",
		subheadline:
			'In Pakistan, many orphaned children are forced to collect garbage to survive. Education is their only way out.',
		image:
			"https://placehold.co/1920x1080/444/fff?text=Young+boy+carrying+heavy+sack+of+garbage+in+dusty+environment"
	},
	consequences: {
		heading: "What happens when education stops?",
		items: [
			{ title: 'School fees unpaid', image: 'https://placehold.co/200x150/dcdcdc/666?text=Dilapidated+School', alt: 'Illustration of a broken school building' },
			{ title: 'Children leave classrooms', image: 'https://placehold.co/200x150/dcdcdc/666?text=Children+Leaving', alt: 'Illustration of children leaving a classroom' },
			{ title: 'Forced into labor', image: 'https://placehold.co/200x150/dcdcdc/666?text=Child+Labor', alt: 'Illustration of a child working' },
			{ title: 'Childhood ends early', image: 'https://placehold.co/200x150/dcdcdc/666?text=End+of+Childhood', alt: 'Illustration of a sad child sitting alone' }
		]
	},
	urgency: {
		heading: "Education can't wait for sponsorship.",
		body: "Kids can't pause their lives. That's why we keep them in school while they wait for sponsors to commit.",
		emphasis: 'Every month of school matters.'
	},
	howWeEnsure: {
		heading: 'How We Ensure Education Continues',
		subpoints: ['Children Stay in School', 'Monthly Support Tracked', 'Transparent Progress updates'],
		children: [
			{ name: 'Ayesha', age: 8, status: 'Needs Sponsor', statusClass: 'bg-orange-700', amount: 30, image: 'https://placehold.co/600x400/888/fff?text=Portrait+of+Ayesha', alt: 'Portrait of Ayesha, a young girl' },
			{ name: 'Bilal', age: 10, status: 'Early Supported', statusClass: 'bg-green-800', amount: 25, image: 'https://placehold.co/600x400/777/fff?text=Portrait+of+Bilal', alt: 'Portrait of Bilal, a young boy' },
			{ name: 'Sara', age: 7, status: 'Needs Sponsor', statusClass: 'bg-orange-700', amount: 35, image: 'https://placehold.co/600x400/999/fff?text=Portrait+of+Sara', alt: 'Portrait of Sara, a young girl' }
		]
	},
	finalCta: {
		headline: 'You can keep a child in school and out of child labor.',
		tagline: 'Make a difference. Change a future.'
	},
	footer: {
		year: 2022,
		orgName: 'J17 Foundation'
	}
};
