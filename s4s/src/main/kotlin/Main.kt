import bottomup.BottomUp
import sketchral.InputFactory
import sketchral.OutputParser
import sketchral.Result
import sketchral.withNegEx
import util.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit 
 
/*fun bottomUpTests() {
    val bu = BottomUp(listquery)
    println("Running bottom-up for MutableList.add")
    println("Found ${bu.enumerate(addFunc, 6)}")
    println("Running bottom-up for MutableList.addAll")
    println("Found ${bu.enumerate(addAllFunc, 6)}")
    println("Running bottom-up for duplicate")
    println("Found ${bu.enumerate(dupFunc, 6)}")
}*/

fun writeToTmp(content: String) = File("tmp.sk").printWriter().use { out -> out.println(content) }

fun callSketch(input: String): String {
    writeToTmp(input)
    return ("sketch tmp.sk -V 5 --slv-nativeints --bnd-inline-amnt 5".runCommand())
        ?: throw Exception("I'm sad")
}

//val tests = listOf(ListQuery(), ArithmeticQuery)
fun main(args: Array<String>) {
//    bottomUpTests()
    val func = addAllFunc
    var ifac = InputFactory(func, listquery)
    val synth = callSketch(ifac.synthInput(listOf(), mapOf()))
    var res = OutputParser(synth, ifac).parseProperty()
    if (res !is Result.Ok) return;
    var phi = res.value
    println("Initial synthesized property: $phi")

    while(true){
        val precision = callSketch(ifac.precisionInput(phi, listOf(), listOf(), mapOf()))
        val op = OutputParser(precision, ifac)
        val result = op.parseProperty()
        if (result is Result.Ok) {
            phi = result.value
            println("Property with increased precision: $phi")
            ifac = ifac.withNegEx(op.parseNegExPrecision())
        }
        else break
    }


//    val input = generateSequence(::readLine).joinToString("\n")
//    val jsonElement = Json.parseToJsonElement(input)
//    val rawProgram = Json.decodeFromJsonElement<RawProgram>(jsonElement)
//    val prettyJsonPrinter = Json { prettyPrint = true }
//    println(prettyJsonPrinter.encodeToString(optimizedProgram))
}

fun String.runCommand( 
    workingDir: File = File(System.getProperty("user.dir"))
): String? {
    try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

