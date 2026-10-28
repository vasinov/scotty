# Scotty: Quantum Computing in Scala

[![CI](https://github.com/vasinov/scotty/actions/workflows/ci.yml/badge.svg)](https://github.com/vasinov/scotty/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/xyz.entangled/scotty_2.13)](https://central.sonatype.com/artifact/xyz.entangled/scotty_2.13)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Scotty is a quantum computing framework for Scala with a JVM state-vector simulator. Build circuits, define custom gates, inspect simulated amplitudes and probabilities, and collect measurement results from Scala programs.

**[Documentation](https://vasinov.github.io/scotty/)** · **[Getting Started](https://vasinov.github.io/scotty/getting-started/)** · **[Examples](https://vasinov.github.io/scotty/examples/)** · **[Issues](https://github.com/vasinov/scotty/issues)**

## Installation

Scotty **0.6.0** is published for **Scala 2.12 and 2.13**. Add this to `build.sbt`:

```scala
libraryDependencies += "xyz.entangled" %% "scotty" % "0.6.0"
```

For Maven and Gradle, use [scotty_2.13](https://central.sonatype.com/artifact/xyz.entangled/scotty_2.13/0.6.0) or [scotty_2.12](https://central.sonatype.com/artifact/xyz.entangled/scotty_2.12/0.6.0) to match your Scala binary version. The Maven organization remains `xyz.entangled`; the repository now lives at `vasinov/scotty`. Scala 3 artifacts are not published.

## Create a Bell pair

```scala
import scotty.quantum._
import scotty.quantum.gate.StandardGate._
import scotty.simulator.QuantumSimulator

implicit val simulator: QuantumContext = QuantumSimulator()
val bell = Circuit(H(0), CNOT(0, 1))
val state = simulator.run(bell)
println(StateProbabilityReader(state).toHumanString)
```

```text
00: Amplitude: 0.707+0.000i, P: 50.00%
11: Amplitude: 0.707+0.000i, P: 50.00%
```

Measure it with `simulator.runAndMeasure(bell).toBinary` to get either `00` or `11`. Gate index `0` refers to the rightmost bit in displayed basis strings. See the [register ordering caveat](https://vasinov.github.io/scotty/circuits-and-qubits/#register-ordering) when initializing or labeling multi-qubit registers.

The simulator's state vector grows exponentially with qubit count. Any `Measure` operation causes a whole-register measurement after all gates; this release does not implement general intermediate measurements or classical feed-forward. See [simulator limits](https://vasinov.github.io/scotty/quantum-context-and-simulator/#memory-and-performance) and [measurement behavior](https://vasinov.github.io/scotty/operations/) before building larger protocols.

## Contributing

Use JDK 17 and sbt to run the library tests:

```sh
sbt +test
```

Documentation lives in [`docs/src/`](docs/src/index.md) and uses the default VitePress theme. With Node.js 22, Python 3.9+ and sbt installed:

```sh
npm ci
npm run docs:generate
npm run docs:build
npm run docs:preview
```

The documentation build compiles and executes examples against published `0.6.0` with mdoc, then validates the built site's links. The repository's `0.6.1-SNAPSHOT` version is for development. See the [contributor guide](docs/src/contributing/index.md) for the workflow and deployment details.

Bug reports and pull requests are welcome. Include tests for behavior changes and runnable examples for documentation changes.

## License

Scotty is available under the [Apache License 2.0](LICENSE).
