# Martian Robots

A Kotlin command-line solution to the Martian Robots problem: robots move around a bounded
rectangular grid, fall off the edge and are lost, and leave a "scent" that stops later robots
falling off from the same point.

## Requirements

- JDK 17 or newer. Nothing else: the Gradle wrapper downloads Gradle and Kotlin on first run.

## Build and run

```sh
# Run against the sample from the brief (file argument)
./gradlew run -q --args=examples/sample.txt

# Or pipe input on stdin
./gradlew run -q < examples/sample.txt
```

Expected output:

```
1 1 E
3 3 N LOST
2 3 S
```

To build a standalone launcher instead of going through Gradle each time:

```sh
./gradlew installDist
build/install/martian-robots/bin/martian-robots examples/sample.txt
```

Invalid input is reported on stderr with the offending line number, and the program exits with status 1:

```
$ printf '5 3\n1 1 E\nFXF\n' | build/install/martian-robots/bin/martian-robots
Invalid input. Line 3: unknown instruction 'X', expected one of L, R, F
```

## Tests

```sh
./gradlew test
```

The tests use `kotlin.test` on JUnit 5. They cover:

- the sample input and output from the brief;
- robots lost off each of the four edges, reporting their last on-grid position;
- instructions after a robot is lost not being carried out;
- scent: a later robot ignores the fatal move and carries on with its remaining instructions;
- scent being tied to a grid point, not to an edge or direction;
- input validation: coordinate limits, instruction length, unknown orientations or instructions,
  robots starting off the grid, and a missing instruction line;
- the whole program end to end through `Cli` (`CliTest`): stdin and file input, Windows line endings,
  and for every kind of failure the exact stderr message, empty stdout and exit code 1;
- the edge and scent rules in isolation (`StepperTest`), and commands as pure proposals (`CommandsTest`);
- adding a new command type (a `B`ackward command defined only in the test), which is lost and saved by
  scent exactly like `F` without implementing either rule.

## Design

```
src/main/kotlin/martianrobots/
  Model.kt        Direction, Coordinate, Robot, Mars (grid bounds + scents)
  Commands.kt     Command interface, L/R/F implementations, CommandSet registry
  Stepper.kt      Applies the grid-edge and scent rules to whatever a command proposes
  Simulation.kt   Runs each robot's instructions in turn through the Stepper
  InputParser.kt  Text input -> validated Mission; all input errors are raised here
  Main.kt         main() and Cli: read file/stdin, print results, map errors to exit code 1
```

- **Commands propose, the Stepper decides.** The brief says more command types may be needed later.
  A `Command` is a pure function, `propose(robot): Robot`, that says where the robot would be if
  nothing stopped it; it knows nothing about the grid or scents. The `Stepper` applies the brief's
  rules to every proposal: accept it if it stays on the grid, ignore it if it would leave from a
  scented point, otherwise mark the robot lost and leave a scent. So the rules are written once,
  and a new command can neither bypass nor duplicate them. To add one, implement `Command` and
  register it with `CommandSet.STANDARD.with(symbol, command)`. The test suite does exactly this with a
  one-line backward command (`B`) defined in `SimulationTest`.
  I stopped there: there is no plugin loading or configuration, because nobody has asked for it.
- **Robots are immutable values.** Each step returns a new `Robot`. The only mutable state is the
  scent set inside `Mars`, and only the `Stepper` touches it. That state really is shared between
  robots, and it's the reason robots have to run in order.
- **`main` is a one-line wrapper.** The command-line logic lives in `Cli`, which takes its streams as
  parameters and returns the exit status instead of calling `exitProcess`. That is what lets `CliTest`
  run the real program in-memory, with no subprocess and no build step.
- **Parse first, then simulate.** The input is fully validated and instruction characters are resolved
  to `Command`s before any robot moves. A bad line therefore fails the whole run with a clear message,
  rather than producing partial output that looks valid.
- **Direction turning** uses the enum's clockwise declaration order, so there is no hand-written
  left/right table to get wrong.

## Assumptions

- **Scent is per grid point, not per direction.** The brief says a move off the world "from a grid point
  from which a robot has been previously lost" is ignored. So at a scented corner, a robot is protected
  from falling off either adjacent edge, not only the one the first robot went over. There is a test for this.
- **A lost robot stops executing.** It reports the last position and orientation it had on the grid,
  followed by `LOST`.
- **Robots don't collide.** Robots run one at a time and the brief doesn't mention collisions, so
  positions are not checked against each other.
- **Out-of-range input is rejected, not clamped.** Coordinates above 50, instruction strings of 100 or
  more characters, and robots starting outside the grid all fail with an error.
- **Blank lines are ignored.** This makes trailing newlines and blank lines between robots harmless.
  The trade-off is that an empty instruction string can't be expressed. That seemed acceptable, because
  the brief describes instructions as a string of letters.
- **Commands are case-sensitive.** Instructions must be upper case (`L`, `R`, `F`) and orientations
  must be `N`, `E`, `S` or `W`, as in the brief.

## What I'd do with more time

- **Multi-square commands** (e.g. jump 2): the `Stepper` checks only where a proposal ends up, so a
  robot could appear to skip over the edge. I'd expand such commands into single steps so that scent
  is checked at every point, rather than build that now for a command nobody has asked for.
- **Commands that sense the world** (e.g. "move forward only if not scented"): pass `propose` a
  read-only view of the terrain, so commands can look but only the `Stepper` can change anything.
- **A lost robot's orientation** comes from before the fatal instruction. With a combined
  turn-and-move command that would be a decision to make; for `F` the two are the same.
- **Property-based tests** (for example with Kotest), such as "a robot never reports a position outside
  the grid" and "four turns in the same direction are a no-op", run over generated inputs.
- **Streaming input** line by line instead of reading it all into memory. That isn't needed at the
  specified sizes (50x50 grid, fewer than 100 instructions), but it would matter for very large batches.
- **Reporting every validation error** in one pass, rather than stopping at the first.
- **CI and linting:** a GitHub Actions workflow running `./gradlew test`, and ktlint or detekt.
