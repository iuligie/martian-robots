package martianrobots

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Runs the whole program end to end through its command-line interface, with in-memory streams. */
class CliTest {

    private val sampleInput = """
        5 3
        1 1 E
        RFRFRFRF
        3 2 N
        FRRFLLFFRRFLL
        0 3 W
        LLFFFLFLFL
    """.trimIndent() + "\n"

    private val sampleOutput = "1 1 E\n3 3 N LOST\n2 3 S\n"

    private data class Result(val exitCode: Int, val stdout: String, val stderr: String)

    private fun runCli(vararg args: String, stdin: String = ""): Result {
        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val exitCode = Cli(
            ByteArrayInputStream(stdin.toByteArray()),
            PrintStream(out, true),
            PrintStream(err, true),
        ).run(arrayOf(*args))
        return Result(exitCode, out.toString().normaliseLineEndings(), err.toString().normaliseLineEndings())
    }

    private fun String.normaliseLineEndings() = replace(System.lineSeparator(), "\n")

    @Test
    fun `reads the mission from stdin and prints one line per robot`() {
        assertEquals(Result(0, sampleOutput, ""), runCli(stdin = sampleInput))
    }

    @Test
    fun `reads the mission from a file given as the only argument`() {
        val file = createTempFile("mission", ".txt").toFile().apply {
            writeText(sampleInput)
            deleteOnExit()
        }

        assertEquals(Result(0, sampleOutput, ""), runCli(file.path))
    }

    @Test
    fun `accepts Windows line endings`() {
        assertEquals(Result(0, sampleOutput, ""), runCli(stdin = sampleInput.replace("\n", "\r\n")))
    }

    @Test
    fun `invalid input prints the error to stderr, nothing to stdout, and exits with 1`() {
        val result = runCli(stdin = "5 3\n1 1 E\nFXF\n")

        assertEquals(
            Result(1, "", "Invalid input. Line 3: unknown instruction 'X', expected one of L, R, F\n"),
            result,
        )
    }

    @Test
    fun `a missing input file is reported and exits with 1`() {
        val missing = File("does-not-exist-${System.nanoTime()}.txt").path
        val result = runCli(missing)

        assertEquals(1, result.exitCode)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.startsWith("Could not read input:"), result.stderr)
    }

    @Test
    fun `more than one argument prints usage and exits with 1`() {
        assertEquals(Result(1, "", "Usage: martian-robots [input-file]\n"), runCli("a.txt", "b.txt"))
    }

    @Test
    fun `empty input is reported and exits with 1`() {
        assertEquals(
            Result(1, "", "Invalid input. Input is empty: expected grid size on the first line\n"),
            runCli(stdin = ""),
        )
    }
}
