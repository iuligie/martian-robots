package martianrobots

/**
 * A single robot instruction.
 *
 * A command only proposes the robot's next state, as if nothing could stop it. Whether that
 * state is allowed (grid edge, scent) is decided by the [Stepper], so every command, including
 * ones added later, is subject to the same rules without repeating them.
 *
 * To add a command, implement this interface and register it against a character in a [CommandSet].
 */
fun interface Command {
    fun propose(robot: Robot): Robot
}

object TurnLeft : Command {
    override fun propose(robot: Robot): Robot = robot.copy(direction = robot.direction.turnLeft())
}

object TurnRight : Command {
    override fun propose(robot: Robot): Robot = robot.copy(direction = robot.direction.turnRight())
}

object MoveForward : Command {
    override fun propose(robot: Robot): Robot = robot.copy(position = robot.position.step(robot.direction))
}

/** Maps instruction characters to commands. */
class CommandSet(private val commands: Map<Char, Command>) {

    fun lookup(symbol: Char): Command? = commands[symbol]

    val symbols: Set<Char> get() = commands.keys

    /** A copy of this set with [command] registered against [symbol], e.g. `CommandSet.STANDARD.with('B', moveBackward)`. */
    fun with(symbol: Char, command: Command): CommandSet = CommandSet(commands + (symbol to command))

    companion object {
        val STANDARD = CommandSet(
            mapOf(
                'L' to TurnLeft,
                'R' to TurnRight,
                'F' to MoveForward,
            ),
        )
    }
}
