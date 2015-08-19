import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern

val command = "cmd.exe /C ant dist"
val targetPattern = Pattern.compile("^[a-zA-Z-]*:$")
val infoPerfAnalyze = "info: PERF: ANALYZE:"
val numberOfTests = 5

fun main(args: Array<String>) {
    val timestampsForTargets = runTests(numberOfTests)
    println("=> Tests completed")
    println("=> Tests number: $numberOfTests")
    println("=> Analysis duration per target (all in ms):")
    timestampsForTargets.forEach {
        println("> ${it.first}")
        println("  all values: ${it.second}")
        val mean = it.second.sum() / numberOfTests
        println("  mean: $mean")
        val variance = it.second.map { Math.pow(it - mean, 2.0) }.sum() / numberOfTests
        println("  variance: $variance")
    }
}

fun cons<T>(x: T, xs: LinkedList<T>): LinkedList<T> { xs.addFirst(x); return xs }

fun runTests(numberOfTests: Int): List<Pair<String, List<Double>>> {
    fun runAccumulatingResults(
            numberOfTests: Int,
            acc: List<Pair<String, LinkedList<Double>>>
    ): List<Pair<String, List<Double>>> =
        if (numberOfTests > 0) {
            println("==================")
            val result = runTest()
            println("==================")
            val newAcc =
                    if (acc.isEmpty())
                        result.map { it.first to linkedListOf(it.second) }
                    else {
                        assert(acc.size() == result.size(), "Test output has different size")
                        acc.zip(result).map {
                            val target = it.first.first
                            val accValues = it.first.second
                            val resultValue = it.second.second
                            target to cons(resultValue, accValues)
                        }
                    }
            runAccumulatingResults(numberOfTests - 1, newAcc)
        }
        else acc

    return runAccumulatingResults(numberOfTests, listOf())
}

private fun runTest(): List<Pair<String, Double>> {
    val runtime = Runtime.getRuntime()
    val proc = runtime.exec(command)
    println("=> Process started")

    var isr: InputStreamReader? = null
    var bufferedReader: BufferedReader? = null
    val timestamps = linkedListOf<Pair<String, Double?>>()
    try {
        isr = InputStreamReader(proc.inputStream)
        bufferedReader = BufferedReader(isr)
        println("=> Processing output...")
        var line: String? = bufferedReader.readLine()
        while (line != null) {
            processLine(line, timestamps)
            line = bufferedReader.readLine()
        }

        println("=> Output processed")
        val exitValue = proc.waitFor()
        println("=> Process' exit value: $exitValue")
    } catch (ioe: IOException) {
        ioe.printStackTrace();
    } finally {
        isr?.close()
        bufferedReader?.close()
    }
    val result = timestamps.filter { it.second != null }.map { it.first to it.second as Double }
    println("=> Timestamps:")
    result.forEach { println(it) }
    return result
}

private fun processLine(line: String, timestamps: LinkedList<Pair<String, Double?>>) {
    if (targetPattern.matcher(line).matches()) {
        timestamps.addFirst(line to null)
        println("> Target found: $line")
    }
    if (line.contains(infoPerfAnalyze)) {
        val timestamp = extractTimestamp(line)
        assert(timestamps.isNotEmpty(), "Empty timestamps list")
        val newTop = timestamps.removeFirst().first to timestamp
        timestamps.addFirst(newTop)
        println("> Timestamp found: $timestamp")
    }
}

fun extractTimestamp(line: String): Double {
    val parts = line.split(Pattern.compile("\\s"))
    // the format is "`word` `word` `word` timestamp ms"
    assert(parts.size() > 1, "Ant dist output format assumption is failed")
    val timestamp = parts[parts.size() - 2]
    return timestamp.toDouble()
}