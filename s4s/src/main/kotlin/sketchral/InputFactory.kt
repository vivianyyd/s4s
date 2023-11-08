package sketchral

import util.Example
import util.Query
import util.U

class InputFactory(val query: Query) {
    private val numAtom = 1
    private val minimizeTerms = false  // TODO add commandline flag later
    private val numInputs = query.type.inputs.size

    /** Here, params correspond to indices: inputs at 0..n-1, output at n */
    private val paramToSketchType: List<String>

    private val argToConstructorCall = mutableMapOf<Any, String>()
    private val argToVVal = mutableMapOf<Any, Int>()
    private val typeToArgs = mutableMapOf<String, MutableSet<Any>>()

    init {
        // Build up map of dummy types for params
        var i = 0
        val kotlinToInt = (query.type.inputs + listOf(query.type.output)).toSet().associateWith { i++; i }
        paramToSketchType = (0..numInputs).map {
            if (it == numInputs)
                "T${kotlinToInt[query.type.output]!!}"
            else
                "T${kotlinToInt[query.type.inputs[it]]!!}"
        }

        // Build up map of constructors of dummy values for examples
        var j = 0
        query.examples.forEach { example ->
            val argToConstructorCallMini = paramToSketchType.withIndex().associate { (i, ty) ->
                val arg = if (i == numInputs) example.output else example.inputs[i]

                if (ty in typeToArgs) typeToArgs[ty]!!.add(arg) else typeToArgs[ty] = mutableSetOf(arg)

                argToVVal[arg] = j
                Pair(arg, "new $ty(v=${j++})")
            }
            argToConstructorCall.putAll(argToConstructorCallMini)
        }
    }

    private fun lenDefinedForParam(param: Int) =
        (if (param == numInputs) -1 else param) !in query.argsWithUndefinedLength

    private fun paramToName(param: Int) = if (param == numInputs) "o" else "x$param"

    private val argsDefn = (0..numInputs).joinToString(separator = ", ") {
        "${paramToSketchType[it]} ${paramToName(it)}"
    }

    private val argsCall = (0..numInputs).joinToString(separator = ", ") {
        paramToName(it)
    }

    private fun sketchVals(args: List<Any>) = args.map { argToConstructorCall[it] }.joinToString(separator = ", ")

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
            "${paramToSketchType[i]} ${paramToName(i)} = ${argToConstructorCall[arg]};"
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

        val atomGen = "U_gen(${argsCall})"
        val sk = StringBuilder()

        // Emit generator for property
        sk.append("generator boolean property_gen(${argsDefn}) {\n")
        sk.append("\tif (??) { return false; }\n")
        if (minimizeTerms && !maxsat) {
            sk.append("\tint t = ??;\n")
            for (i in 0 until numAtom) {
                val propertyGen = propertyGenCode(i + 1)
                sk.append("\tboolean atom_$i = ${atomGen};\n")
                sk.append("\tif (t == ${i + 1}) { return ${propertyGen}; }\n")
            }
            sk.append("\tminimize(t);\n")
        } else {
            for (i in 0 until numAtom) {
                sk.append("\tboolean atom_${i} = ${atomGen};\n")
            }
            val propertyGen = propertyGenCode(numAtom)
            sk.append("\treturn ${propertyGen};\n")
        }
        sk.append("}\n")

