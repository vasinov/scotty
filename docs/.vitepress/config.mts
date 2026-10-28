import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'Scotty',
  description: 'Quantum computing in Scala',
  lang: 'en-US',
  base: '/scotty/',
  srcDir: 'generated',
  markdown: { math: true },
  sitemap: { hostname: 'https://vasinov.github.io/scotty/' },
  themeConfig: {
    nav: [
      { text: 'Guide', link: '/getting-started/' },
      { text: 'Contributing', link: '/contributing/' }
    ],
    sidebar: [
      { text: 'Introduction', link: '/' },
      { text: 'Getting Started', link: '/getting-started/' },
      { text: 'Quantum Context and Simulator', link: '/quantum-context-and-simulator/' },
      { text: 'Circuits and Qubits', link: '/circuits-and-qubits/' },
      { text: 'Superposition and Measurement', link: '/superposition-and-measurement/' },
      { text: 'Operations', link: '/operations/' },
      { text: 'Standard Gates', link: '/standard-gates/' },
      { text: 'Modifiers and Custom Gates', link: '/modifiers-and-custom-gates/' },
      { text: 'State Readers', link: '/state-readers/' },
      { text: 'Examples', link: '/examples/' },
      { text: 'Contributing', link: '/contributing/' }
    ],
    search: { provider: 'local' },
    outline: [2, 3],
    socialLinks: [{ icon: 'github', link: 'https://github.com/vasinov/scotty' }],
    editLink: {
      pattern: 'https://github.com/vasinov/scotty/edit/master/docs/src/:path',
      text: 'Edit this page on GitHub'
    },
    footer: { message: 'Released under the Apache License 2.0.' }
  }
})
