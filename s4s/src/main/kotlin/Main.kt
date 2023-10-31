import bottomup.BottomUp
import util.*

// Note: reflection throws an exception if this is declared inside the function where it's used
fun add(x: MutableList<Int>, y: Int): List<Int> {
    x.add(y)
    return x
}

fun addAll(x: MutableList<Int>, y: List<Int>): List<Int> {
    x.addAll(y)
    return x
}

object ListImpl : UPrimImpl {
    override fun len(x: Any): Int =
        when (x) {
            is List<*> -> x.size
            else -> throw UnsupportedOperationException("Length is not implemented for $x")
        }
}

fun main(args: Array<String>) {
    testAdd()
    testAddAll()
//    testDouble()

//    val input = generateSequence(::readLine).joinToString("\n")
//    val jsonElement = Json.parseToJsonElement(input)
//    val rawProgram = Json.decodeFromJsonElement<RawProgram>(jsonElement)
//    val prettyJsonPrinter = Json { prettyPrint = true }
//    println(prettyJsonPrinter.encodeToString(optimizedProgram))
}

fun testAdd() {
    println("Running test for MutableList.add")
    val t = Type(listOf(MutableList::class, Int::class), List::class)

    val posExamplesAdd = mutableListOf<Example>()
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2, 3), 5), listOf(1, 2, 3, 5)))
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 3), listOf(1, 2, 3)))
    posExamplesAdd.add(Example(listOf(mutableListOf<Int>(), 3), listOf(3)))

    val negExamplesAdd = mutableListOf<Example>()
    negExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 3), listOf(1, 2, 3, 4)))

    val queryAdd = Query(::add, t, posExamplesAdd, negExamplesAdd, ListImpl)

    BottomUp(queryAdd).enumerate(6)
}

fun testAddAll() {
    println("Running test for MutableList.addAll")
    val t = Type(listOf(MutableList::class, List::class), List::class)

    val posExamplesAddAll = mutableListOf<Example>()
    posExamplesAddAll.add(Example(listOf(mutableListOf(1, 2, 3), listOf(5)), listOf(1, 2, 3, 5)))
    posExamplesAddAll.add(Example(listOf(mutableListOf(1, 2), listOf()), listOf(1, 2)))
    posExamplesAddAll.add(Example(listOf(mutableListOf(), listOf(3)), listOf(3)))
    posExamplesAddAll.add(Example(listOf(mutableListOf(1, 2, 3), listOf(5, 7, 9)), listOf(1, 2, 3, 5, 7, 9)))

    val negExamplesAddAll = mutableListOf<Example>()
    negExamplesAddAll.add(Example(listOf(mutableListOf(1, 2), listOf(3)), listOf(1, 2, 3, 4)))
    negExamplesAddAll.add(Example(listOf(mutableListOf(1, 2), listOf(3)), listOf(1, 2)))
    negExamplesAddAll.add(Example(listOf(mutableListOf(1, 2), listOf()), listOf(1, 2, 3)))

    val queryAddAll = Query(::addAll, t, posExamplesAddAll, negExamplesAddAll, ListImpl)

    BottomUp(queryAddAll).enumerate(6)
}