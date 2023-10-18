package util

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class Query(val f: KFunction<Any>, val type: Type, val examples: List<Example>, val uImpl: UPrimImpl) {
    val lens: Map<Any, Int>

    init {
        assert(checkFn(f, type))
        assert(examples.all { checkEx(it, type) })

        // Construct map of lengths and confirm that they were successfully computed
        lens = mutableMapOf()
        assert(null !in examples.flatMap { ex ->
            (ex.inputs + listOf(ex.output)).map {
                try {
                    lens[it] = uImpl.len(it)
                    lens[it]
                } catch (e: Exception) {
                    null
                }
            }
        })
    }
}
