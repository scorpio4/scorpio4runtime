package com.scorpio4.fact.stream;
/*
 *
 */


import com.scorpio4.oops.FactException;

import java.util.Map;

/**
 * Scorpio4 (c) 2013-2014
 * User  : lee
 * Date  : 12/09/13
 * Time  : 11:53 PM
 *
 * Experimental FactStreamer
 *
 */
public interface FactStreamer {

    /* Read a stream */
    public FactStream pull(String raw_query, Map map, FactStream stream) throws FactException;

    /* Write a stream */
    public FactStream push(String context_uri)  throws FactException;

    public void done(FactStream stream)  throws FactException;

}
