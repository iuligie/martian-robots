package martianrobots

import kotlin.test.Test
import kotlin.test.assertEquals

class DirectionTest {

    @Test
    fun `turning left goes anticlockwise`() {
        assertEquals(
            listOf(Direction.W, Direction.N, Direction.E, Direction.S),
            listOf(Direction.N, Direction.E, Direction.S, Direction.W).map(Direction::turnLeft),
        )
    }

    @Test
    fun `turning right goes clockwise`() {
        assertEquals(
            listOf(Direction.E, Direction.S, Direction.W, Direction.N),
            listOf(Direction.N, Direction.E, Direction.S, Direction.W).map(Direction::turnRight),
        )
    }

    @Test
    fun `north increases y as defined in the brief`() {
        assertEquals(Coordinate(2, 3), Coordinate(2, 2).step(Direction.N))
    }
}
