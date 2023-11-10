import bottomup.BottomUp
import sketchral.InputFactory
import sketchral.OutputParser
import util.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

fun bottomUpTests() {
    val bu = BottomUp(query)
    println("Running bottom-up for MutableList.add")
    println("Found ${bu.enumerate(addFunc, 6)}")
    println("Running bottom-up for MutableList.addAll")
    println("Found ${bu.enumerate(addAllFunc, 6)}")
    println("Running bottom-up for duplicate")
    println("Found ${bu.enumerate(dupFunc, 6)}")
}

fun writeToTmp(content: String) = File("tmp.sk").printWriter().use { out -> out.println(content) }

fun callSketch(input: String): String {
    writeToTmp(input)
    return ("sketch tmp.sk -V 5 --slv-nativeints --bnd-inline-amnt 5".runCommand())
        ?: throw Exception("I'm sad")
}

fun main(args: Array<String>) {
//    bottomUpTests()
    val func = dupFunc
    val ig = InputFactory(func, query)
    val synth = callSketch(ig.synthInput(listOf(), mapOf()))
    val phi = OutputParser(synth).parseProperty()
    println("Initial synthesized property: $phi")
    val precision = callSketch(ig.precisionInput(phi, listOf(), listOf(), mapOf()))
    val newPhi = OutputParser(precision).parseProperty()
    println("Property with increased precision: $newPhi")

//    val input = generateSequence(::readLine).joinToString("\n")
//    val jsonElement = Json.parseToJsonElement(input)
//    val rawProgram = Json.decodeFromJsonElement<RawProgram>(jsonElement)
//    val prettyJsonPrinter = Json { prettyPrint = true }
//    println(prettyJsonPrinter.encodeToString(optimizedProgram))
}

fun String.runCommand(
    workingDir: File = File(System.getProperty("user.dir"))
): String? {
    try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

val query by lazy {
    Query(listOf(addFunc, addAllFunc, dupFunc), ListImpl)
}

object ListImpl : UPrimImpl {
    override fun len(x: Any): Int =
        when (x) {
            is List<*> -> x.size
            else -> throw UnsupportedOperationException("Length is not implemented for $x")
        }
}

// Note: reflection throws an exception if these are declared inside the function where it's used
fun add(x: MutableList<Int>, y: Int): List<Int> {
    x.add(y)
    return x
}

fun addAll(x: MutableList<Int>, y: List<Int>): List<Int> {
    x.addAll(y)
    return x
}

val addFunc by lazy {
    val t = Type(listOf(MutableList::class, Int::class), List::class)

    val posExamplesAdd = mutableListOf<Example>()
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 3), listOf(1, 2, 3)))
    posExamplesAdd.add(Example(listOf(mutableListOf<Int>(), 3), listOf(3)))

    val negExamplesAdd = mutableListOf<Example>()
    negExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 3), listOf(1, 2, 3, 4)))

    Func(::add, t, posExamplesAdd, negExamplesAdd)
}

val addAllFunc by lazy {
    val t = Type(listOf(MutableList::class, List::class), List::class)

    val posExamplesAddAll = mutableListOf<Example>()
    posExamplesAddAll.add(Example(listOf(mutableListOf(1, 2, 3), listOf(5)), listOf(1, 2, 3, 5)))
    posExamplesAddAll.add(Example(listOf(mutableListOf(1, 2), listOf()), listOf(1, 2)))
    posExamplesAddAll.add(Example(listOf(mutableListOf(), listOf(3)), listOf(3)))
    posExamplesAddAll.add(Example(listOf(mutableListOf(1, 2, 3), listOf(5, 6, 7, 8)), listOf(1, 2, 3, 5, 6, 7, 8)))

    val negExamplesAddAll = mutableListOf<Example>()
    negExamplesAddAll.add(Example(listOf(mutableListOf(1, 2), listOf(3)), listOf(1, 2, 3, 4)))

    Func(::addAll, t, posExamplesAddAll, negExamplesAddAll)
}

fun dup(l: List<Int>): List<Int> = l.flatMap { listOf(it, it) }

val dupFunc by lazy {
    val t = Type(listOf(List::class), List::class)

    val posExamplesDup = mutableListOf<Example>()
    posExamplesDup.add(Example(listOf(listOf(1, 2)), listOf(1, 1, 2, 2)))
    posExamplesDup.add(Example(listOf(listOf<Int>()), listOf<Int>()))
    posExamplesDup.add(Example(listOf(listOf(1)), listOf(1, 1)))
    posExamplesDup.add(Example(listOf(listOf(1, 2, 3)), listOf(1, 1, 2, 2, 3, 3)))

    val negExamplesDup = mutableListOf<Example>()
    negExamplesDup.add(Example(listOf(listOf(1, 2)), listOf(1, 2)))
    Func(::dup, t, posExamplesDup, negExamplesDup)
}