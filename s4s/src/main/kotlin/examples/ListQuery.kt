import bottomup.BottomUp
import sketchral.InputFactory
import sketchral.OutputParser
import sketchral.Result
import sketchral.withNegEx
import util.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit 

val listquery by lazy {
    Query(listOf(addFunc, addAllFunc, dupFunc,delAllFunc,delFirstFunc,dropFunc,elemFunc, elemIndexFunc), ListImpl)
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

fun delAll(x: MutableList<Int>): List<Int> {
    return listOf<Int>()
}
fun del(x: MutableList<Int>): List<Int> {
    x.removeAt(0)
    return x
}
fun drop(x: MutableList<Int>, y: Int): List<Int> {
    return dummyInt(x,y)
}
fun elem(x: MutableList<Int>, y: Int): Boolean {
    return dummyBool(x,y)
}
fun elemindex(x: MutableList<Int>, y: Int): Int {
    return dummyElem(x,y)
}
fun dummyInt(x: MutableList<Int>, y: Int): List<Int> {
    return add(x,y)
}
fun dummyBool(x: MutableList<Int>, y: Int): Boolean {
    return true
}
fun dummyMutate(x: MutableList<Int>): List<Int> {
    return del(x)
}
fun dummyElem(x: MutableList<Int>, y: Int): Int {
    return 1
}
val addFunc by lazy {
    val t = Type(listOf(MutableList::class, Int::class), List::class)
    val posExamplesAdd = mutableListOf<Example>()
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 3), listOf(1, 2, 3)))
    //posExamplesAdd.add(Example(listOf(mutableListOf<Int>(), 3), listOf(3)))
    val negExamplesAdd = mutableListOf<Example>()
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
//    negExamplesAddAll.add(Example(listOf(mutableListOf(1, 2), listOf(3)), listOf(1, 2, 3, 4)))

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
//    negExamplesDup.add(Example(listOf(listOf(1, 2)), listOf(1, 2)))
    Func(::dup, t, posExamplesDup, negExamplesDup)
}
val delAllFunc by lazy {
    val t = Type(listOf(MutableList::class), List::class)

    val posExamplesAdd = mutableListOf<Example>()
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2)), listOf<Int>()))
    posExamplesAdd.add(Example(listOf(mutableListOf<Int>()), listOf<Int>()))
    posExamplesAdd.add(Example(listOf(mutableListOf<Int>(0)), listOf<Int>())) 

    val negExamplesAdd = mutableListOf<Example>()
    Func(::delAll, t, posExamplesAdd, negExamplesAdd)
}
val delFirstFunc by lazy {
    val t = Type(listOf(MutableList::class), List::class)
    val posExamplesAdd = mutableListOf<Example>()
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2)), listOf(2)))  
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2, 3)), listOf(2,3)))
    val negExamplesAdd = mutableListOf<Example>() 
    Func(::del, t, posExamplesAdd, negExamplesAdd)
}

val dropFunc by lazy{
    val t = Type(listOf(MutableList::class, Int::class), List::class)
    val posExamplesAdd = mutableListOf<Example>()
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 1), listOf(1)))
    posExamplesAdd.add(Example(listOf(mutableListOf<Int>(), 0), listOf<Int>()))
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2, 3), 2), listOf(1,2)))
    val negExamplesAdd = mutableListOf<Example>()
    Func(::drop, t, posExamplesAdd, negExamplesAdd)
}

val elemFunc by lazy{
    //note from alex: I do not believe that we are going to be able to 
    //get much in terms of semantics out of this one, but tally-ho!
    val t = Type(listOf(MutableList::class, Int::class), Boolean::class)
    val posExamplesAdd = mutableListOf<Example>()
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 1), true))
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 4), false))
    posExamplesAdd.add(Example(listOf(mutableListOf<Int>(), 0), false))
    posExamplesAdd.add(Example(listOf(mutableListOf(1), 1), true))
    posExamplesAdd.add(Example(listOf(mutableListOf(1), 2), false))
    val negExamplesAdd = mutableListOf<Example>()
    Func(::elem, t, posExamplesAdd, negExamplesAdd)
}
val elemIndexFunc by lazy{
    //note from alex: I do not believe that we are going to be able to 
    //get much in terms of semantics out of this one, but tally-ho!
    val t = Type(listOf(MutableList::class, Int::class), Int::class)
    val posExamplesAdd = mutableListOf<Example>()
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 1), 0)) 
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 1), 1)) 
    posExamplesAdd.add(Example(listOf(mutableListOf(1, 2), 1), 1)) 
    val negExamplesAdd = mutableListOf<Example>()
    Func(::elemindex, t, posExamplesAdd, negExamplesAdd)
}

/*spyro list functions: 
we seem to already have append and stutter
append, deleteAll, deleteFirst, drop, elem, elemindex, ith, min, replicate
reverse, reverse2, snoc, stutter, take

*/
