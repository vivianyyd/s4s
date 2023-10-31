package util

import bottomup.IntValue
import bottomup.BooleanValue
import bottomup.EvaluationResult

interface U {
    /** Maps each example to this node's value in that example, using cached values. */
    fun evaluateFromCachedChildren(query: Query, cachedChildren: List<EvaluationResult>): EvaluationResult
}

interface UBoolean : U

data class UCmp(val cmp: Cmp, val left: UInt, val right: UInt) : UBoolean {
    override fun evaluateFromCachedChildren(
        query: Query,
        cachedChildren: List<EvaluationResult>
    ): EvaluationResult {
        assert(cachedChildren.size == 2)
        return query.examples.associateWith { ex ->
            BooleanValue(
                cmp.evaluate(
                    (cachedChildren.first()[ex] as IntValue).value,
                    (cachedChildren.last()[ex] as IntValue).value
                )
            )
        }
    }
}

data class UBop(val op: BoolOp, val left: UBoolean, val right: UBoolean? = null) : UBoolean {
    override fun evaluateFromCachedChildren(
        query: Query,
        cachedChildren: List<EvaluationResult>
    ): EvaluationResult {
        when (op) {
            BoolOp.AND, BoolOp.OR -> {
                assert(cachedChildren.size == 2)
                return query.examples.associateWith { ex ->
                    BooleanValue(
                        op.evaluate(
                            (cachedChildren.first()[ex] as BooleanValue).value,
                            (cachedChildren.last()[ex] as BooleanValue).value
                        )
                    )
                }
            }
            BoolOp.NOT -> {
                assert(cachedChildren.size == 2)
                return query.examples.associateWith { ex ->
                    BooleanValue(
                        op.evaluate(
                            (cachedChildren.first()[ex] as BooleanValue).value, null
                        )
                    )
                }
            }
        }
    }
}

interface UInt : U

data class ULiteral(val value: Int) : UInt {
    override fun evaluateFromCachedChildren(
        query: Query,
        cachedChildren: List<EvaluationResult>
    ): EvaluationResult {
        assert(cachedChildren.isEmpty())
        return query.examples.associateWith { IntValue(value) }
    }
}

/**
 * @param parameter: The index of the parameter. For a function with n inputs, the first argument is parameter 0 and
 * the output is parameter n.
 */
data class ULen(val parameter: Int) : UInt {
    fun evaluate(query: Query): EvaluationResult {
        return query.examples.associateWith { ex ->
            val element = if (parameter == query.type.inputs.size) ex.output else ex.inputs[parameter]
            IntValue(query.lens[element]!!)
        }
    }

    override fun evaluateFromCachedChildren(
        query: Query,
        cachedChildren: List<EvaluationResult>
    ): EvaluationResult {
        assert(cachedChildren.isEmpty())
        return evaluate(query)
    }
}

data class UOp(val op: IntOp, val left: UInt, val right: UInt) : UInt {
    override fun evaluateFromCachedChildren(
        query: Query,
        cachedChildren: List<EvaluationResult>
    ): EvaluationResult {
        assert(cachedChildren.size == 2)
        return query.examples.associateWith { ex ->
            IntValue(
                op.evaluate(
                    (cachedChildren.first()[ex] as IntValue).value,
                    (cachedChildren.last()[ex] as IntValue).value
                )
            )
        }
    }
}

enum class Cmp(val commutative: Boolean = false, val evaluate: (Int, Int) -> Boolean) {
    EQ(true,
        { x, y -> x == y }),
    LT(evaluate = { x, y -> x < y }),
    GT(evaluate = { x, y -> x > y }),
    LE(evaluate = { x, y -> x <= y }),
    GE(evaluate = { x, y -> x >= y })
}

enum class BoolOp(val commutative: Boolean = false, val evaluate: (Boolean, Boolean?) -> Boolean) {
    AND(true, { x, y -> x && y!! }),
    OR(true, { x, y -> x || y!! }),
    NOT(evaluate = { x, _ -> !x })
}

enum class IntOp(val commutative: Boolean = false, val evaluate: (Int, Int) -> Int) {
    ADD(true, { x, y -> x + y }),
    MUL(true, { x, y -> x * y }),
    SUB(evaluate = { x, y -> x - y });
//    DIV

    override fun toString() = super.toString().lowercase()
}

interface UPrimImpl {
    /** Must be supported for all input and output types to the queried function. */
    fun len(x: Any): Int
}
