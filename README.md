# Algorithm Race Visualiser

A side-by-side terminal visualizer and benchmarking tool for classic sorting and searching algorithms, implemented in pure Java.

Algorithm Race renders multi-panel ASCII animations in real time, illustrating how different algorithms operate on identical datasets before evaluating their performance through micro-benchmarks.

---

## Showcase

https://github.com/user-attachments/assets/60a2e828-0fcb-4792-b503-4bb7d2ccbecf

---

## Overview

The application demonstrates algorithm behavior across two distinct phases:

1. **Sorting Race**: Compares **Bubble Sort**, **Insertion Sort**, and **Selection Sort** simultaneously on the same randomized dataset. Real-time operation counters track comparisons and swaps while active elements are highlighted frame-by-frame.
2. **Search Race**: Evaluates **Linear Search**, **Binary Search**, and **Jump Search** on the sorted output across two test cases:
   - **Target Present**: Locating the median array value.
   - **Target Absent**: Searching for an out-of-bounds value to observe worst-case scan behavior.
3. **Performance Report**: Executes automated trials on larger sample sizes ($n = 1200$) to measure wall-clock execution time and calculate average probe counts per key.

---

## Features

- **Multi-Panel ASCII Rendering**: Synchronized side-by-side terminal visualization.
- **Real-Time Operation Tracking**: Live counters for comparisons, element swaps, and search probes.
- **Zero External Dependencies**: Built entirely with standard Java (JDK 8+).
- **Terminal Color Support**: ANSI-colored highlights with automated fallback via the standard `NO_COLOR` environment variable.
- **Automated Validation**: Verifies array ordering and search correctness after every run.

---

## Visual Legend

| Symbol | Color | Description |
| :---: | :---: | :--- |
| `#` | Default | Bar representation of an element value |
| `^` | Yellow | Comparison between two elements |
| `X` | Red | Swap operation between two elements |
| `?` | Cyan | Probe / index inspection during search |
| `!` | Green | Successful target discovery |

---

## Project Structure

```
algorithmrace/
├── src/
│   └── algorithmrace/
│       ├── AlgorithmRace.java   # Main entry point and CLI coordinator
│       ├── Race.java            # Algorithm logic, generator, and frame recorder
│       ├── Visualizer.java      # Terminal renderer and ANSI chart engine
│       ├── Report.java          # Benchmark runner (n=1200) and reporter
│       └── Text.java            # ANSI styling, colors, and NO_COLOR support
├── docs/
│   ├── showcase.mp4             # High-definition video demonstration
│   └── screenshots/             # High-resolution stage captures
├── .gitignore
├── LICENSE
└── README.md
```

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 8 or higher (tested on OpenJDK 21).
- **Terminal**: Any terminal supporting standard ANSI escape codes (minimum 110 columns × 24 rows recommended).

### Compilation & Execution

Compile all source files into a `bin/` directory and run the application:

```bash
# Compile
javac -d bin src/algorithmrace/*.java

# Run
java -cp bin algorithmrace.AlgorithmRace
```

### Configuration Options

Upon startup, the program prompts for dataset parameters:
- **Array size ($4$–$30$)**: Number of elements to visualize (default: `24`).
- **Random seed**: Seed for reproducible pseudorandom array generation (default: `42`).

To run in monochrome mode, set `NO_COLOR`:
```bash
NO_COLOR=1 java -cp bin algorithmrace.AlgorithmRace
```

---

## Contributing

Contributions and pull requests are welcome. If you would like to add more algorithms or suggest improvements, feel free to submit a pull request directly.

### Contributors

- [@ashwarth-vsv](https://github.com/ashwarth-vsv) — Sorting and searching algorithm implementations
- [@siddarthsc10-cloud](https://github.com/siddarthsc10-cloud) — Benchmarking engine and performance reporting (`Report.java`)

---

## License

This project is open-source and available under the [MIT License](LICENSE).

**AI Disclosure**: Portions of the Java source code were developed with AI pair-programming assistance. All implementations have been reviewed and verified by the maintainer.
