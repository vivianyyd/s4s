package util

interface U

interface UBoolean : U

data class UCmp(val cmp: Cmp, val left: UInt, val right: UInt) : UBoolean

data class UBOp(val op: BoolOp, val left: UBoolean, val right: UBoolean) : UBoolean

interface UInt : U

data class UVariable(val name: String) : U

data class ULen(val variable: UVariable) : UInt

data class UOp(val op: IntOp, val left: UInt, val right: UInt) : UInt

enum class Cmp(val commutative: Boolean = false) {
    EQ(true), LT, GT, LE, GE
}

enum class BoolOp(val commutative: Boolean = false) {
    AND(true), OR(true), NOT
}

enum class IntOp(val commutative: Boolean = false) {
    ADD(true), MUL(true), SUB;
//    DIV

    override fun toString() = super.toString().lowercase()
}

interface UPrimImpl {
    /** Must be supported for all input and output types to the queried function. */
    fun len(x: Any): Int
}
