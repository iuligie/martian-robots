package martianrobots

import kotlin.test.Test
import kotlin.test.assertEquals

class SimulationTest {

    private fun runMission(input: String): List<String> =
        Simulation.run(InputParser().parse(input)).map(Robot::report)

    @Test
    fun `sample input from the brief produces the sample output`() {
        val input = """
            5 3
            1 1 E
            RFRFRFRF
            3 2 N
            FRRFLLFFRRFLL
            0 3 W
            LLFFFLFLFL
        """.trimIndent()

        assertEquals(listOf("1 1 E", "3 3 N LOST", "2 3 S"), runMission(input))
    }

    @Test
    fun `robot that stays on the grid is not lost`() {
        assertEquals(listOf("2 2 N"), runMission("5 5\n0 0 N\nFFRFFL"))
    }

    @Test
    fun `robot is lost off each edge and reports its last position on the grid`() {
        val input = """
            2 2
            1 2 N
            F
            2 1 E
            F
            1 0 S
            F
            0 1 W
            F
        """.trimIndent()

        assertEquals(listOf("1 2 N LOST", "2 1 E LOST", "1 0 S LOST", "0 1 W LOST"), runMission(input))
    }

    @Test
    fun `instructions after a robot is lost are not carried out`() {
        assertEquals(listOf("0 1 N LOST"), runMission("1 1\n0 0 N\nFFRRFF"))
    }

    @Test
    fun `scent makes a later robot ignore the move off the edge and carry on`() {
        val input = """
            3 3
            1 3 N
            F
            1 2 N
            FFRF
        """.trimIndent()

        // Second robot reaches the scented point, ignores the fatal F, turns right and keeps moving.
        assertEquals(listOf("1 3 N LOST", "2 3 E"), runMission(input))
    }

    @Test
    fun `scent applies to the grid point, not the direction the first robot left in`() {
        // First robot is lost going north from the corner 3 3; the second tries to leave east from the same point.
        val input = """
            3 3
            3 3 N
            F
            3 3 E
            F
        """.trimIndent()

        assertEquals(listOf("3 3 N LOST", "3 3 E"), runMission(input))
    }

    @Test
    fun `scent only protects the point it was left on`() {
        val input = """
            3 3
            1 3 N
            F
            2 3 N
            F
        """.trimIndent()

        assertEquals(listOf("1 3 N LOST", "2 3 N LOST"), runMission(input))
    }

    @Test
    fun `robot with no scent at its point is lost even if the previous robot was saved by a scent`() {
        val input = """
            3 3
            0 3 N
            F
            0 3 N
            F
            0 0 S
            F
        """.trimIndent()

        assertEquals(listOf("0 3 N LOST", "0 3 N", "0 0 S LOST"), runMission(input))
    }

    @Test
    fun `on a single point grid the first robot is lost and its scent saves the next`() {
        assertEquals(listOf("0 0 W LOST", "0 0 N"), runMission("0 0\n0 0 W\nF\n0 0 N\nFFFF"))
    }

    @Test
    fun `turning a full circle in either direction returns to the original orientation`() {
        assertEquals(listOf("0 0 N", "0 0 N"), runMission("1 1\n0 0 N\nLLLL\n0 0 N\nRRRR"))
    }

    @Test
    fun `a new command gets the edge and scent rules without implementing them`() {
        // Moves one point backwards while keeping its orientation. It knows nothing about the grid or scents.
        val moveBackward = Command { robot ->
            robot.copy(position = robot.position.step(robot.direction.turnLeft().turnLeft()))
        }
        val commands = CommandSet.STANDARD.with('B', moveBackward)
        val input = """
            5 5
            2 2 N
            BBF
            2 0 N
            B
            2 0 N
            BF
        """.trimIndent()

        val results = Simulation.run(InputParser(commands).parse(input)).map(Robot::report)

        // Moves normally; is lost backing off the south edge; a later robot is saved by the scent it left.
        assertEquals(listOf("2 1 N", "2 0 N LOST", "2 1 N"), results)
    }
}
