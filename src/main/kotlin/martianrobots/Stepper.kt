package martianrobots

/**
 * Applies the rules of the world to a command's proposal. This is the only code that decides
 * whether a robot is lost and the only code that reads or leaves scents.
 */
class Stepper(private val mars: Mars) {

    fun step(robot: Robot, command: Command): Robot {
        val proposed = command.propose(robot)
        return when {
            proposed.position in mars -> proposed
            // A previous robot was lost from here: ignore the instruction instead of falling off.
            mars.hasScent(robot.position) -> robot
            else -> {
                // Scent goes on the last point the robot occupied, and it is reported there.
                mars.leaveScent(robot.position)
                robot.copy(lost = true)
            }
        }
    }
}
