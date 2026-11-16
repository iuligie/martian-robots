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
