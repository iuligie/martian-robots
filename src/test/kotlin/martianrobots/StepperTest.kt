package martianrobots

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The rules from the brief, tested once, independently of which command made the proposal. */
class StepperTest {

    private val mars = Mars(Coordinate(3, 3))
    private val stepper = Stepper(mars)
    private val atNorthEdge = Robot(Coordinate(1, 3), Direction.N)

    @Test
    fun `a proposal on the grid is accepted`() {
        val robot = Robot(Coordinate(1, 1), Direction.N)

        assertEquals(Robot(Coordinate(1, 2), Direction.N), stepper.step(robot, MoveForward))
    }

    @Test
    fun `a proposal off the grid loses the robot at its last position and leaves a scent there`() {
        val result = stepper.step(atNorthEdge, MoveForward)

        assertEquals(Robot(Coordinate(1, 3), Direction.N, lost = true), result)
        assertTrue(mars.hasScent(Coordinate(1, 3)))
    }

    @Test
    fun `a proposal off the grid from a scented point is ignored`() {
        mars.leaveScent(Coordinate(1, 3))

        assertEquals(atNorthEdge, stepper.step(atNorthEdge, MoveForward))
    }

    @Test
    fun `turning on a scented edge point is unaffected`() {
        mars.leaveScent(Coordinate(1, 3))

        assertEquals(Robot(Coordinate(1, 3), Direction.E), stepper.step(atNorthEdge, TurnRight))
    }

    @Test
    fun `the rules apply to any command, not just the standard ones`() {
        val jumpOffTheMap = Command { it.copy(position = Coordinate(-5, -5)) }
        val robot = Robot(Coordinate(2, 2), Direction.N)

        assertEquals(robot.copy(lost = true), stepper.step(robot, jumpOffTheMap))
        assertTrue(mars.hasScent(Coordinate(2, 2)))
        assertFalse(mars.hasScent(Coordinate(-5, -5)))
    }
}
