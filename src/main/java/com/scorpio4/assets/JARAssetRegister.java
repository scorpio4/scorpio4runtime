package com.scorpio4.assets;

import com.scorpio4.vocab.COMMON;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * scorpio4 (c) 2013-2014
 * Module: com.scorpio4
 * @author lee
 * Date  : 1/05/2014
 * Time  : 3:08 PM
 */
public class JARAssetRegister extends BaseAssetRegister {
	private static final Logger log = LoggerFactory.getLogger(JARAssetRegister.class);
    Map mimeToExtension = new HashMap();

	private ClassLoader classLoader;
    private static String separatorURN= "urn:";

	public JARAssetRegister() {
        init();
		setClassLoader(Thread.currentThread().getContextClassLoader());
		log.info("JAR Class Loader: "+getClassLoader());
	}

	public JARAssetRegister(ClassLoader classLoader) {
        init();
		setClassLoader(classLoader);
		log.trace("Class Loader: "+getClassLoader());
	}


    private void init() {
	    Object put = mimeToExtension.put(COMMON.MIME_GROOVY, ".groovy");
	    put = mimeToExtension.put(COMMON.MIME_JAVASCRIPT, ".js");
	    put = mimeToExtension.put(COMMON.MIME_JSON, ".json");
	    put = mimeToExtension.put(COMMON.MIME_SPARQL, ".sparql");
	    put = mimeToExtension.put(COMMON.MIME_SQL, ".sql");
	    put = mimeToExtension.put(COMMON.MIME_PLAIN, ".txt");
	    put = mimeToExtension.put(COMMON.MIME_XHTML, ".xhtml");
	    put = mimeToExtension.put(COMMON.MIME_HTML, ".html");
	    put = mimeToExtension.put(COMMON.MIME_XML, ".xml");
	    put = mimeToExtension.put(COMMON.MIME_CSV, ".csv");
    }

    /**
     * Get the correct path to generate the file that will be used as property file
     * In Windows the File.separator present issues and it was replaced by the default /.
     * @param path
     * @return
     */
	public static String fromURNToPath(String path) {
        log.trace("fromURNToPath: "+path);
		if (path.startsWith(separatorURN)){
            path = path.substring(separatorURN.length(),path.length());
        }
		return path.replaceAll(":", "/"/*File.separator*/);
	}

    @Override
    public String getString(String uri, String mimeType) throws IOException {
	    if (uri.startsWith("classpath:")) {
		    uri = uri.substring("classpath:".length());
	    }
        String scriptPath = fromURNToPath(uri);
        log.debug("getScriptJAR: "+uri+" from: "+classLoader);
        if (mimeType!=null&&!mimeType.equals("") && uri.indexOf(".")<0) {
            String extn = (String) mimeToExtension.get(mimeType);
            scriptPath+=extn;
        }
        InputStream resourceAsStream = classLoader.getResourceAsStream(scriptPath);
        if (resourceAsStream==null) throw new IOException("JAR asset not found: "+scriptPath);
        return toString(resourceAsStream);
    }

    public Properties getProperties(String uri) throws IOException {
		String scriptPath = fromURNToPath(uri);
		log.debug("getProperties: "+uri+" from: "+classLoader);
		InputStream resourceAsStream = classLoader.getResourceAsStream(scriptPath);
		if (resourceAsStream==null) throw new IOException("JAR properties not found: "+scriptPath);
		Properties properties = new Properties();
		properties.load(resourceAsStream);
		return properties;
	}

	public static String toString(InputStream inputStream) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, "UTF-8");
		return writer.toString();
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

}
