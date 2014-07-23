package com.scorpio4.asq;
/*
 */


import com.scorpio4.asq.core.Pattern;
import com.scorpio4.asq.core.Term;
import com.scorpio4.oops.ASQException;
import com.scorpio4.util.Identifiable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author lee
 * Date  : 18/09/13
 * Time  : 9:25 PM
 */
public interface ASQ extends Identifiable {
    public static String NS = "urn:scorpio4:asq:";
//    public static String THIS = NS+"this";
//    public static String HAS = NS+"has";
//    public static String THAT = NS+"that";


    public ASQ where(String _this, String _has, String _that) throws ASQException;
    public ASQ where(Pattern pattern);

    // syntax sugar
    public ASQ optional(String _this, String _has, String _that) throws ASQException;

    // syntax sugar
    public ASQ optional(String _this, String _has, String _that, String _type) throws ASQException;


    public ASQ filter(String _fn) throws ASQException;

    public void clear();
    public Collection<Pattern> getPatterns();

    public void bind(Term term);
    public Map<String,Term> getBindings();
	public Collection<Term> getSelects();
    public Set<Pattern> getFunctions();
}
