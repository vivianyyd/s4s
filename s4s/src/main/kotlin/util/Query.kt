package util

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class Query(
    val f: KFunction<Any>,
    val type: Type,
    val posExamples: List<Example>,
    val negExamples: List<Example>,  // must still be valid types TODO testing: check if we can get expressive examples just with the same inputs but diff outputs. or do the inputs need to be diff
    val uImpl: UPrimImpl
) {
    val lens: Map<Any, Int>
    val examples = posExamples + negExamples
    val argsWithUndefinedLength: Set<Int>

    init {
        assert(checkFn(f, type))
        assert(examples.all { checkEx(it, type) })

        // Construct map of lengths and confirm that they were successfully computed
        lens = mutableMapOf()
        argsWithUndefinedLength = mutableSetOf()
        examples.forEach { ex ->
            (ex.inputs).mapIndexed { i, it ->
                try {
                    lens[it] = uImpl.len(it)
                } catch (_: Exception) {
                    argsWithUndefinedLength.add(i)
                }
            }
            try {
                lens[ex.output] = uImpl.len(ex.output)
            } catch (_: Exception) {
                argsWithUndefinedLength.add(-1)
            }
        }
    }
}
