package com.scorpio4.asq.sparql;

import com.scorpio4.asq.ASQ;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.asq.sparql
 * User  : lee
 * Date  : 17/07/2014
 * Time  : 11:46 AM
 */
public class ConstructSPARQL extends SelectSPARQL {
	ASQ construct;
	ASQ where;
	/**
	 * CONSTRUCT query
	 *
	 * @param construct
	 * @param where*
	 */
	public ConstructSPARQL(ASQ construct, ASQ where) {
		this.construct=construct;
		this.where=where;
		build(construct, where, this.sparql);
	}

	public ASQ getConstruct() {
		return construct;
	}

	public ASQ getWhere() {
		return where;
	}

}
