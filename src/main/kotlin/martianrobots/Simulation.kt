package martianrobots

/** One robot's starting state and the instructions it will carry out. */
data class RobotPlan(val start: Robot, val instructions: List<Command>)

data class Mission(val upperRight: Coordinate, val robots: List<RobotPlan>)

/** Runs robots one after another on a single [Mars], so scents carry over between robots. */
class Simulation(mars: Mars) {

    private val stepper = Stepper(mars)

    fun run(plan: RobotPlan): Robot =
        plan.instructions.fold(plan.start) { robot, command ->
            if (robot.lost) robot else stepper.step(robot, command)
        }

    companion object {
        fun run(mission: Mission): List<Robot> {
            val simulation = Simulation(Mars(mission.upperRight))
            return mission.robots.map(simulation::run)
        }
    }
}
