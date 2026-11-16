package martianrobots

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.PrintStream
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(Cli(System.`in`, System.out, System.err).run(args))
}

/**
 * The command-line interface: reads the mission from the file given as the only argument,
 * or from [stdin] when there is none, and prints one line per robot to [stdout].
 *
 * Kept separate from [main] so it can be tested with in-memory streams; it returns the exit
 * status instead of exiting.
 */
class Cli(
    private val stdin: InputStream,
    private val stdout: PrintStream,
    private val stderr: PrintStream,
) {
    fun run(args: Array<String>): Int {
        val input = try {
            when (args.size) {
                0 -> stdin.bufferedReader().readText()
                1 -> File(args[0]).readText()
                else -> return fail("Usage: martian-robots [input-file]")
            }
        } catch (e: IOException) {
            return fail("Could not read input: ${e.message}")
        }

        val mission = try {
            InputParser().parse(input)
        } catch (e: InputException) {
            return fail("Invalid input. ${e.message}")
        }

        Simulation.run(mission).forEach { stdout.println(it.report()) }
        return SUCCESS
    }

    private fun fail(message: String): Int {
        stderr.println(message)
        return FAILURE
    }

    companion object {
        const val SUCCESS = 0
        const val FAILURE = 1
    }
}
