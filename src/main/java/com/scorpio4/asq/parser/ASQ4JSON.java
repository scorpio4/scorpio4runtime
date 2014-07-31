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
 * Scorpio4 (c) 2013-2014
 * Module: com.scorpio4.asq.core.parser
 * @author lee
 * Date  : 16/11/2013
 * Time  : 12:02 AM
 */
public class ASQ4JSON implements ASQParser {
    private static final Logger log = LoggerFactory.getLogger(ASQ4JSON.class);

	Gson gson = new Gson();
    ASQ asq = null;
    Properties ns = JarHelper.loadProperties("META-INF/rdf.namespace.props");

    public ASQ4JSON(File file) throws IOException, ASQException {
        this(new FileInputStream(file));
    }

    public ASQ4JSON(InputStream in) throws IOException, ASQException {
	    
	    String text = StreamCopy.toString(in);
        System.err.println("JSON: " + text);
	    Map asqMap = gson.fromJson(text, Map.class);
        parse(asqMap);
    }

    public ASQ4JSON(String json) throws IOException, ASQException {
        Map asqMap = gson.fromJson(json, Map.class);
        parse(asqMap);
    }

    public ASQ4JSON(Map asqMap) throws IOException, ASQException {
        parse(asqMap);
    }


    private void parse(Map asqMap) throws ASQException {
        String id = (String)asqMap.get("this");
        if (asqMap.containsKey("where")) {
            Object where = asqMap.get("where");
            if (where instanceof Collection) {
	            this.asq = makeWhere(id, (Collection)where);
            }
            else throw new ASQException("urn:scorpio4:asq:core:parser:oops:invalid-where#"+where.toString());
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
            if (optional==null) optional = false;
            asq.where(_this, _has, _that, _type, optional);

            log.trace("this:"+_this+", _that: "+_has+", that:"+_that);
        }
        return asq;
    }

    public ASQ getASQ() {
        return asq;
    }

}
