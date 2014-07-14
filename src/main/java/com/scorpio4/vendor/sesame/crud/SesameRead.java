package com.scorpio4.vendor.sesame.crud;

import com.scorpio4.oops.FactException;
import com.scorpio4.vendor.sesame.util.QueryTools;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * cuebic (c) 2013
 * Module: com.cuebic.sparql
 * User  : lee
 * Date  : 12/12/2013
 * Time  : 2:20 AM
 */
public class SesameRead {
    private static final Logger log = LoggerFactory.getLogger(SesameRead.class);
    String sparql = null;
	RepositoryConnection connection;

    public SesameRead(RepositoryConnection connection, String sparql) {
        this.connection=connection;
        this.sparql = sparql;
    }

	public SesameRead(SesameCRUD crud, String sparql) {
		this.connection=crud.getConnection();
		this.sparql = sparql;
	}

    public Collection<Map> execute() throws FactException {
        try {
            return QueryTools.toCollection(connection, sparql);
        } catch (IOException e) {
            throw new FactException(e.getMessage(),e);
        } catch (RepositoryException e) {
            throw new FactException(e.getMessage(),e);
        } catch (MalformedQueryException e) {
            throw new FactException(e.getMessage(),e);
        } catch (QueryEvaluationException e) {
            throw new FactException(e.getMessage(),e);
        }
    }


}
