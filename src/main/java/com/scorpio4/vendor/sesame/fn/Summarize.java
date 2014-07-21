package com.scorpio4.vendor.sesame.fn;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */
import net.sf.classifier4J.summariser.SimpleSummariser;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * scorpio4 (c) 2013
 * Module: com.scorpio4.vendor.sesame.fn
 * @author lee
 * Date  : 29/10/2013
 * Time  : 7:22 PM
 */
public class Summarize extends CustomFunction {

    public Summarize() {
    }

    @Override
    public String getFunctionName() {
        return "summarize";
    }

    @Override
    public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
        if (args.length<1) throw new ValueExprEvaluationException("Missing term to summarize");
        if (args.length>2) throw new ValueExprEvaluationException("Too many terms");
        SimpleSummariser summariser = new SimpleSummariser();
        int lines = args.length>1?((Literal)args[1]).intValue():1;
        if (lines<1||lines>32) throw new ValueExprEvaluationException("Number of lines must be between 1 and 32");
        String summary = summariser.summarise(args[0].stringValue(),lines);
        return valueFactory.createLiteral(summary);
    }
}
