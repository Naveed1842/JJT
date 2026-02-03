/** @type {import('tailwindcss').Config} */
export default {
	content: ['./src/**/*.{html,js,svelte,ts}'],
	theme: {
		extend: {
			fontFamily: {
				serif: ['Lora', 'Georgia', 'serif'],
				sans: ['Open Sans', 'system-ui', 'sans-serif']
			}
		}
	},
	plugins: []
};
