package com.scorpio4.vendor.sesame.fn;
 /*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */

import com.scorpio4.util.IdentityHelper;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a custom SPARQL function that generates a unique (UUID) string
 */
public class UUID extends CustomFunction {
	private static final Logger log = LoggerFactory.getLogger(Contrive.class);

	public UUID() {
	}

	@Override
	public String getFunctionName() {
		return "UUID";
	}

	/**
 * Executes the UUID function.
 *
 * @return A URI representing a universally unique identifier
 *
 * @throws org.openrdf.query.algebra.evaluation.ValueExprEvaluationException
 *		 if more than one argument is supplied or if the supplied argument is not a literal.
 */

	public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
		if (args.length > 1) {
			throw new ValueExprEvaluationException("fn:UUID() accepts an optional prefix string");
		}

		log.debug("Generate UUID");

		if (args.length==0) {
			return valueFactory.createURI( IdentityHelper.uuid() );
		}

		return valueFactory.createURI( IdentityHelper.uuid(args[0].toString()) );
	}

}
