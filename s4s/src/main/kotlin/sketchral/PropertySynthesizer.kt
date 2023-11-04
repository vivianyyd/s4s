package sketchral

import com.sun.tools.javac.comp.Todo
import util.Query
import util.Example
import util.U

typealias Lambdas = Map<String, String>
typealias Examples = List<Example>

class PropertySynthesizer(query: Query) {
    private val inputGenerator = InputGenerator(query)

    // TODO timeout variable up here and make each step check for timeout

    val phiTruth = "out = true" // Synthesized property. Might delete

    private fun callSketch(sketchInput: String): Pair<String?, Int> {
        // TODO Write temp file containing [code] as the sketch input
        try {
            TODO()
            // TODO Run sketch with temp file, time it
            /*
            Spyro does:
            output = subprocess.check_output(
                [SKETCH_BINARY_PATH, path,
                    '--bnd-inline-amnt', str(self.__inline_bnd),
                    '--slv-seed', str(self.__slv_seed),
                    '--slv-timeout', f'{self.__timeout / 60.0:2f}'],
                stderr=subprocess.PIPE)
             */
            // TODO return output, time
        } catch (e: Exception) {
            TODO()
            // TODO handle. Might also handle timeout separately
        }
    }
/**
 * */
    fun synthesize(pos: Examples, negMust: Examples, negMay: Examples, lams: Lambdas): Pair<U?, Lambdas?> {
        // TODO Run Sketch with temp file
        val code = inputGenerator.synthInput(pos, negMust, negMay, lams)
        val (output, time) = callSketch(code)
        if (output != null) {
            val outParser = OutputParser(output)
            val phi = outParser.parseProperty()
            val lam = outParser.getLams()
            return Pair(phi, lam)
        } else return Pair(null, null)
    }
/**
 *
 * arg notes: lams we have no idea about and may be perpetually empty
 * maxsatInput generates harnesses that are required to fulfill the neg must example,and harnesses that fulfill as many may conditions as possible
 * maxSynth tries to run sketch on the maxSat harnesses,
 * ==> this returns the result of one step of trying to generate new L properties off of the old one and fulfilling as many "may"s as possible
 *     the output is: additional mays, changes, synthesized property, and synthesized lambdas.
 *
 * **/
    fun maxSynth(
        pos: Examples,
        negMust: Examples,
        negMay: Examples,
        lams: Lambdas,
        phiInit: U?
    ): Pair<Pair<Examples, Examples>, Pair<U, Lambdas>> {
        val code = inputGenerator.maxsatInput(pos, negMust, negMay, lams)
        val (output, elapsed_time) = callSketch(code)
        if (output != null) {
            val outParser = OutputParser(output)
            val (newNegMay, delta) = outParser.parseMaxsat(negMay)
            val phi = outParser.parseProperty()
            val lam = outParser.getLams()
            return Pair(Pair(newNegMay, delta), Pair(phi, lam))
        } else {
            if (phiInit == null) throw Exception("MaxSynth failed")
            val (newNegMay, delta) = Pair(listOf<Example>(), negMay)
            return Pair(Pair(newNegMay, delta), Pair(phiInit, lams))
        }
    }

    fun checkSoundness(phi: U, lams: Lambdas): Triple<Example?, Lambdas?, Boolean> {
        val code = inputGenerator.soundnessInput(phi, lams)
        val (output, elapsed_time) = callSketch(code)
        if (output != null) {
            val outParser = OutputParser(output)
            val e_pos = outParser.parsePosEx()
            val lam = outParser.getLams()
            return Triple(e_pos, lam, false)
        } else {
            return Triple(null, null, elapsed_time >= timeout)
        }
    }

    fun checkPrecision(
        phi: U,
        phiList: List<U>,
        pos: Examples,
        negMust: Examples,
        negMay: Examples,
        lams: Lambdas
    ): Triple<Example?, U?, Lambdas?> {
        val code = inputGenerator.precisionInput(phi, phiList, pos, negMust, negMay, lams)
        val (output, elapsed_time) = callSketch(code)
        if (output != null) {
            val outParser = OutputParser(output)
            val e_neg = outParser.parseNegExPrecision()
            val phi = outParser.parseProperty()
            val lam = outParser.getLams()
            return Triple(e_neg, phi, lam)
        } else {
            return Triple(null, null, null)
        }
    }
    // TODO the above is up to line 288 of property_synthesizer.py

    /**not 100%sure if most precise is a list or a set/conjunct/single yet
      **/
    /**
     * pos: set of positive examples
     * negMust: set of negative examples
     * negMay: set of potential negative examples
     * lams: ????????? seem to be set of functions that are produced along
     *          with the set of properties, probably hand in hand/ part of the definition.
     *          might just be a part of synthesizing the best l property conjunction
     * phiList: seems to be a set of consistent phi's
     * phi truth: string value
     * phi init is an initial phi we get from synthesizeAllProperties
     * mostprecise : boolean, usually true:otherwise turned on when user hyperparameter disable_min is turned off.
     * updatePsi: from what i can tell always false.
     * **/
    /**
     * **/
    fun synthesizeProperty(
        pos: Examples,
        negMust: Examples,
        negMay: Examples,
        lams: Lambdas,
        phiList:List<U?>,
        phiInit: U?,
        mostPrecise: Boolean,
    updatePsi: Boolean
    ): Pair<Pair<Examples, Examples>, Triple<U, Lambdas,U>> {
    return TODO()
    }

        fun synthesizeAllProperties():Pair<List<U>,List<U?>> {
            val phiList = listOf<U?>()
            val funList = listOf<U?>()
            val pos = listOf<Example>()
            val negMay = listOf<Example>()
            val lamFunctions = listOf<U?>()
            while(true) {
                /**here we need to **/
            }
            return TODO()
    }
    /**
     * in between synthesize all props and run is a bunch of statistics code
     * is very long, will do it another day ==> has nothing to do with actually synthesizing,
     * looks useful for analysis.
     *
     * */
    fun run():Triple<List<U>,List<U?>, List<Float>?>
    {
        /**skipping logging code insert*/
        val (phiList,funList) = synthesizeAllProperties()
        val statistics = null
        /**skipping logging code insert*/
        return Triple(phiList, funList,statistics)
    }
}
