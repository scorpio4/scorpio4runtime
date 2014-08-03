package com.scorpio4.asq.core;

import com.scorpio4.oops.ASQException;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.asq.core
 * User  : lee
 * Date  : 3/08/2014
 * Time  : 1:01 PM
 */
public class RawTerm extends Term {

	public RawTerm(String term) throws ASQException {
		super(term);
	}

	public String toString() {
		return term;
	}
}
