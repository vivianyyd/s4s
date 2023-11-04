package sketchral

import util.Example
import util.Query
import util.U

class InputGenerator(query: Query) {
    val freshNum = 0
    val numAtom = 1
    val minimizeTerms = false  // TODO add commandline flag later
    val template: Template

    init {
        template = SpyralParser(query)
    }

    // DO NOT TRANSLATE THESE THEY ARE USELESS
    /*
    private fun generators(): String {
        val rules = template.getGeneratorRules()
        return rules.joinToString(separator = "\n") { ruleToCode(it) }
    }
    private fun ruleToCode(rule: Any): String {
        val typ = rule[0]
        val symbol = rule[1]
        val exprList = rule[2]

        val context = template.getContext()
        val numCallsPrev = mutableMapOf()

        val argDefn = template.getArgsDefn()

        var code = "generator ${typ} ${symbol}_gen(${argDefn}) {\n"
        code += "\tint t = ??;\n"
        exprList.forEachIndexed { n, e ->
            val numCalls = countGeneratorCalls(context, e)
            code += subcallGen(context, numCallsPrev, numCalls)
            numCallsPrev = max_dict(numCallsPrev, numCalls)

            val contextInit = context.mapValues { _ -> 0 }
            val (_, eCode, eOut) = exprToCode(contextInit, e, typ)

            if (n + 1 == exprList.size) {
                code += eCode
                code += "\treturn ${eOut};\n"
            } else {
                code += "\tif (t == ${n}) {\n"
                code += eCode
                code += "\t\treturn ${eOut};\n"
                code += "\t}\n"
            }
        }
        code += "}\n"
        return code
    }
    */

    // TODO I think the below compare is a translation of part of the spec language into sketch. So let's do this later
    /*
    def __compare(self):
        code = 'generator boolean compare(int x, int y) {\n'

        # This seems more efficient than the regex style
        code += '\tint t = ??;\n'
        code += '\tif (t == 0) { return x == y; }\n'
        code += '\tif (t == 1) { return x <= y; }\n'
        code += '\tif (t == 2) { return x >= y; }\n'
        code += '\tif (t == 3) { return x < y; }\n'
        code += '\tif (t == 4) { return x > y; }\n'
        code += '\treturn x != y; \n'

        code += '}'

        return code
    */

    private fun lamFunctions(lams: Lambdas) = lams.values.joinToString(postfix = "\n\n", separator = "\n\n")

    private fun posExamples(pos: Examples): String {
        var code = ""
        pos.forEachIndexed { i, ex ->
            code += "\nharness void positive_example_$i () {\n$ex\n}\n\n"
        }
        return code
    }

    private fun negExamplesSynth(negMust: Examples, negMay: Examples): String {
        var code = ""
        (negMay + negMust).forEachIndexed { i, ex ->
            code += "\nharness void negative_example_$i () {\n$ex\n}\n\n"
        }
        return code
    }

    private fun propertyCode(maxsat: Boolean = false): String {
        fun propertyGenCode(n: Int) = (0 until n).joinToString(separator = " || ") { "atom_$it" }

        val propertyGenSymbol = template.getGeneratorRules()[0][1]  // TODO this will specify U

        val argCall = template.getArgsCall()
        val argDefn = template.getArgsDefn()
        val atomGen = "${propertyGenSymbol}_gen(${argCall})"
        var code: String = ""  // generators() + "\n\n" + compare() + "\n\n"

        // Emit generator for property
        code += "generator boolean property_gen(${argDefn}) {\n"
        code += "\tif (??) { return false; }\n"
        if (minimizeTerms && !maxsat) {
            code += "\tint t = ??;\n"
            for (i in 0 until numAtom) {
                val propertyGen = propertyGenCode(i + 1)
                code += "\tboolean atom_$i = ${atomGen};\n"
                code += "\tif (t == ${i + 1}) { return ${propertyGen}; }\n"
            }
            code += "\tminimize(t);\n"
        } else {
            for (i in 0 until numAtom) {
                code += "\tboolean atom_${i} = ${atomGen};\n"
            }
            val propertyGen = propertyGenCode(numAtom)
            code += "\treturn ${propertyGen};\n"
        }
        code += "}\n\n"

        // Set property to be the result of calling generator
        code += "void property(${argDefn}, ref boolean out) {\n"
        code += "\tout = property_gen(${argCall});\n"
        code += "}\n\n"
        return code
    }

    fun synthInput(pos: Examples, negMust: Examples, negMay: Examples, lams: Lambdas): String {
        var code: String = template.impl()
        code += lamFunctions(lams)
        code += posExamples(pos)
        code += negExamplesSynth(negMust, negMay)
        code += propertyCode()
        return code
    }

    fun maxsatInput(pos: Examples, negMust: Examples, negMay: Examples, lams: Lambdas): String {
        TODO()
    }

    fun soundnessInput(phi: U, lams: Lambdas): String {
        TODO()
    }

    fun precisionInput(
        phi: U,
        phiList: List<U>,
        pos: Examples,
        negMust: Examples,
        negMay: Examples,
        lams: Lambdas
    ): String {
        TODO()
    }
}
