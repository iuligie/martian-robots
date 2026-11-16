package martianrobots

import kotlin.test.Test
import kotlin.test.assertEquals

/** Commands are pure: they only propose a next state, with no grid or scent involved. */
class CommandsTest {

    private val robot = Robot(Coordinate(2, 2), Direction.N)

    @Test
    fun `turn left changes orientation only`() {
        assertEquals(Robot(Coordinate(2, 2), Direction.W), TurnLeft.propose(robot))
    }

    @Test
    fun `turn right changes orientation only`() {
        assertEquals(Robot(Coordinate(2, 2), Direction.E), TurnRight.propose(robot))
    }

    @Test
    fun `move forward proposes the next point even if it would be off any grid`() {
        val atOrigin = Robot(Coordinate(0, 0), Direction.S)

        assertEquals(Robot(Coordinate(0, -1), Direction.S), MoveForward.propose(atOrigin))
    }
}
