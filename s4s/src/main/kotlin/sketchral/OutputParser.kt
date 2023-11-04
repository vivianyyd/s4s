package sketchral

import util.Example
import util.U

class OutputParser(output: String) {
    fun parseProperty(): U { TODO() }

    fun getLams(): Lambdas { TODO() }

    fun parsePosEx(): Example { TODO() }

    fun parseNegExPrecision(): Example { TODO() }

    fun parseMaxsat(neg_may: List<Example>): Pair<List<Example>, List<Example>> { TODO() }
}