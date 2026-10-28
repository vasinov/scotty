# Scotty

Scotty is a quantum computing framework for Scala with a JVM state-vector simulator. Describe circuits with Scala values, run them locally, inspect amplitudes and probabilities, and sample classical measurement results.

These docs cover **Scotty 0.6.0**, published for **Scala 2.12 and 2.13**. Scala 3 artifacts are not published. The repository's `0.6.1-SNAPSHOT` version is for development.

[Get started](getting-started/index.md) with installation and a complete Bell-pair example, or browse the [gate reference](standard-gates/index.md).

## What you can do

- Build circuits from standard gates, controlled gates and custom single-qubit matrices.
- Initialize registers with basis states or custom qubits.
- Inspect a simulated state without measuring it.
- Run repeated experiments and collect measurement counts.

The simulator runs on your CPU. Its state vector grows exponentially with the number of qubits; see [practical limits](quantum-context-and-simulator/index.md#memory-and-performance). This release does not provide a hardware backend or circuit export to OpenQASM or Quil.

## Project home

[github.com/vasinov/scotty](https://github.com/vasinov/scotty) is Scotty's home for source code, issues, contributions and documentation sources. These documentation pages are built from that repository and hosted on GitHub Pages.
