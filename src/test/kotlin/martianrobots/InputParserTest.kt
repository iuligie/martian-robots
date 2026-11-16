package martianrobots

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InputParserTest {

    private val parser = InputParser()

    private fun assertRejected(input: String, expectedMessage: String) {
        val error = assertFailsWith<InputException> { parser.parse(input) }
        assertEquals(expectedMessage, error.message)
    }

    @Test
    fun `parses grid, robots and instructions`() {
        val mission = parser.parse("5 3\n1 1 E\nRFL\n")

        assertEquals(Coordinate(5, 3), mission.upperRight)
        assertEquals(Robot(Coordinate(1, 1), Direction.E), mission.robots.single().start)
        assertEquals(listOf(TurnRight, MoveForward, TurnLeft), mission.robots.single().instructions)
    }

    @Test
    fun `tolerates extra whitespace and blank lines`() {
        val mission = parser.parse("\n  5   3  \n\n 1\t1  E \n RF \n\n")

        assertEquals(1, mission.robots.size)
    }

    @Test
    fun `grid with no robots is valid and produces no output`() {
        assertEquals(emptyList(), parser.parse("5 3").robots)
    }

    @Test
    fun `accepts the maximum coordinate and the longest instruction string`() {
        val mission = parser.parse("50 50\n50 50 N\n" + "L".repeat(99))

        assertEquals(99, mission.robots.single().instructions.size)
    }

    @Test
    fun `rejects empty input`() =
        assertRejected("  \n", "Input is empty: expected grid size on the first line")

    @Test
    fun `rejects malformed grid line`() =
        assertRejected("5\n1 1 E\nF", "Line 1: expected grid size as 'x y', got '5'")

    @Test
    fun `rejects coordinates above the maximum`() =
        assertRejected("51 3", "Line 1: coordinate 51 must be between 0 and 50")

    @Test
    fun `rejects negative coordinates`() =
        assertRejected("5 3\n-1 1 E\nF", "Line 2: coordinate -1 must be between 0 and 50")

    @Test
    fun `rejects non-numeric coordinates`() =
        assertRejected("5 x", "Line 1: 'x' is not a whole number")

    @Test
    fun `rejects unknown orientation`() =
        assertRejected("5 3\n1 1 Q\nF", "Line 2: unknown orientation 'Q', expected one of N, E, S, W")

    @Test
    fun `rejects robot starting outside the grid`() =
        assertRejected("5 3\n6 1 E\nF", "Line 2: robot starts at 6 1, outside the grid 0 0 to 5 3")

    @Test
    fun `rejects unknown instruction`() =
        assertRejected("5 3\n1 1 E\nFXF", "Line 3: unknown instruction 'X', expected one of L, R, F")

    @Test
    fun `rejects instruction strings of 100 characters or more`() =
        assertRejected("5 3\n1 1 E\n" + "F".repeat(100), "Line 3: instruction string must be less than 100 characters, got 100")

    @Test
    fun `rejects robot with no instruction line`() =
        assertRejected("5 3\n1 1 E\nF\n2 2 N", "Line 4: robot position has no instruction line")

    @Test
    fun `a missing instruction line mid-file is reported where the input goes wrong`() =
        assertRejected("5 3\n1 1 E\n2 2 N\nFF", "Line 3: unknown instruction '2', expected one of L, R, F")
}
