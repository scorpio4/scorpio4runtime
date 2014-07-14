package com.scorpio4.vendor.sesame.fn;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */

import org.apache.commons.codec.digest.DigestUtils;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A custom SPARQL function that derives (contrives) a permanent URI from a set of 1 or more values
 * The 1st argument is the prefix, subsequent arguments are used to contrive the permanent URI
 *
 * To build the custom function, ensure that
 * ./META-INF/services/org.openrdf.query.algebra.evaluation.function.Function is configured correctly
 * i.e. it contains a line "com.scorpio4.vendor.sesame.fn.Contrive"
 *
 * To use the custom function, copy the JAR file into the classpath for Sesame.
 * Test the function using:
	 PREFIX fn: <http://scorpio4.com/openrdf/function/>

	 SELECT ?this ?that WHERE {
	 ?this a rdfs:Class.
	 BIND(fn:contrive("urn:example:contrived:",?this) AS ?that)
	 }

 *
 *
 */
public class Contrive extends CustomFunction {
	private static final Logger log = LoggerFactory.getLogger(Contrive.class);

	public Contrive() {
	}

	public String getFunctionName() {
		return "contrive";
	}

	/**
	 * Executes the Contrive function.
	 *
	 * @return A URI representing a universally unique identifier
	 *
	 * @throws org.openrdf.query.algebra.evaluation.ValueExprEvaluationException
	 *		 if more insufficient arguments are supplied
	 */

	public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
		if (args.length < 2)  throw new ValueExprEvaluationException("contrive() requires a mandatory prefix and at least one value");

		// get the prefix (1st argument)
		Value prefixURI = args[0];
		return valueFactory.createURI(_evaluate(prefixURI.stringValue(), args));
	}

	public String _evaluate(String prefixURI, Value... args) {
		// get the contrived values (multi-part keys)
		StringBuilder contrived = new StringBuilder();
		for(int i=1;i<args.length;i++) contrived.append(args[i].stringValue());
		// append the SHA hash of the contrived values to the prefix
		return prefixURI + DigestUtils.shaHex(contrived.toString());
	}
}
