# TypingRaceSimulator

Object Oriented Programming Project — ECS414U

## Project Structure

```
TypingRaceSimulator/
├── Part1/               # Textual simulation (Java, command-line)
│   ├── Typist.java      # Typist class — complete implementation
│   ├── TypingRace.java  # Race engine — bugs fixed
│   └── TypistTest.java  # Test suite (46 tests)
├── Part2/               # GUI simulation (to be completed)
└── README.md
```

---

## Part 1 — Textual Simulation

### Requirements
- Java Development Kit (JDK) 11 or higher
- No external libraries required

### How to compile

```bash
cd Part1
javac Typist.java TypingRace.java
```

### How to run the race

The race is started by calling `startRace()` on a `TypingRace` object.
A `main` method is already included in `TypingRace.java`:

```bash
java TypingRace
```

This runs a race with three typists:
- TURBOFINGERS — accuracy 0.85
- QWERTY_QUEEN  — accuracy 0.60
- HUNT_N_PECK   — accuracy 0.30

### How to run the tests

```bash
javac Typist.java TypistTest.java
java TypistTest
```

Expected output: **46 passed, 0 failed**

---

## Part 2 — GUI Simulation

Place all GUI-related source files in the `Part2/` folder.
The graphical version is started by calling `startRaceGUI()`.


```bash
java TypingRaceGUI
```
---

## Notes

- All code compiles and runs using standard command-line tools — no IDE required.
- The starter code in Part1 originally contained deliberate bugs; all 7 have been identified and fixed (see Report.pdf).
