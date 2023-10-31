package bottomup

import util.*
import kotlin.reflect.KClass

interface Value

data class IntValue(val value: Int) : Value

data class BooleanValue(val value: Boolean) : Value

// TODO improve types for EvaluationResult/Value/etc

typealias TypeSize = Pair<KClass<*>, Int>

/** A mapping representing the result of evaluating some predicate for each example. */
typealias EvaluationResult = Map<Example, Value>

class BottomUp(private val query: Query) {
    private val typeSizeToExpr: MutableMap<TypeSize, MutableList<Pair<U, EvaluationResult>>> = mutableMapOf()
    private val valuesToExpr: MutableMap<EvaluationResult, U> = mutableMapOf()


    fun enumerate(bound: Int) {
        enumerateWithValues(bound).forEach { (node, _) ->
            TODO("If the node is a boolean, evaluates to true on all positive examples and false on all negative examples, return it")
        }
    }

    /** Just like [enumerate], but also yields the result of evaluating the yielded node in all environments as
    specified by the examples in [query]. */
    private fun enumerateWithValues(bound: Int) = iterator {
        // Some literals
        (0..4).forEach { literal ->
            val nodeAndEvaluated = Pair(ULiteral(literal), query.examples.associateWith { IntValue(literal) })
            typeSizeToExpr.addMulti(
                Pair(Int::class, 1),
                nodeAndEvaluated
            )
            yield(nodeAndEvaluated)
        }
        // The lengths of all parameters
        val lenTerminals = (0..query.type.inputs.size).map { ULen(it) }
        for (lenNode in lenTerminals) {
            val evaluated = lenNode.evaluate(query)
            if (evaluated in valuesToExpr) continue
            valuesToExpr[evaluated] = lenNode
            typeSizeToExpr.addMulti(
                Pair(Int::class, 1),
                Pair(lenNode, evaluated)
            )
            yield(Pair(lenNode, evaluated))
        }

        for (possSize in 1..bound) {
            IntOp.values().forEach { op ->
                generate(
                    2,
                    makeNode = { args -> UOp(op, args.first().first as UInt, args.last().first as UInt) },
                    Int::class,
                    possSize
                ).forEach { yield(it) }
            }
            Cmp.values().forEach { cmp ->
                generate(
                    2,
                    makeNode = { args -> UCmp(cmp, args.first().first as UInt, args.last().first as UInt) },
                    Int::class,
                    possSize
                ).forEach { yield(it) }
            }
            BoolOp.values().forEach { op ->
                when (op) {
                    BoolOp.AND, BoolOp.OR -> generate(
                        2,
                        makeNode = { args -> UBop(op, args.first().first as UBoolean, args.last().first as UBoolean) },
                        Boolean::class,
                        possSize
                    ).forEach { yield(it) }
                    BoolOp.NOT -> generate(
                        1,
                        makeNode = { args -> UBop(op, args.first().first as UBoolean) },
                        Boolean::class,
                        possSize
                    ).forEach { yield(it) }
                }
            }
        }
    }

    /** Just here to reduce some code duplication. */
    private fun generate(
        numChildren: Int,
        makeNode: (List<Pair<U, EvaluationResult>>) -> U,
        childType: KClass<*>,
        size: Int
    ) = iterator {
        for (childPartitions in intPartitions(size - 1 - numChildren, numChildren)) {
            val candidates = mutableListOf<List<Pair<U, EvaluationResult>>>()
            childPartitions.forEach { childMinusOne ->
                typeSizeToExpr[Pair(childType, childMinusOne + 1)]?.let { candidates.add(it) }
            }
            if (candidates.size != numChildren) continue  // we failed to find candidates for all children, so this partition won't work
            for (candidateArgs in product(candidates.first(), candidates.last())) {
                val node = makeNode(candidateArgs)
                val evaluated = node.evaluateFromCachedChildren(query, candidateArgs.map { it.second })
                if (evaluated in valuesToExpr) continue
                valuesToExpr[evaluated] = node
                typeSizeToExpr.addMulti(Pair(Int::class, size), Pair(node, evaluated))
                yield(Pair(node, evaluated))
            }
        }
    }

    private fun <K, V> MutableMap<K, MutableList<V>>.addMulti(key: K, value: V) {
        if (key in this) {
            this[key]!!.add(value)
        } else {
            this[key] = mutableListOf(value)
        }
    }

    private fun intPartitions(target: Int, numParts: Int): List<List<Int>> {
        if (target < 0) return listOf()
        if (numParts == 1) return listOf(listOf(target))
        val parts = mutableListOf<List<Int>>()
        for (x1 in 0..target) {
            for (x2s in intPartitions(target - x1, numParts - 1)) {
                parts.add(listOf(x1) + x2s)
            }
        }
        return parts
    }

    private fun <T> product(a: List<T>, b: List<T>): List<List<T>> {
        val result = mutableListOf<List<T>>()
        a.forEach { ai ->
            b.forEach { bi ->
                result.add(listOf(ai, bi))
            }
        }
        return result
    }
}
