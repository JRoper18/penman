fun main(args: Array<String>) {
	println(AMRParser.parse("(h / have-03\n" +
			"   :ARG0 (p4 / person\n" +
			"             :quant 4\n" +
			"             :subset-of (p2 / person                       \n" +
			"                            :ARG0-of (s / survive-01)\n" +
			"                            :quant 5)\n" +
			"             :subset (p3 / person\n" +
			"                         :quant 3\n" +
			"                         :ARG1-of (d3 / diagnose-01)))\n" +
			"   :ARG1 (d / disease))").triples)
}