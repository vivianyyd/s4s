package util

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class Example(
    val inputs: List<Any>,
    val output: Any
)

data class Type(
    val inputs: List<KClass<*>>,
    val output: KClass<*>
)

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

fun checkEx(example: Example, type: Type): Boolean {
    val correctArity = example.inputs.size == type.inputs.size
    val correctTypes =
        (example.inputs + listOf(example.output)).zip(type.inputs + listOf(type.output))
            .all { (ex, ty) ->
                ty.isInstance(ex)
            }
    return correctArity && correctTypes
}

fun checkFn(fn: KFunction<Any>, type: Type): Boolean {
    val correctReturnType = type.output == fn.returnType.classifier
    val correctArgTypes =
        type.inputs.zip(fn.parameters.map { p -> p.type.classifier }).all { (expected, actual) ->
            expected == actual
        }
    return correctReturnType && correctArgTypes
}
