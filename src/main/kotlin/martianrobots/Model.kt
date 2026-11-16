package martianrobots

/** Compass orientation. Declared clockwise so turning is a step through [entries]. */
enum class Direction(val dx: Int, val dy: Int) {
    N(0, 1),
    E(1, 0),
    S(0, -1),
    W(-1, 0);

    fun turnLeft(): Direction = entries[(ordinal + entries.size - 1) % entries.size]

    fun turnRight(): Direction = entries[(ordinal + 1) % entries.size]
}

data class Coordinate(val x: Int, val y: Int) {
    fun step(direction: Direction): Coordinate = Coordinate(x + direction.dx, y + direction.dy)
}

data class Robot(
    val position: Coordinate,
    val direction: Direction,
    val lost: Boolean = false,
) {
    /** Output format required by the brief, e.g. `3 3 N LOST`. */
    fun report(): String = buildString {
        append("${position.x} ${position.y} $direction")
        if (lost) append(" LOST")
    }
}

/**
 * The bounded world. Lower-left is always (0, 0).
 *
 * Holds the scents left by lost robots; this is the only state shared between robots,
 * which is why robots must be run sequentially.
 */
class Mars(val upperRight: Coordinate) {
    private val scents = mutableSetOf<Coordinate>()

    init {
        require(upperRight.x >= 0 && upperRight.y >= 0) { "Grid upper-right must not be negative: $upperRight" }
    }

    operator fun contains(coordinate: Coordinate): Boolean =
        coordinate.x in 0..upperRight.x && coordinate.y in 0..upperRight.y

    fun hasScent(coordinate: Coordinate): Boolean = coordinate in scents

    fun leaveScent(coordinate: Coordinate) {
        scents += coordinate
    }
}
