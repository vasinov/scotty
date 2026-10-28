# Contributing

Scotty's source, documentation and issue tracker live at [vasinov/scotty](https://github.com/vasinov/scotty).

## Build and test

Use **JDK 17**, **sbt** (the repository pins its version), **Node.js 22** and **Python 3.9 or newer**. The library cross-builds for Scala 2.12 and 2.13.

```sh
git clone https://github.com/vasinov/scotty.git
cd scotty
sbt +test
npm ci
npm run docs:generate
npm run docs:build
npm run docs:preview
```

Open the preview at `http://localhost:4173/scotty/`. The build checks local links, assets and anchors under the same `/scotty/` base path used in production.

## Edit documentation

Edit Markdown in `docs/src/`. The default VitePress theme is configured in `docs/.vitepress/config.mts`; there is no custom theme. Runnable Scala fences use `scala mdoc:silent`; setup and assertions can use `scala mdoc:invisible`. Fences on one page share a session; use `mdoc:reset:silent` to start an independent example.

`docs-build/` is a separate sbt build using the published `xyz.entangled:scotty_2.13:0.6.0` dependency. This keeps the release documentation honest even when development sources change. Update that dependency and the documented version together for a future release.

Run `npm run docs:generate` after changing sources. It checks examples and creates `docs/generated/`, which is ignored by Git. `npm run docs:build` builds those generated pages and checks their links. For live editing, run `sbt 'mdoc --watch'` in `docs-build/` and `npm run docs:dev` in a second terminal.

## Continuous integration and deployment

GitHub Actions runs the library tests on both Scala binary versions, checks released examples with mdoc, and builds and validates the documentation. Successful pushes to `master` deploy to [GitHub Pages](https://vasinov.github.io/scotty/). Pull requests validate without deploying.

Pages uses the **GitHub Actions** source in repository settings. Builds upload `docs/.vitepress/dist` as a Pages artifact; deployment waits for both library tests and documentation checks.

For code changes, include focused tests for the behavior being changed. For documentation changes, keep examples complete and executable and verify displayed output. The library's `0.6.1-SNAPSHOT` version is separate from the published `0.6.0` documentation target.
