package step2

import bottomup.BottomUp
import util.*
import kotlin.reflect.KFunction
import kotlin.reflect.KClass

data class Function(val f: KFunction<Any>, val type: Type)

interface Node {
    fun hasHoles(): Boolean
}

data class Application(val f: Func, val args: List<Node>) : Node {
    override fun hasHoles() = args.any { it.hasHoles() }
}

data class Input(val index: Int) : Node {
    override fun hasHoles() = false
}

data class Hole(val targetType: KClass<*>, val constraint: U? = null) : Node {
    override fun hasHoles() = true
}

/** A projection of a concrete value onto U, that is, all the info associated with a value that can be expressed in U. */
data class Projection(val len: Int)

data class ProjectedExample(
    val inputs: List<Projection>,
    val output: Projection
) {
    constructor(ex: Example, impl: UPrimImpl) : this(
        ex.inputs.map { Projection(impl.len(it)) },
        Projection(impl.len(ex.output))
    )
}

typealias World = List<ProjectedExample>  // We may make different choices of for example partitioning a length into two.
// I'm realizing this is gonna be hard

/**
 * Now that we're done with step 1, it's time for a step 2!
 *
 * Kinda sad this can't be called bottoms up since it's a top-down synthesizer
 */
class TopDown(private val query: Query, private val targetExamples: List<Example>, private val targetType: Type) {
    private val properties = query.functions.associateWith { BottomUp(query).property(it) }.filterNotNull()
    val targetExProjected = targetExamples.map { ProjectedExample(it, query.uImpl) }

    fun enumerate(): U {
        val root = nodes(targetType.output)


        // TODO let's try ends-in enumeration
    }

    private fun Func.examplesFitProperty(examples: List<ProjectedExample>): Boolean {
        return examples.all { ex ->
            properties[this]?.evaluate(ex) ?: false
        }
    }

    private fun Func.hasCorrectType(outType: KClass<*>): Boolean = type.output == outType

    private val nodesForType = mutableMapOf<KClass<*>, List<Node>>()

    private fun nodes(outType: KClass<*>): List<Node> = nodesForType.getOrPut(outType) {
        targetType.inputs.mapIndexedNotNull { i, ty -> if (ty == outType) Input(i) else null } +
                query.functions.filter { it.hasCorrectType(outType) }.map { Application(it, holes(it)) }
    }
}

fun <K, V> Map<K, V?>.filterNotNull(): Map<K, V> =
    this.mapNotNull { it.value?.let { value -> it.key to value } }.toMap()

fun holes(function: Func) = function.type.inputs.map { Hole(it) }
