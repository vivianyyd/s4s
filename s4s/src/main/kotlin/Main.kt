import bottomup.BottomUp
import util.*
import java.io.FileOutputStream
import java.io.PrintWriter

// Note: reflection throws an exception if this is declared inside the function where it's used
fun f(x: MutableList<Int>, y: Int): List<Int> {
    x.add(y)
    return x
}

object impl : UPrimImpl {
    override fun len(x: Any): Int =
        when (x) {
            is List<*> -> x.size
            else -> throw UnsupportedOperationException("Length is not implemented for $x")
        }
}

fun s(x: List<*>): Int = x.size

fun main(args: Array<String>) {
    val t = Type(listOf(MutableList::class, Int::class), List::class)
    val e0 = Example(listOf(mutableListOf(1, 2, 3), 5), listOf(1, 2, 3, 5))
    val e1 = Example(listOf(mutableListOf(1, 2), 3), listOf(1, 2, 3))
    val e2 = Example(listOf(mutableListOf<Int>(), 3), listOf(3))

    val n0 = Example(listOf(mutableListOf(1, 2), 3), listOf(1, 2, 3, 4))

    println(checkEx(e0, t))
    println(checkEx(e1, t))
    println(checkEx(e2, t))
    println(checkFn(::f, t))

    val query = Query(::f, t, listOf(e0, e1, e2), listOf(n0), impl)

    BottomUp(query).enumerate(6)
//    val e2 = Example(listOf(listOf(1, 2, 3)), 3)
//    val t2 = Type(listOf(List::class), Int::class)
//    println(checkEx(e2, t2))
//    println(checkFn(::s, t2))

//    val e = Example(listOf(2,3), 4)
//    val t = Type(listOf(Boolean::class, Int::class), Int::class)
//    println(checkEx(e, t))
//    println(checkFn(::f, t))
//
//    val e2 = Example(listOf(listOf(1, 2, 3)), 3)
//    val t2 = Type(listOf(List::class), Int::class)
//    println(checkEx(e2, t2))
//    println(checkFn(::s, t2))

//    val input = generateSequence(::readLine).joinToString("\n")
//    val jsonElement = Json.parseToJsonElement(input)
//    val rawProgram = Json.decodeFromJsonElement<RawProgram>(jsonElement)
//    val prettyJsonPrinter = Json { prettyPrint = true }
//    println(prettyJsonPrinter.encodeToString(optimizedProgram))
}
