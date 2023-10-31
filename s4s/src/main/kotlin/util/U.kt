package util

import bottomup.BooleanValue
import bottomup.EvaluationResult
import bottomup.IntValue
import bottomup.Value

interface U {
    /** Maps each example to this node's value in that example, using cached values. */
    fun evaluateFromCachedChildren(examples: List<Example>, cachedChildren: List<EvaluationResult>): EvaluationResult
}

interface UBoolean : U

data class UCmp(val cmp: Cmp, val left: UInt, val right: UInt) : UBoolean {
    override fun evaluateFromCachedChildren(
        examples: List<Example>,
        cachedChildren: List<EvaluationResult>
    ): EvaluationResult {
        assert(cachedChildren.size == 2)
        return examples.associateWith { ex ->
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
        examples: List<Example>,
        cachedChildren: List<EvaluationResult>
    ): EvaluationResult {
        TODO("Not yet implemented")
    }
}

interface UInt : U

data class ULiteral(val value: Int) : UInt {
    override fun evaluateFromCachedChildren(
        examples: List<Example>,
        cachedChildren: List<EvaluationResult>
    ): EvaluationResult {
        TODO("Not yet implemented")
    }
}

data class UParameter(val which: Int) : U {
    override fun evaluateFromCachedChildren(
        examples: List<Example>,
        cachedChildren: List<EvaluationResult>
    ): EvaluationResult {
        TODO("Not yet implemented")
    }
}

data class ULen(val parameter: UParameter) : UInt {
    fun evaluate(examples: List<Example>): EvaluationResult {
        //throw TODO()
        /** len evaluation on U?*/
        val evalRes = mutableMapOf<Example, Value>()
        // examples.forEach { ex -> evalRes.put() }
        return TODO()
    }

    override fun evaluateFromCachedChildren(
        examples: List<Example>,
        cachedChildren: List<EvaluationResult>
    ): EvaluationResult {
        TODO("Not yet implemented")
    }
}

data class UOp(val op: IntOp, val left: UInt, val right: UInt) : UInt {
    override fun evaluateFromCachedChildren(
        examples: List<Example>,
        cachedChildren: List<EvaluationResult>
    ): EvaluationResult {
        TODO("Not yet implemented")
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

enum class BoolOp(val commutative: Boolean = false, val evaluate: (Boolean, Boolean) -> Boolean) {
    AND(true, { x, y -> x && y }),
    OR(true, { x, y -> x || y }),
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
