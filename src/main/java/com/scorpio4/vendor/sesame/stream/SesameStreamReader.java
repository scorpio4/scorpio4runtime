package com.scorpio4.vendor.sesame.stream;
/*
 *   Copyright (c) 2009-2014 Lee Curtis.
 *
 *
 */

import com.scorpio4.fact.stream.FactStream;
import com.scorpio4.oops.FactException;
import com.scorpio4.vocab.COMMONS;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.Map;

/**
 * Scorpio4 (c) 2013-2014
 * @author lee
 * Date  : 22/11/2013
 * Time  : 5:06 PM
 */
public class SesameStreamReader {
    private RepositoryConnection repositoryConnection = null;
    private FactStream learn = null;

    public SesameStreamReader(Repository repository, FactStream learn) throws RepositoryException {
        this.repositoryConnection=repository.getConnection();
        setFactStream(learn);
    }

    public SesameStreamReader(RepositoryConnection repositoryConnection, FactStream learn) {
        this.repositoryConnection=repositoryConnection;
        setFactStream(learn);
    }

    public RepositoryConnection getConnection() {
        return this.repositoryConnection;
    }

    public FactStream getFactStream() {
        return learn;
    }

    public void setFactStream(FactStream learn) {
        this.learn = learn;
    }

    public void stream(String query, Map bindings) throws FactException {
        if (query==null) throw new FactException("urn:scorpio4:fact:finder:stream:oops:missing-query");
        try {
            FactStream stream = getFactStream();
            GraphQuery graphQuery = getConnection().prepareGraphQuery(QueryLanguage.SPARQL, query);
            GraphQueryResult result = graphQuery.evaluate();
            while (result.hasNext()) {
                Statement stmt = result.next();
                if (stmt.getObject() instanceof Literal) {
                    Literal value = (Literal)stmt.getObject();
                    String dataType = COMMONS.XSD+"string";
                    if (value.getDatatype()!=null) dataType = value.getDatatype().toString();
                    stream.fact(stmt.getSubject().toString(), stmt.getPredicate().toString(), value.getLabel(), dataType);
                } else {
                    stream.fact(stmt.getSubject().toString(), stmt.getPredicate().toString(), stmt.getObject().toString());
                }
            }
            result.close();
        } catch (RepositoryException e) {
            throw new FactException("Repository failed: "+e.getMessage(), e);
        } catch (QueryEvaluationException e) {
            throw new FactException("Query failed: "+e.getMessage()+"\n"+query, e);
        } catch (MalformedQueryException e) {
            throw new FactException("Query error: "+e.getMessage()+"\n"+query, e);
        }
    }

}
