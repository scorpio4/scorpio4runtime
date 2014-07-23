package com.scorpio4.fact.stream;
/*
 *
 */

import com.scorpio4.oops.FactException;

/**
 * FactCore (c) 2013
 * Module: com.factcore.fact.stream
 * User  : root
 * Date  : 11/14/13
 * Time  : 11:47 AM
 */
public class SinkStream implements FactStream {

    public SinkStream() {

    }
    @Override
    public void fact(String s, String p, Object o) throws FactException {
        // NO-OP
    }

    @Override
    public void fact(String s, String p, Object o, String xsdType) throws FactException {
        // NO-OP
    }

    @Override
    public String getIdentity() {
        return "urn:factcore:fact:stream:Sink";
    }
}
