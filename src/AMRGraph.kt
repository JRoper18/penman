data class AMRGraph(val types : Map<String, AMRConcept>, val relations : Map<String, Array<String?>>) {
    val triples : Set<Triple<String, String, String>> by lazy {
        val set = mutableSetOf<Triple<String, String, String>>()
        for(type in types){
            set.add(Triple(type.key, "instanceof", type.value.type))
            for(role in type.value.roles){
                set.add(Triple(type.key, role.type.toString(), role.value))
            }
        }
        for(relation in relations){
            for(index in relation.value.indices){
                val arg = relation.value[index]
                if(arg != null){
                    set.add(Triple(relation.key, "ARG" + index, arg))
                }
            }
        }
        set.toSet()
    }
}