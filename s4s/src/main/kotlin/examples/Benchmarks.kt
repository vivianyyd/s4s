import bottomup.BottomUp
import sketchral.InputFactory
import sketchral.OutputParser
import sketchral.Result
import sketchral.withNegEx
import util.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit 

data class TestQuery(val  functions:List<Func>, val names:List<String> , val query:Query){
    val map: Map<String, Func>
    val path = "."
    init{
      map = mutableMapOf<String, Func>()
      for(i in 0..names.size){
        map.put(names.get(i), functions.get(i))
      }
    } 
    fun runTest(){
        for(key in map.keys){ 
        var file: String = path+"/"+key+".txt"
        var func = map.get(key)!!
        var ifac = InputFactory(func, query)
        val synth = callSketch(ifac.synthInput(listOf(), mapOf()))
        var res = OutputParser(synth, ifac).parseProperty()
        if (res !is Result.Ok) return;
        var phi = res.value
        //////this is where we need to put in the redirect of output and send it to path
        File(file).writeText("Initial synthesized property: $phi")
        while(true){
            val precision = callSketch(ifac.precisionInput(phi, listOf(), listOf(), mapOf()))
            val op = OutputParser(precision, ifac)
            val result = op.parseProperty()
            for(input in func.posExamples){
                File(file).writeText("Example:     "+input)
            }
            if (result is Result.Ok) {
                phi = result.value
                File(file).writeText("Property with increased precision: $phi")
                ifac = ifac.withNegEx(op.parseNegExPrecision())
            }
            else break
        }
        }
    } 
}