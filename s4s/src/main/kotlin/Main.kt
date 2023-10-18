import util.Example
import util.Type
import util.checkEx
import util.checkFn
import java.io.FileOutputStream
import java.io.PrintWriter

// Note: reflection throws an exception if this is declared inside the function where it's used
fun f(x: Int, y: Int): Int = x + y

fun s(x: List<*>): Int = x.size

fun main(args: Array<String>) {
    val e = Example(listOf(2,3), 4)
    val t = Type(listOf(Boolean::class, Int::class), Int::class)
    println(checkEx(e, t))
    println(checkFn(::f, t))

    val e2 = Example(listOf(listOf(1, 2, 3)), 3)
    val t2 = Type(listOf(List::class), Int::class)
    println(checkEx(e2, t2))
    println(checkFn(::s, t2))


//    val input = generateSequence(::readLine).joinToString("\n")
//    val jsonElement = Json.parseToJsonElement(input)
//    val rawProgram = Json.decodeFromJsonElement<RawProgram>(jsonElement)
//    val prettyJsonPrinter = Json { prettyPrint = true }
//    println(prettyJsonPrinter.encodeToString(optimizedProgram))
}
