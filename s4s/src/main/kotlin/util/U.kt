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

    override fun toString() = "$left $cmp $right"
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

    override fun toString() = when (op) {
        BoolOp.AND, BoolOp.OR -> "$left $op $right"
        BoolOp.NOT -> "$op $left"
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

    override fun toString() = "$value"
}

/**
 * @param parameter: The index of the parameter. The output is index -1.
 */
data class ULen(val parameter: Int) : UInt {
    fun evaluate(query: Query): EvaluationResult {
        return query.examples.associateWith { ex ->
            val element = if (parameter == -1) ex.output else ex.inputs[parameter]
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

    override fun toString() = if (parameter == -1) "len(out)" else "len(x$parameter)"
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

    override fun toString() = "$left $op $right"
}

enum class Cmp(val commutative: Boolean = false, val evaluate: (Int, Int) -> Boolean, val str: String) {
    EQ(true,
        { x, y -> x == y }, "="
    ),
    LT(evaluate = { x, y -> x < y }, str = "<"),
    GT(evaluate = { x, y -> x > y }, str = ">"),
    LE(evaluate = { x, y -> x <= y }, str = "<="),
    GE(evaluate = { x, y -> x >= y }, str = ">=");

    override fun toString() = this.str
}

enum class BoolOp(val commutative: Boolean = false, val evaluate: (Boolean, Boolean?) -> Boolean, val str: String) {
    AND(true, { x, y -> x && y!! }, "&&"),
    OR(true, { x, y -> x || y!! }, "||"),
    NOT(evaluate = { x, _ -> !x }, str = "!");

    override fun toString() = this.str
}

enum class IntOp(val commutative: Boolean = false, val evaluate: (Int, Int) -> Int, val str: String) {
    ADD(true, { x, y -> x + y }, "+"),
    MUL(true, { x, y -> x * y }, "*"),
    SUB(evaluate = { x, y -> x - y }, str = "-");
//    DIV

    override fun toString() = this.str
}

interface UPrimImpl {
    /** Must be supported for all input and output types to the queried function. */
    fun len(x: Any): Int
}
