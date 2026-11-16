package martianrobots

class InputException(message: String) : Exception(message)

/**
 * Parses the text format from the brief into a [Mission].
 *
 * Validation happens here so the simulation only ever sees well-formed data.
 * Blank lines are ignored, so trailing newlines or blank separators between robots are harmless.
 */
class InputParser(private val commandSet: CommandSet = CommandSet.STANDARD) {

    fun parse(input: String): Mission {
        val lines = input.lines()
            .mapIndexed { index, text -> Line(index + 1, text.trim()) }
            .filter { it.text.isNotEmpty() }

        if (lines.isEmpty()) throw InputException("Input is empty: expected grid size on the first line")

        val upperRight = parseGrid(lines.first())
        val robots = lines.drop(1).chunked(2).map { pair ->
            val position = pair[0]
            val instructions = pair.getOrNull(1) ?: position.fail("robot position has no instruction line")
            RobotPlan(
                start = parseRobot(position, upperRight),
                instructions = parseInstructions(instructions),
            )
        }
        return Mission(upperRight, robots)
    }

    private fun parseGrid(line: Line): Coordinate {
        val parts = line.tokens()
        if (parts.size != 2) line.fail("expected grid size as 'x y', got '${line.text}'")
        return Coordinate(line.coordinate(parts[0]), line.coordinate(parts[1]))
    }

    private fun parseRobot(line: Line, upperRight: Coordinate): Robot {
        val parts = line.tokens()
        if (parts.size != 3) line.fail("expected robot position as 'x y orientation', got '${line.text}'")
        val position = Coordinate(line.coordinate(parts[0]), line.coordinate(parts[1]))
        val direction = Direction.entries.firstOrNull { it.name == parts[2] }
            ?: line.fail("unknown orientation '${parts[2]}', expected one of ${Direction.entries.joinToString()}")
        if (position.x > upperRight.x || position.y > upperRight.y) {
            line.fail("robot starts at ${position.x} ${position.y}, outside the grid 0 0 to ${upperRight.x} ${upperRight.y}")
        }
        return Robot(position, direction)
    }

    private fun parseInstructions(line: Line): List<Command> {
        if (line.text.length >= MAX_INSTRUCTION_LENGTH) {
            line.fail("instruction string must be less than $MAX_INSTRUCTION_LENGTH characters, got ${line.text.length}")
        }
        return line.text.map { symbol ->
            commandSet.lookup(symbol)
                ?: line.fail("unknown instruction '$symbol', expected one of ${commandSet.symbols.joinToString()}")
        }
    }

    private data class Line(val number: Int, val text: String) {
        fun tokens(): List<String> = text.split(WHITESPACE)

        fun coordinate(token: String): Int {
            val value = token.toIntOrNull() ?: fail("'$token' is not a whole number")
            if (value !in 0..MAX_COORDINATE) fail("coordinate $value must be between 0 and $MAX_COORDINATE")
            return value
        }

        fun fail(reason: String): Nothing = throw InputException("Line $number: $reason")
    }

    companion object {
        const val MAX_COORDINATE = 50
        const val MAX_INSTRUCTION_LENGTH = 100
        private val WHITESPACE = Regex("\\s+")
    }
}
