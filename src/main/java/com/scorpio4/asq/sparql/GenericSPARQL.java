package com.scorpio4.asq.sparql;
/*
 *

 */

import com.scorpio4.asq.ASQ;
import com.scorpio4.asq.core.LiteralTerm;
import com.scorpio4.asq.core.Pattern;
import com.scorpio4.asq.core.RawTerm;
import com.scorpio4.asq.core.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Fact:Core (c) 2013
 * Module: com.scorpio4.asq.core.sparql
 * @author lee
 * Date  : 11/01/2014
 * Time  : 9:39 PM
 */
public abstract class GenericSPARQL {
	protected static final Logger log = LoggerFactory.getLogger(GenericSPARQL.class);
    protected StringBuilder sparql = new StringBuilder();

    protected GenericSPARQL() {
    }

    protected void buildProjections(StringBuilder sparql, ASQ asq) {
        if (asq==null) return;
        Collection<Pattern> patterns = asq.getPatterns();
        if (patterns==null||patterns.size()==0) return;

        for(Term term: asq.getSelects()) {
            buildProjection(sparql, term);
        }
    }

    protected void buildProjection(StringBuilder sparql, Term term) {
        if (term.isBinding()) sparql.append(term).append(" ");
    }

    protected void buildWherePattern(StringBuilder sparql, ASQ asq) {
        if (asq==null) return;
        sparql.append("{ \n");
        for(Pattern pattern: asq.getPatterns()) {
            buildPattern(sparql, pattern);
        }
        for(Pattern pattern: asq.getFunctions()) {
            buildFunction(sparql, pattern);
        }
        sparql.append("}\n");
    }

    protected void buildPattern(StringBuilder sparql, Pattern pattern) {
	    if (pattern==null || !pattern.isDefined()) return;
        if (pattern.isOptional()) sparql.append("\nOPTIONAL {\n");
        buildTerm(sparql, pattern.getThis());

	    Term has = pattern.getHas();
	    if (has.toString().equalsIgnoreCase("a"))
            sparql.append("a ");
        else
            buildTerm(sparql, has);

        buildTerm(sparql, pattern.getThat());
        sparql.append(".\n");
//        buildWherePattern(sparql, pattern.getNext());

	    for(Pattern nested: pattern.getNested()) {
		    buildPattern(sparql, nested);
	    }

        if (pattern.isOptional()) sparql.append("}\n");
    }

    protected void buildTerm(StringBuilder sparql, Term term) {
        if (term.isFunction()) return;
        if (term.isBinding()) {
	        sparql.append(term.toString()).append(" ");
        }
        else if (term instanceof RawTerm) sparql.append(term).append(" ");
        else sparql.append("<").append(term).append("> ");
    }

    protected void buildFunction(StringBuilder sparql, Pattern pattern) {
        if (pattern.isFilter())  {
            sparql.append("FILTER (").append(pattern.getHas()).append(" )\n");
        } else {
            sparql.append("BIND (").append(pattern.getHas()).append(" AS ");
            buildTerm(sparql, pattern.getThat());
            sparql.append(")\n");
        }
    }

    // BINDINGS FOR UPDATE / CREATE

    protected void buildBindings(StringBuilder sparql, ASQ asq) {
        if (asq==null) return;
        sparql.append("{\n");
        for(Pattern pattern: asq.getPatterns()) {
            buildBinding(sparql, pattern.getThis());
            buildBinding(sparql, pattern.getHas());
            buildBinding(sparql, pattern.getThat());
            sparql.append(".\n");
        }
        sparql.append("}\n");
    }

    protected void buildBinding(StringBuilder sparql, Term term) {
        if (term.isFunction()) return;
        if (term instanceof LiteralTerm) {
            LiteralTerm literalTerm = (LiteralTerm)term;
            if (term.isBinding()) sparql.append("\"{{").append(literalTerm).append("}}\"^^"+literalTerm.getXSDType());
            else sparql.append("\"").append(term).append("\"");
        } else {
            if (term.isBinding()) sparql.append("<{{").append(term).append("}}> ");
            else sparql.append("<").append(term).append("> ");
        }
    }

//    public void bind(String var, String term) throws ASQException {
//        ArrayList terms = new ArrayList();
//        terms.add(new Term(null, term));
//        bind(var,terms);
//    }

    public void bind(String var, Collection<Term> terms) {
        sparql.append("BINDINGS ").append(var).append(" { (");
        for(Term term: terms) {
            buildBinding(sparql, term);
        }
        sparql.append(")}");
    }

    public String toString() {
        return sparql.toString();
    }
}
