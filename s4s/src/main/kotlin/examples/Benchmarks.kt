import bottomup.BottomUp
import sketchral.InputFactory
import sketchral.OutputParser
import sketchral.Result
import sketchral.withNegEx
import util.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit 

data class TestQuery(query:Query){
    val map = mutableMapOf<String, Func>()
    val path = "test"
    init(query:Query){
      
    } 
    fun runTest(func: Func,query:Query){}
    /*{var ifac = InputFactory(func, ListQuery::listquery)
    val synth = callSketch(ifac.synthInput(listOf(), mapOf()))
    var res = OutputParser(synth, ifac).parseProperty()
    if (res !is Result.Ok) return;
    var phi = res.value
  //////this is where we need to put in the redirect of output and send it to path
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
    }
 */   
}