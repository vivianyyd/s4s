/*import bottomup.BottomUp
import sketchral.InputFactory
import sketchral.OutputParser
import sketchral.Result
import sketchral.withNegEx
import util.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit 



val addEx by lazy{
    mutableListOf<Example>(
        Example(listOf(mutableListOf(1, 2), 3), listOf(1, 2, 3)), 
        Example(listOf(mutableListOf<Int>(), 3), listOf(3))
    )
} 
val sumFunc = Func(null,Type(listOf(MutableList::class, Int::class), List::class), sumEx, mutableListOf<Example>())
val diffFunc = Func(null,Type(listOf(MutableList::class, Int::class), List::class), diffEx, mutableListOf<Example>())
val mulFunc =Func(null,Type(listOf(MutableList::class, Int::class), List::class), mulEx, mutableListOf<Example>())
val maxFunc = Func(null,Type(listOf(MutableList::class, Int::class), List::class), maxEx, mutableListOf<Example>())
object ArithImpl : UPrimImpl {
    override fun len(x: Any): Int =
        when (x) {
            is Int -> x
            else -> throw UnsupportedOperationException("Length is not implemented for $x")
        }
}
private val funlist = listOf(addFunc, addAllFunc, dupFunc,delAllFunc,delFirstFunc,dropFunc,replicateFunc, reverseFunc,snocFunc)
private val namelist = listOf("add", "addAll", "dup", "delAll", "delFirst", "drop", "replicate", "reverse","snoc")
val arithmeticquery by lazy {
    Query(funlist, ArithImpl)
}
val arithmeticTest by lazy{
    TestQuery(funlist,namelist,arithmeticquery, "./test_outputs/arithmetic/")
}
*/