package com.scorpio4.asq.sparql;

import com.scorpio4.asq.ASQ;
import com.scorpio4.asq.core.BasicASQ;
import com.scorpio4.asq.core.Pattern;
import com.scorpio4.oops.ASQException;

import java.util.Collection;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.asq.sparql
 * @author lee
 * Date  : 17/07/2014
 * Time  : 11:46 AM
 */
public class ConstructSPARQL extends SelectSPARQL {
	ASQ construct;
	ASQ where;

	/**
	 * CONSTRUCT query used that uses the same pattern
	 * as the head and the body.
	 * Useful for copying matched triples to another graph
	 *
	 * @param asq*
	 */
	public ConstructSPARQL(ASQ asq) throws ASQException {
		this(flattenAndAssert(asq),asq);
	}

	private static ASQ flattenAndAssert(ASQ asq) throws ASQException {
		BasicASQ basicASQ = new BasicASQ(asq.getIdentity());
		Collection<Pattern> patterns = asq.getPatterns();
		flattenAndAssert(basicASQ, patterns);
		return basicASQ;
	}

	private static void flattenAndAssert(ASQ asq, Collection<Pattern> patterns) throws ASQException {
		for(Pattern pattern: patterns) {
			if (!pattern.isFilter() && !pattern.isFunctional()) {
				asq.where(new Pattern(pattern));
			}
			flattenAndAssert(asq, pattern.getNested());
		}
	}

	/**
	 * CONSTRUCT query that constructs the output graph
	 * constraint the constraint patterns are matched.
	 * Useful for inferencing one graph from another,
	 * translating one DSL to another
	 *
	 * @param construct
	 * @param constraint*
	 */
	public ConstructSPARQL(ASQ construct, ASQ constraint) {
		this.construct=construct;
		this.where=constraint;
		build(construct, constraint, this.sparql);
	}

	public ASQ getConstruct() {
		return construct;
	}

	public ASQ getWhere() {
		return where;
	}

}
