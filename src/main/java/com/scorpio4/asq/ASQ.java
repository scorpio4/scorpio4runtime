package com.scorpio4.asq;
/*
 *

 */


import com.scorpio4.asq.core.Pattern;
import com.scorpio4.asq.core.Term;
import com.scorpio4.oops.ASQException;
import com.scorpio4.util.Identifiable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * FactCore (c) 2013
 * Module: com.factcore.fact.query
 * User  : lee
 * Date  : 18/09/13
 * Time  : 9:25 PM
 */
public interface ASQ extends Identifiable {
    public static String NS = "urn:factcore:asq:";
    public static String THIS = NS+"this";
    public static String HAS = NS+"has";
    public static String THAT = NS+"that";


    public ASQ where(String _this, String _has, String _that) throws ASQException;
    public ASQ where(Pattern pattern);

    // syntax sugar
    public ASQ optional(String _this, String _has, String _that) throws ASQException;

    // syntax sugar
    public ASQ optional(String _this, String _has, String _that, String _type) throws ASQException;


    public ASQ filter(String _fn) throws ASQException;

    public void clear();
    public List<Pattern> getPatterns();

    public Term bind(Term term);
    public Map<String,Term> getBindings();

    public Set<Pattern> getFunctions();
}
