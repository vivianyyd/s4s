import bottomup.BottomUp
import sketchral.InputFactory
import util.*

fun bottomUpTests() {
    println("Running bottom-up for MutableList.add")
    BottomUp(addQuery).enumerate(6)
    println("Running bottom-up for MutableList.addAll")
    BottomUp(addAllQuery).enumerate(6)
    println("Running bottom-up for duplicate")
    BottomUp(dupQuery).enumerate(6)
    // TODO add a test for duplicating each element
}

fun main(args: Array<String>) {
//    bottomUpTests()
    val ig = InputFactory(addQuery)
    println(ig.synthInput(addQuery.posExamples, addQuery.negExamples, listOf(), mapOf()))

//    val input = generateSequence(::readLine).joinToString("\n")
//    val jsonElement = Json.parseToJsonElement(input)
//    val rawProgram = Json.decodeFromJsonElement<RawProgram>(jsonElement)
//    val prettyJsonPrinter = Json { prettyPrint = true }
//    println(prettyJsonPrinter.encodeToString(optimizedProgram))
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

val addQuery by lazy {
    val t = Type(listOf(MutableList::class, Int::class), List::class)

    val posExamplesAdd = mutableListOf<Example>()
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 3), listOf(1, 2, 3)))
    posExamplesAdd.add(Example(listOf(mutableListOf<Int>(), 3), listOf(3)))

    val negExamplesAdd = mutableListOf<Example>()
    negExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 3), listOf(1, 2, 3, 4)))

    Query(::add, t, posExamplesAdd, negExamplesAdd, ListImpl)
}

val addAllQuery by lazy {
    val t = Type(listOf(MutableList::class, List::class), List::class)

    val posExamplesAddAll = mutableListOf<Example>()
    posExamplesAddAll.add(Example(listOf(mutableListOf(1, 2, 3), listOf(5)), listOf(1, 2, 3, 5)))
    posExamplesAddAll.add(Example(listOf(mutableListOf(1, 2), listOf()), listOf(1, 2)))
    posExamplesAddAll.add(Example(listOf(mutableListOf(), listOf(3)), listOf(3)))

    val negExamplesAddAll = mutableListOf<Example>()
    negExamplesAddAll.add(Example(listOf(mutableListOf(1, 2), listOf(3)), listOf(1, 2, 3, 4)))

    Query(::addAll, t, posExamplesAddAll, negExamplesAddAll, ListImpl)
}

fun dup(l: List<Int>): List<Int> = l.flatMap { listOf(it, it) }

val dupQuery by lazy {
    val t = Type(listOf(List::class), List::class)

    val posExamplesAddAll = mutableListOf<Example>()
    posExamplesAddAll.add(Example(listOf(listOf(1, 2, 3)), listOf(1, 1, 2, 2, 3, 3)))
    posExamplesAddAll.add(Example(listOf(listOf<Int>()), listOf<Int>()))

    val negExamplesAddAll = mutableListOf<Example>()
    negExamplesAddAll.add(Example(listOf(listOf(1, 2)), listOf(1, 2)))

    Query(::addAll, t, posExamplesAddAll, negExamplesAddAll, ListImpl)
}