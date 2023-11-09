package sketchral

import util.Example
import util.Func
import util.Query
import util.U

class InputFactory(val function: Func, val query: Query) {
    private val numAtom = 1  // TODO add flag later
    private val minimizeTerms = false  // TODO add commandline flag later
    private val numInputs = function.type.inputs.size
    private val argToDummy = mutableMapOf<Any, Int>()
    private val paramsWithLen =
        (0..numInputs).filter { (if (it == numInputs) -1 else it) !in function.argsWithUndefinedLength }

    init {
        // Make dummy values for examples
        function.examples.flatMap { (it.inputs + listOf(it.output)) }.toSet().forEachIndexed { i, arg ->
            argToDummy[arg] = i
        }
    }

    private fun paramToName(param: Int) = if (param == numInputs) "o" else "x$param"

    private val argsDefn = (0..numInputs).joinToString(separator = ", ") { "int ${paramToName(it)}" }

    private val argsCall = (0..numInputs).joinToString(separator = ", ") { paramToName(it) }

    /** Gonna keep this til we understand it */
    private fun lamFunctions(lams: Lambdas) = lams.values.joinToString(postfix = "\n", separator = "\n")

    /**
     * Spyro generates code for positive examples which looks like:
     *      int o = 0;
     *      max2(-12, -2, o);
     *      boolean out;
     *      property(-12, -2, o, out);
     *      assert out;
     * For the same example, we write:
     *      boolean out;
     *      property(-12, -2, -1, out);
     *      assert out;
     * Spyro generates code for negative examples which looks like:
     *      boolean out;
     *      property(-31, -30, -30, out);
     *      assert !out;
     * For the same example, we generate the same code.
     */
    private fun sketchEx(ex: Example, negative: Boolean): String {
        val lines = mutableListOf<String>()
        lines.add("\tboolean out;")

        // Declare and define values
        lines.addAll(ex.args.mapIndexed { i, arg ->
            "int ${paramToName(i)} = ${argToDummy[arg]};"
        })

        lines.add("property($argsCall, out);")
        lines.add("assert ${if (negative) "!" else ""}out;")
        return lines.joinToString(separator = "\n\t")
    }

    private fun posExamples(pos: Examples): String {
        val sk = StringBuilder()
        pos.forEachIndexed { i, ex ->
            sk.append("harness void positive_example_$i () {\n${sketchEx(ex, false)}\n}\n")
        }
        return sk.toString()
    }

    private fun negExamplesSynth(negMust: Examples, negMay: Examples): String {
        val sk = StringBuilder()
        (negMay + negMust).forEachIndexed { i, ex ->
            sk.append("\nharness void negative_example_$i () {\n${sketchEx(ex, true)}\n}\n\n")
        }
        return sk.toString()
    }

    private fun propertyCode(maxsat: Boolean = false): String {
        fun propertyGenCode(n: Int) = (0 until n).joinToString(separator = " || ") { "atom_$it" }

        val atomGen = "U_gen(${argsCall}, n)"
        val sk = StringBuilder()

        // Emit generator for property
        sk.append("generator boolean property_gen(${argsDefn}) {\n")
        sk.append("\tif (??) { return false; }\n")
        sk.append("\tint n = ??;\n")
        if (minimizeTerms && !maxsat) {
            sk.append("\tint t = ??;\n")
            for (i in 0 until numAtom) {
                val propertyGen = propertyGenCode(i + 1)
                sk.append("\tboolean atom_$i = ${atomGen};\n")
                sk.append("\tminimize(n);\n")
                sk.append("\tif (t == ${i + 1}) { return ${propertyGen}; }\n")
            }
            sk.append("\tminimize(t);\n")
        } else {
            for (i in 0 until numAtom) {
                sk.append("\tboolean atom_${i} = ${atomGen};\n")
            }
            val propertyGen = propertyGenCode(numAtom)
            sk.append("\tminimize(n);\n")
            sk.append("\treturn ${propertyGen};\n")
        }
        sk.append("}\n")

        // Set property to be the result of calling generator
        sk.append("void property(${argsDefn}, ref boolean out) {\n")
        sk.append("\tout = property_gen(${argsCall});\n")
        sk.append("}\n")
        return sk.toString()
    }