        // Set property to be the result of calling generator
        sk.append("void property(${argsDefn}, ref boolean out) {\n")
        sk.append("\tout = property_gen(${argsCall});\n")
        sk.append("}\n")
        return sk.toString()
    }

    private val setup by lazy { // TODO add type decls
        val sk = StringBuilder()
        val td = mutableListOf<String>()
        // Declare types
        paramToSketchType.toSet().forEach {
            td.add("struct $it { int v; }")
        }
        sk.append(td.joinToString(separator = "\n", postfix = "\n"))

        // Declare length functions
        (0..numInputs).filter {
            lenDefinedForParam(it)
        }.map { paramToSketchType[it] }.toSet().forEach {ty ->
            val ld = mutableListOf("int length$ty($ty x) {")
            typeToArgs[ty]!!.forEach { arg ->
                ld.add("if (x.v == ${argToVVal[arg]}) { return ${query.lens[arg]}; }")
            }
            ld.add("return -1;")  // bottom value if no matches
            sk.append(ld.joinToString(separator="\n\t", postfix="\n}\n"))
        }

        sk.toString()
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
        val uGen = mutableListOf("generator boolean U_gen($argsDefn) {")
        uGen.add("int e1 = E_gen($argsCall);")
        uGen.add("int e2 = E_gen($argsCall);")
        uGen.add("return compare(e1, e2);")
        sb.append(uGen.joinToString(separator = "\n\t"))
        sb.append("\n}\n")

        val compareGen = mutableListOf("generator boolean compare(int x, int y) {")
        compareGen.add("int t = ??;")
        compareGen.add("if (t == 0) { return x == y; }")
        compareGen.add("if (t == 1) { return x <= y; }")
        compareGen.add("if (t == 2) { return x >= y; }")
        compareGen.add("if (t == 3) { return x < y; }")
        compareGen.add("if (t == 4) { return x > y; }")
        compareGen.add("return x != y;")
        sb.append(compareGen.joinToString(separator = "\n\t"))
        sb.append("\n}\n")

        // Integer expressions
        val eGen = mutableListOf("generator int E_gen($argsDefn) {")
        eGen.add("int t = ??;")
        eGen.add("if (t == 0) { return 0; }")
        eGen.add("if (t == 1) { return 1; }")
        (0..numInputs).filter { lenDefinedForParam(it) }.forEachIndexed { i, param ->
            eGen.add("if (t == ${i + 2}) { return length${paramToSketchType[param]}(${if (param == numInputs) "o" else "x$param"}); }")
        }
        eGen.add("int e1 = E_gen($argsCall);")
        eGen.add("int e2 = E_gen($argsCall);")
        eGen.add("return op(e1, e2);")
        sb.append(eGen.joinToString(separator = "\n\t"))
        sb.append("\n}\n")

        val opGen = mutableListOf("generator int op(int x, int y) {")
        opGen.add("int t = ??;")
        opGen.add("if (t == 0) { return x + y; }")
        opGen.add("if (t == 1) { return x * y; }")
        opGen.add("return x - y;")
        sb.append(opGen.joinToString(separator = "\n\t"))
        sb.append("\n}\n")

        sb.append("\n")
        sb.toString()
    }
}

/*
Done:
    variables are for ex. list l; list lout. We produce these from the query: x1...xn for n inputs and out for the output
    relation states reverse(l, lout), describes functions with which synth spec. free with query
    generator is the DSL for specs. we hard-code this

example generator - recursive constructor for each type. we'll have to look at the parsing details but this should instead in our impl be a choice between inputs and any output not equal to the true output. key: we only have lengths. I think this might actually be bad since functions aren't unique mappings from size to size. ex filter
we'll tell it len is uninterpreted but give it values on all example elements including ones we synthesize
what about this
pos example
f(x, y) = z
len x, y, z given
negative example
f(x, y) = w
len x, y, w given

We should never need to mention f to sketch at all!! Except for producing negative examples. But we could even get
around that too with asserts, forcing it to only pick things that aren't already pairs.
ie. if (gen inputs == ex1 inputs) assert gen output != ex1 output

This is sketch code, it is polymorphic. Len will be a polymorphic uninterpreted fn
void forall<T>([int n], fun f, ref T[n] x){
    for(int i=0; i<n; ++i){
        f(x[i]);
    }
}
Len will have a bunch of ifs

length is a polymorphic function which has ifs that check if arg is equal to one of the known example values which we pass in, or global values
OR
length is polymorphic uninterpreted function and we have assumes that say its value on example vars

Should we use this for generating negative examples?
Sketch supports the use of the $(type) construct to instruct the synthesizer to consider all variables of the
specified type within scope when searching for a solution.
harness void main(int x) {
int a = 2;
double b = 2.3;
assert x * $(int) == x + x; // $(int) === {| 0 | a | x |}
}
BUT The default
value of any primitive type will also be considered as one of the choices - this may be dangerous
vars can't be uninitialized, so if we have some dummy x of type T, where T mapsto 3, we need int[1][1][1] x = {{{randint}}}; in the sketch. if y == x, we need y to be the same thing

need generation to take in variables else it'll just output garbage

Harness functions are not allowed to take heap allocated objects (struct, adt) as inputs and all
global variables are reset to their initial values before the evaluation of each harness.

uninterpreted function cannot involve structs, even if temporary
    ret_type name(args);
 */
// template.implementation is all the input code except for var, relation, generator, example

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
