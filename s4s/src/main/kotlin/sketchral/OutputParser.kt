package sketchral

import util.Example
import util.U

class OutputParser(val output: String) {
    fun parseProperty(): String {
        var left = output.substringAfterLast("void property (")
        left = left.substringAfter('{')
        left = left.substringBefore('}')
        val lines = left.split(';').map { it.trim() }
        val lenStoreToName = lines.filter { "length" in it }.associate {
            var line = it.substringAfter('(')  // "parameter, variable_len_stored_in)"
            val variable = line.substringBefore(',')
            line = line.substringAfter(',')
            val storedLen = line.substringBefore(')').trim()
            storedLen to variable
        }.toSortedMap(reverseOrder())  // natural order puts short strings first, but we want to replace substrings after
        var property = lines.find { "out =" in it }?.substringAfter("out =")?.trim() ?: throw Exception("I'm sad")
        lenStoreToName.forEach { (lenStore, name) -> property = property.replace(lenStore, "length($name)")}
        return "$property;"
    }

    fun getLams(): Lambdas {
        TODO()
    }

    fun parsePosEx(): Example {
        TODO()
    }

    fun parseNegExPrecision(): Example {
        TODO()
    }

    fun parseMaxsat(neg_may: List<Example>): Pair<List<Example>, List<Example>> {
        TODO()
    }
}