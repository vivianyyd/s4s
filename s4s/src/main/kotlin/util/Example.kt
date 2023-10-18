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
