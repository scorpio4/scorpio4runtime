package com.scorpio4.vendor.sesame.stream;
/*
 *   Fact:Core - CONFIDENTIAL
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */

import com.scorpio4.fact.FactSpace;
import com.scorpio4.fact.stream.FactStream;
import com.scorpio4.oops.FactException;
import com.scorpio4.vocab.COMMONS;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * FactCore (c) 2013
 * Module: com.factcore.vendor.sparql
 * @author lee
 * Date  : 26/10/2013
 * Time  : 8:54 PM
 */
public class SesameStreamWriter implements FactStream {
    ValueFactory vf = null;
    RepositoryConnection conn = null;
    URI context = null;
    boolean absolute = true;

	public SesameStreamWriter(FactSpace factSpace) throws RepositoryException {
		this(factSpace.getConnection(), factSpace.getIdentity());
	}

    public SesameStreamWriter(RepositoryConnection conn, String context_uri) throws RepositoryException {
        this.conn = conn;
        this.vf = this.conn.getValueFactory();
        this.context = vf.createURI(context_uri);
    }

    public void clear() throws FactException {
        try {
            this.conn.clear(this.context);
        } catch (RepositoryException e) {
            throw new FactException("urn:factcore:finder:sparql:oops:clear#"+e.getMessage(),e);
        }
    }

    @Override
    public void fact(String s, String p, Object o) throws FactException {
        if (s==null||p==null||o==null) return;
        try {
            if (absolute) this.conn.remove(vf.createURI(s), vf.createURI(p), null, context);
            this.conn.add(vf.createURI(s), vf.createURI(p), vf.createURI(o.toString()), context );
        } catch (RepositoryException e) {
            throw new FactException("urn:factcore:finder:sparql:oops:add-fact#"+e.getMessage(),e);
        }
    }

    @Override
    public void fact(String s, String p, Object o, String xsdType) throws FactException {
        if (s==null||p==null||o==null||xsdType==null) return;
        try {
            if (xsdType.indexOf(":")<1) xsdType = COMMONS.XSD+xsdType;
            if (absolute) this.conn.remove(vf.createURI(s), vf.createURI(p), null, context);
            this.conn.add(vf.createURI(s), vf.createURI(p), vf.createLiteral(o.toString(), vf.createURI(xsdType)), context );
        } catch (RepositoryException e) {
            throw new FactException("urn:factcore:finder:sparql:oops:add-literal#"+e.getMessage(),e);
        }
    }

    public void done() throws FactException {
        try {
            this.conn.commit();
        } catch (RepositoryException e) {
            throw new FactException("urn:factcore:finder:sparql:oops:not-done#"+e.getMessage(),e);
        }
    }

    @Override
    public String getIdentity() {
        return context.toString();
    }
}
