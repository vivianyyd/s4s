import bottomup.BottomUp
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
val replicateEx by lazy{
    mutableListOf<Example>( 
        Example(listOf(listOf(1, 2)), listOf(1,2)),
        Example(listOf(listOf<Int>()), listOf<Int>())
    )
}
val reverseEx by lazy{
    mutableListOf<Example>( 
        Example(listOf(listOf(1, 2)), listOf(2,1)),
        Example(listOf(listOf<Int>()), listOf<Int>())
    )
}
private val snocEx by lazy{
    mutableListOf<Example>( 
        Example(listOf(mutableListOf(1, 2), 1), listOf(1,1,2)),
        Example(listOf(mutableListOf<Int>(), 0), listOf(0))
    )
}
private val maxEx by lazy{
    mutableListOf<Example>( 
        Example(listOf(listOf(1, 2), listOf(1)), listOf(1,2)),
        Example(listOf(listOf<Int>(), listOf(0)), listOf(0)),
        Example(listOf(listOf(1), listOf(1,2,4)), listOf(1,2,4)),
        Example(listOf(listOf(3,2), listOf(1,2,4)), listOf(1,2,4)),
        Example(listOf(listOf<Int>(), listOf<Int>()), listOf<Int>())
    )
}
private val max3Ex by lazy{
    mutableListOf<Example>( 
        Example(listOf(listOf(1, 2), listOf(1),listOf(1)), listOf(1,2)),
        Example(listOf(listOf<Int>(), listOf(0),listOf(1,1)), listOf(1,1)),
        Example(listOf(listOf(1), listOf(1,2,4), listOf(3,5)), listOf(1,2,4))
    )
}
private val addFunc = Func(null,Type(listOf(MutableList::class, Int::class), List::class), addEx, mutableListOf<Example>())
private val addAllFunc = Func(null, Type(listOf(MutableList::class, List::class), List::class), addAllEx, mutableListOf<Example>())
private val dupFunc =  Func(null, Type(listOf(List::class), List::class), dupEx, mutableListOf<Example>())
private val delAllFunc = Func(null, Type(listOf(MutableList::class), List::class), delAllEx, mutableListOf<Example>())
private val delFirstFunc = Func(null, Type(listOf(MutableList::class), List::class), delFirstEx, mutableListOf<Example>())
private val dropFunc = Func(null, Type(listOf(MutableList::class, Int::class), List::class), dropEx,  mutableListOf<Example>())
private val replicateFunc = Func(null, Type(listOf(List::class), List::class), replicateEx,  mutableListOf<Example>())
private val reverseFunc = Func(null, Type(listOf(List::class), List::class), reverseEx,  mutableListOf<Example>())
private val snocFunc = Func(null, Type(listOf(MutableList::class, Int::class), List::class), snocEx,  mutableListOf<Example>())
private val maxFunc = Func(null, Type(listOf(List::class, List::class), List::class), maxEx,  mutableListOf<Example>())
private val max3Func = Func(null, Type(listOf(List::class, List::class,List::class), List::class), max3Ex,  mutableListOf<Example>())

object ListImpl : UPrimImpl {
    override fun len(x: Any): Int =
        when (x) {
            is List<*> -> x.size
            else -> throw UnsupportedOperationException("Length is not implemented for $x")
        }
}
private val funlist = listOf(addFunc, addAllFunc, dupFunc,delAllFunc,delFirstFunc,dropFunc,maxFunc, max3Func,replicateFunc, reverseFunc,snocFunc)
private val namelist = listOf("add", "addAll", "dup", "delAll", "delFirst", "drop", "max","max3","replicate", "reverse","snoc")
val listquery by lazy {
    Query(funlist, ListImpl)
}
val listTest by lazy{
    TestQuery(funlist,namelist,listquery, "./test_outputs/list/")
}
