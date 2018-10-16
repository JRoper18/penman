import java.util.*
import kotlin.test.currentStackTrace

object AMRParser {
    fun parse(str : String) : AMRGraph {
        val concepts = mutableMapOf<String, AMRConcept>()
        val unprocessedRelations = mutableMapOf<Pair<String, String>, String>()
        val conceptStack = Stack<Pair<String, String>>()
        val relationStack = Stack<String>()
        val tokens = str.trim().split(Regex("([\\s\\n])|(?<=\\()|(?=\\)|(?<=:))"))
        //Splits it into parens and colons and gets rid of spaces
        var i = 0
        while(i < tokens.size){
            val current = tokens[i]
            when(current){
                "(" -> {
                    //New concept declaration. Our next tokens look like {"(", name, "/", type}
                    val varName = tokens[i + 1]
                    val currentType = tokens[i + 3]
                    conceptStack.push(Pair(varName, currentType))
                    i += 3
                }
                ")" -> {
                    //End concept declaration. Create the concept, clear roles, and add it to the map.
                    //Also add it as an arg to the last relation.
                    val conData = conceptStack.pop()
                    val varName = conData.first
                    val currentType = conData.second
                    val newConcept = AMRConcept(currentType, mutableSetOf())
                    concepts.put(varName, newConcept)
                    if(!relationStack.isEmpty()){
                        //This isn't the root node, so it's something's arg.
                        unprocessedRelations.put(Pair(conceptStack.peek().first, relationStack.pop()), varName)
                    }
                }
                ":" -> {
                    //Start of a relation or a role.
                    val relName = tokens[i + 1];
                    val isConstantRef = !tokens[i + 2].equals("(")
                    if(isConstantRef){
                        unprocessedRelations.put(Pair(conceptStack.peek().first, relName), tokens[i + 2])
                        i++
                    }
                    else{
                        relationStack.push(relName)
                    }
                    i++
                }
            }
            i++
        }
        val relationMap = mutableMapOf<String, Array<String?>>()
        for(key in unprocessedRelations.keys){
            val sourceConcept = key.first;
            val relName = key.second;
            val destConcept = unprocessedRelations.get(key)!!
            if(relName.startsWith("ARG")){
                //It's a relation
                val num : Int;
                val fromDest : String
                val toDest : String
                if(relName.endsWith("-of")){
                    //It's an INVERSE relation
                    num = Integer.parseInt(relName.substring(3, relName.length - 3))
                    fromDest = destConcept //Swap from and to if it's inverse
                    toDest = sourceConcept
                }
                else {
                    num = Integer.parseInt(relName.substring(3, relName.length))
                    fromDest = sourceConcept
                    toDest = destConcept
                }
                if(!relationMap.containsKey(fromDest)){
                    /*
                    TODO: PropBank holds specs for all possible concept types. We could use this to actually get the expected # of args
                    Found here: https://amr.isi.edu/doc/propbank-amr-frames-arg-descr.txt */
                    relationMap.put(fromDest, arrayOfNulls(9)) //Max 9 args
                }
                var argMap = relationMap.get(fromDest)!!
                argMap[num] = toDest
            }
            else{
                //Non-core role
                val conceptToEdit = concepts.get(sourceConcept)!!
                conceptToEdit.roles.add(AMRRole(AMRRoleType.valueOf(relName.toUpperCase().replace("-", "")), destConcept))
            }
        }
        return AMRGraph(concepts, relationMap)
    }
}