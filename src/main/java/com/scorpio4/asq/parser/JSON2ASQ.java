package com.scorpio4.asq.parser;
/*
 *

 */

import com.google.gson.Gson;
import com.scorpio4.asq.ASQ;
import com.scorpio4.asq.ASQParser;
import com.scorpio4.asq.core.BasicASQ;
import com.scorpio4.oops.ASQException;
import com.scorpio4.util.io.JarHelper;
import com.scorpio4.util.io.StreamCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * FactCore (c) 2013
 * Module: com.factcore.fact.domain.parser
 * User  : lee
 * Date  : 16/11/2013
 * Time  : 12:02 AM
 */
public class JSON2ASQ implements ASQParser {
    private static final Logger log = LoggerFactory.getLogger(JSON2ASQ.class);

	Gson gson = new Gson();
    ASQ asq = null;
    Properties ns = JarHelper.loadProperties("META-INF/rdf.namespace.props");

    public JSON2ASQ(File file) throws IOException, ASQException {
        this(new FileInputStream(file));
    }

    public JSON2ASQ(InputStream in) throws IOException, ASQException {
	    
	    String text = StreamCopy.toString(in);
        System.err.println("JSON: " + text);
	    Map asqMap = gson.fromJson(text, Map.class);
        parse(asqMap);
    }

    public JSON2ASQ(String json) throws IOException, ASQException {
        Map asqMap = gson.fromJson(json, Map.class);
        parse(asqMap);
    }

    public JSON2ASQ(Map asqMap) throws IOException, ASQException {
        parse(asqMap);
    }


    private void parse(Map asqMap) throws ASQException {
        String id = (String)asqMap.get("this");
        if (asqMap.containsKey("where")) {
            Object where = asqMap.get("where");
            if (where instanceof Collection) this.asq = makeWhere(id, (Collection)where);
            else throw new ASQException("urn:factcore:fact:domain:parser:oops:invalid-where#"+where.toString());
        }
    }

    private BasicASQ makeWhere(String id, Collection<Map <String,Object>> where) throws ASQException {
        BasicASQ asq = new BasicASQ(id);
        for(Map term: where) {
            log.trace("where:"+term);
            String _this = asq.cname((String) term.get("this"));
            String _has = asq.cname((String) term.get("has"));
            String _that = asq.cname((String) term.get("that"));
            String _type = asq.cname((String) term.get("type"));
            Boolean optional = (Boolean)term.get("optional");
            if (optional==null) optional = Boolean.valueOf(false);
            asq.where(_this, _has, _that, _type, optional.booleanValue());

            log.trace("this:"+_this+", _that: "+_has+", that:"+_that);
        }
        return asq;
    }

    public ASQ getASQ() {
        return asq;
    }

}
