package martianrobots

/** One robot's starting state and the instructions it will carry out. */
data class RobotPlan(val start: Robot, val instructions: List<Command>)

data class Mission(val upperRight: Coordinate, val robots: List<RobotPlan>)
