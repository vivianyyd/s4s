import bottomup.BottomUp
import sketchral.InputFactory
import sketchral.OutputParser
import sketchral.Result
import sketchral.withNegEx
import util.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit 

/*val ListQuery:TestQuery(query:Query){
    init{

    }
}*/
val listquery by lazy {
    Query(listOf(addFunc, addAllFunc, dupFunc,delAllFunc,delFirstFunc,dropFunc), ListImpl)
}
val addEx by lazy{
    mutableListOf<Example>(
        Example(listOf(mutableListOf(1, 2), 3), listOf(1, 2, 3)), 
        Example(listOf(mutableListOf<Int>(), 3), listOf(3))
    )
}
val addAllEx by lazy{
    mutableListOf<Example>(
        Example(listOf(mutableListOf(1, 2, 3), listOf(5)), listOf(1, 2, 3, 5)),
        Example(listOf(mutableListOf(1, 2), listOf()), listOf(1, 2)),
        Example(listOf(mutableListOf(), listOf(3)), listOf(3)),
        Example(listOf(mutableListOf(1, 2, 3), listOf(5, 6, 7, 8)), listOf(1, 2, 3, 5, 6, 7, 8))
    )
} 
  
val dupEx by lazy{
    mutableListOf<Example>(
        Example(listOf(listOf(1, 2)), listOf(1, 1, 2, 2)),
        Example(listOf(listOf<Int>()), listOf<Int>()),Example(listOf(listOf(1)), listOf(1, 1)),
        Example(listOf(listOf(1, 2, 3)), listOf(1, 1, 2, 2, 3, 3))
    )
}
val delAllEx by lazy{
    mutableListOf<Example>( 
        Example(listOf(mutableListOf(1, 2)), listOf<Int>()),
        Example(listOf(mutableListOf<Int>()), listOf<Int>()),
        Example(listOf(mutableListOf<Int>(0)), listOf<Int>())
    ) 
} 
val delFirstEx by lazy{
    mutableListOf<Example>( 
        Example(listOf(mutableListOf(1, 2)), listOf(2)),
        Example(listOf(mutableListOf(1, 2, 3)), listOf(2,3))
    )
} 

val dropEx by lazy{
    mutableListOf<Example>( 
        Example(listOf(mutableListOf(1, 2), 1), listOf(1)),
        Example(listOf(mutableListOf<Int>(), 0), listOf<Int>()),
        Example(listOf(mutableListOf(1, 2, 3), 2), listOf(1,2))
    )
}

val addFunc = Func(null,Type(listOf(MutableList::class, Int::class), List::class), addEx, mutableListOf<Example>())
val addAllFunc = Func(null, Type(listOf(MutableList::class, List::class), List::class), addAllEx, mutableListOf<Example>())
val dupFunc =  Func(null, Type(listOf(List::class), List::class), dupEx, mutableListOf<Example>())
val delAllFunc = Func(null, Type(listOf(MutableList::class), List::class), delAllEx, mutableListOf<Example>())
val delFirstFunc = Func(null, Type(listOf(MutableList::class), List::class), delFirstEx, mutableListOf<Example>())
val dropFunc = Func(null, Type(listOf(MutableList::class, Int::class), List::class), dropEx,  mutableListOf<Example>())

object ListImpl : UPrimImpl {
    override fun len(x: Any): Int =
        when (x) {
            is List<*> -> x.size
            else -> throw UnsupportedOperationException("Length is not implemented for $x")
        }
}
/*spyro list functions: 
we seem to already have append and stutter
append, deleteAll, deleteFirst, drop, elem, elemindex, ith, min, replicate
reverse, reverse2, snoc, stutter, take

*/