    private val setup by lazy {
        // Declare length function
        val ld = mutableListOf("int length(int x) {")
        function.examples.flatMap { it.args.filterIndexed { i, _ -> i in paramsWithLen } }.toSet().forEach { arg ->
            ld.add("if (x == ${argToDummy[arg]}) { return ${query.lens[arg]}; }")
        }
        ld.add("assert false;")
        ld.joinToString(separator = "\n\t", postfix = "\n}\n")
    }

    fun synthInput(pos: Examples, negMust: Examples, negMay: Examples, lams: Lambdas): String {
        val sk = StringBuilder()
        sk.append(setup)
        sk.append(uGrammar)
        sk.append(lamFunctions(lams))
        sk.append(posExamples(pos))
        sk.append(negExamplesSynth(negMust, negMay))
        sk.append(propertyCode())
        return sk.toString()
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

    /**
     * Generator for elements of U
     */
    private val uGrammar by lazy {
        // I guess we don't need &&,|| since || is part of propertygen and && is built
        // into synthall. and we don't need not, since each compare has a not
        val sb = StringBuilder()

        // The toplevel predicate non-terminal
        val uGen = mutableListOf("generator boolean U_gen($argsDefn, int n) {")
        uGen.add("if (n > 0) {")
        uGen.add("\tint e1 = E_gen($argsCall, n - 1);")
        uGen.add("\tint e2 = E_gen($argsCall, n - 1);")
        uGen.add("\treturn compare(e1, e2, n - 1);")
        uGen.add("}")
        uGen.add("assert false;")
        sb.append(uGen.joinToString(separator = "\n\t"))
        sb.append("\n}\n")

        val compareGen = mutableListOf("generator boolean compare(int x, int y, int n) {")
        compareGen.add("if (n > 0) {")
        compareGen.add("\tint t = ??;")
        compareGen.add("\tif (t == 0) { return x == y; }")
        compareGen.add("\tif (t == 1) { return x <= y; }")
        compareGen.add("\tif (t == 2) { return x >= y; }")
        compareGen.add("\tif (t == 3) { return x < y; }")
        compareGen.add("\tif (t == 4) { return x > y; }")
        compareGen.add("\treturn x != y;")
        compareGen.add("}")
        compareGen.add("assert false;")
        sb.append(compareGen.joinToString(separator = "\n\t"))
        sb.append("\n}\n")

        // Integer expressions
        val eGen = mutableListOf("generator int E_gen($argsDefn, int n) {")
        eGen.add("if (n > 0) {")
        eGen.add("\tint t = ??;")
        eGen.add("\tif (t == 0) { return 0; }")
        eGen.add("\tif (t == 1) { return 1; }")
        paramsWithLen.forEachIndexed { tOffset, param ->
            eGen.add("\tif (t == ${tOffset + 2}) { return length(${if (param == numInputs) "o" else "x$param"}); }")
        }
        eGen.add("\tint e1 = E_gen($argsCall, n - 1);")
        eGen.add("\tint e2 = E_gen($argsCall, n - 1);")
        eGen.add("\treturn op(e1, e2, n - 1);")
        eGen.add("}")
        eGen.add("assert false;")
        sb.append(eGen.joinToString(separator = "\n\t"))
        sb.append("\n}\n")

        val opGen = mutableListOf("generator int op(int x, int y, int n) {")
        opGen.add("if (n > 0) {")
        opGen.add("\tint t = ??;")
        opGen.add("\tif (t == 0) { return x + y; }")
        opGen.add("\tif (t == 1) { return x * y; }")
        opGen.add("\treturn x - y;")
        opGen.add("}")
        opGen.add("assert false;")
        sb.append(opGen.joinToString(separator = "\n\t"))
        sb.append("\n}\n")

        sb.append("\n")
        sb.toString()
    }
}
