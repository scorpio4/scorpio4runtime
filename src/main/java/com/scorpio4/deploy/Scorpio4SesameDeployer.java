package com.scorpio4.deploy;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 * 1st May 2014 - Licensed to Apscore
 */

import com.scorpio4.assets.SesameAssetRegister;
import com.scorpio4.oops.FactException;
import com.scorpio4.util.Identifiable;
import com.scorpio4.util.Steps;
import com.scorpio4.util.io.StreamCopy;
import com.scorpio4.vendor.sesame.util.SesameHelper;
import com.scorpio4.vocab.COMMONS;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Scorpio4SesameDeployer implements Identifiable {
    private final Logger log = LoggerFactory.getLogger(getClass());

	private boolean deployRDF = true;
	private boolean deployScripts = false;
	private boolean obliterate = false;
    private boolean isBooted = false;

	private ValueFactory values = null;

    private String identity;
	private RepositoryConnection connection = null;
	private Map<String, String> extension2type = new HashMap();
    private Map<String, Boolean> scriptTypes = new HashMap();

	// RDFS PRIMARIES//
	private URI provenanceContext = null, a = null, subClassOf = null, hasAsset = null;
    URI textType = null, rdfsLabel = null, mimeExtension = null;
    ScriptEngineManager sem = new ScriptEngineManager();

	long since = 0L;

    public Scorpio4SesameDeployer(String context, RepositoryConnection connection) throws FactException, RepositoryException {
        init(context,connection);
    }

	public void init(String context, RepositoryConnection connection) throws FactException {
        try {
            setConnection(connection);
	        //  (boolean verifyData, boolean stopAtFirstError, boolean preserveBNodeIDs, org.openrdf.rio.RDFParser.DatatypeHandling datatypeHandling) { /* compiled code */ }
	        connection.setParserConfig(new ParserConfig(false, true, false, RDFParser.DatatypeHandling.NORMALIZE));
            setProvenance(context);
            initScriptEngines();
        } catch (RepositoryException e) {
            throw new FactException("Repository error:"+e.getMessage(),e);
        }
    }

	protected void initScriptEngines() throws FactException {
		List<ScriptEngineFactory> scriptEngineFactories = sem.getEngineFactories();
		try {
			this.textType = values.createURI(COMMONS.MIME_PLAIN);
            this.rdfsLabel = values.createURI(COMMONS.RDFS+"label");
            this.mimeExtension = values.createURI(COMMONS.MIME_TYPE+"extension");
			for (int i=0;i<scriptEngineFactories.size();i++) {
				ScriptEngineFactory sef = scriptEngineFactories.get(i);
				List<String> extensions = sef.getExtensions();
				List mimes = sef.getMimeTypes();
				for(int m=0;m<mimes.size();m++) {
					URI mimeURI = values.createURI(COMMONS.MIME_TYPE+mimes.get(m));
					getConnection().add(mimeURI, subClassOf, textType, provenanceContext);
					getConnection().add(mimeURI, a, textType, provenanceContext);
					getConnection().add(mimeURI, rdfsLabel, values.createLiteral(sef.getLanguageName()), provenanceContext);
//					log.debug("\t Script: "+sef.getLanguageName()+" --> "+mimeURI.toString());
					for(int y=0;y<extensions.size();y++) {
						getConnection().add(mimeURI, mimeExtension, values.createLiteral(extensions.get(y)));
						extension2type.put(extensions.get(y), mimeURI.toString());
                        scriptTypes.put(extensions.get(y), new Boolean(true));
					}
				}
			}
			extension2type.put("sparql", COMMONS.MIME_TYPE+ "application/x-sparql-query");
			extension2type.put("rq", COMMONS.MIME_TYPE+ "application/x-sparql-query");
			extension2type.put("camel.xml",	COMMONS.MIME_TYPE+ "application/x-camel-route");
			extension2type.put("asq",	COMMONS.MIME_TYPE+ "application/x-asq");

			extension2type.put("html", COMMONS.MIME_TYPE+ "text/html");
			extension2type.put("xhtml", COMMONS.MIME_TYPE+ "application/xhtml+xml");
			extension2type.put("txt", COMMONS.MIME_TYPE+"plain/text");
            extension2type.put("json", COMMONS.MIME_TYPE+ "application/json");
            extension2type.put("css", COMMONS.MIME_TYPE+"plain/css");
            extension2type.put("xml", COMMONS.MIME_TYPE+"application/xml");
		} catch (RepositoryException e) {
			throw new FactException("Repository failed: "+e.getMessage(),e);
		}
	}

	public void clean() throws FactException {
        try {
            getConnection().clear(this.provenanceContext);
	        getConnection().commit();
	        SesameHelper.defaultNamespaces(getConnection());
        } catch (RepositoryException e) {
            throw new FactException("Obliterate failed: "+e.getMessage(), e);
        }
		if (isDeployingScripts()) initScriptEngines();
        isBooted = true;
        log.debug("Cleaned: "+this.provenanceContext);
	}

	public void deploy(File jarOrPath) throws FactException, IOException, RepositoryException {
		log.debug("Deploying: " + jarOrPath.getAbsolutePath());
		if (jarOrPath.isDirectory()) {
			deploy(jarOrPath, jarOrPath, true);
		} else {
            deploy(new JarFile(jarOrPath));
		}
	}

	public void deploy(URL url) throws FactException, IOException {
		URLConnection urlConnection = url.openConnection();
		if (urlConnection instanceof JarURLConnection) {
			JarURLConnection jarURL = (JarURLConnection) urlConnection;
			deploy(jarURL.getJarFile());
		} else if (url.getProtocol().equals("file")) {
			log.debug("TODO: Deploy File URI: "+url);
		} else {
			deploy(url.toExternalForm(), url.openStream());
		}
	}

	public void classpath(String resource) throws FactException, IOException {
		InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
		deploy(resource, resourceAsStream);
	}

	public void deploy(ZipFile zipFile) throws FactException, IOException {
        log.debug("Deploying Archive: "+zipFile.getName());
		try {
			Enumeration entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
                if(!entry.isDirectory()) deploy(zipFile, entry);
			}
		} catch (RepositoryException e) {
			throw new FactException("Repository failed: "+zipFile.getName(), e);
		} catch (RDFParseException e) {
			throw new FactException("N3 failed: "+zipFile.getName(), e);
		}
	}

	protected void deploy(ZipFile zipFile, ZipEntry entry ) throws FactException, IOException, RDFParseException, RepositoryException {
		deploy( "hacked:"+entry.getName(), zipFile.getInputStream(entry) );
	}

	protected int deploy(File home, File dir, boolean recurse) throws FactException, IOException {
		log.debug("Deploying Directory: "+dir.getAbsolutePath());
		int count = 0;
		File[] files = dir.listFiles();
		if(files==null) throw new IOException("Directory listing failed: "+dir.getAbsolutePath());
		for(int i=0;i<files.length;i++) {
			if (files[i].getName().startsWith(".")) {
				// ignore
			} else if (files[i].isFile()) {
				deploy(home, files[i]);
				count ++;
			}
		}

        for(int i=0;i<files.length;i++) {
            if (files[i].getName().startsWith(".")) {
                // ignore
            } else if (files[i].isDirectory() && recurse) {
                count += deploy(home, files[i], recurse);
            }
        }

		return count;
	}

	public void deploy(File home, File file) throws FactException, IOException {
		if (file.isDirectory()) {
			deploy(home, file, true);
			return;
		}
		if (since>0 && file.lastModified()<since) {
			log.trace("Not modified: "+file.getAbsolutePath());
			return;
		}

		FileInputStream inStream = new FileInputStream(file);
		deploy(Steps.localize("", home.getParentFile(), file), inStream);
		inStream.close();
	}

	public void deploy(String localPath, InputStream inStream) throws FactException, IOException {
        String extension = Steps.toExtension(localPath);
        if (extension==null) {
//          throw new FactException("Missing extension. Resource type unknown: "+localPath);
            log.debug("Missing Extension. Resource type unknown: "+localPath);
            return;
        }
		try {
			if (isDeployingRDF()) {
				if (extension.equals("n3")) {
					deployN3(localPath, inStream);
				} else if (extension.equals("ttl")) {
					deployTTL(localPath, inStream);
				} else if (extension.equals("rdf.xml")) {
					deployRDFXML(localPath, inStream);
				} else if (extension.equals("rdf")) {
					deployRDFXML(localPath, inStream);
				} else if (extension.equals("owl.xml")) {
					deployRDFXML(localPath, inStream);
				} else if (extension.equals("owl")) {
					deployRDFXML(localPath, inStream);
				} else if (extension.equals("nt")) {
					deployNT(localPath, inStream);
				} else if (extension.equals("nq")) {
					deployNQ(localPath, inStream);
				}
			}
			if (isDeployingScripts()) {
				if (scriptTypes.containsKey(extension) ) {
					script(localPath, inStream);
				} else if(extension2type.containsKey(extension) ) {
					asset(localPath, inStream);
				}
			}
		} catch (RDFParseException e) {
			throw new FactException("Corrupt RDF: "+e.getMessage()+"-> "+localPath+" -> "+e.getLineNumber()+":"+e.getColumnNumber(),e);
		} catch (RepositoryException e) {
			throw new FactException("Repository Error: "+e.getMessage()+" -> "+localPath,e);
		}
	}

	private void deployRIO(String localPath, InputStream inStream, RDFFormat format) throws RepositoryException, IOException, RDFParseException {
		getConnection().begin();
		String baseURN = toURN(localPath, "/");
		log.debug("Deploying: "+format+": "+baseURN+" in: "+provenanceContext+" ("+inStream.available()+")");
		getConnection().add(inStream, baseURN, format, provenanceContext);
		describeAsset(baseURN);
		getConnection().commit();
	}

	public void deployN3(String localPath, InputStream inStream) throws IOException, RDFParseException, RepositoryException {
        deployRIO(localPath, inStream, RDFFormat.N3);
    }

	public void deployNQ(String localPath, InputStream inStream) throws IOException, RDFParseException, RepositoryException {
		deployRIO(localPath, inStream, RDFFormat.NQUADS);
	}

	public void deployNT(String localPath, InputStream inStream) throws IOException, RDFParseException, RepositoryException {
		deployRIO(localPath, inStream, RDFFormat.NTRIPLES);
	}

	public void deployTTL(String localPath, InputStream inStream) throws IOException, RDFParseException, RepositoryException {
		deployRIO(localPath, inStream, RDFFormat.TURTLE);
	}

	public void deployRDFXML(String localPath, InputStream inStream) throws IOException, RDFParseException, RepositoryException {
		deployRIO(localPath, inStream, RDFFormat.RDFXML);
	}


    public void asset(String scriptName, InputStream inStream) throws IOException, RDFParseException, RepositoryException {
	    getConnection().begin();
	    String uri = toURN(scriptName, ":");
        log.debug("Asset: "+scriptName+" -> "+uri);
        URI scriptURI = values.createURI(uri);
        asset(scriptName, scriptURI, inStream);
	    getConnection().commit();
    }

    public void script(String scriptName, InputStream inStream) throws IOException, RDFParseException, RepositoryException {
	    getConnection().begin();
        String uri = toURN(scriptName, ".");
        log.debug("Script: "+scriptName+" -> "+uri);
        URI beaURI = values.createURI(uri);
        asset(scriptName, beaURI, inStream);
	    getConnection().commit();
    }

    protected void asset(String scriptName, URI scriptURI, InputStream inStream) throws IOException, RDFParseException, RepositoryException {
		String script = StreamCopy.toString(inStream);
		String extension = Steps.toExtension(scriptName);

		String scriptType = extension2type.get(extension);
		if (scriptType==null) return; // script extension no recognised ... silently ignore
		URI mimeType = values.createURI(scriptType);

		Literal scriptBody = values.createLiteral(script, mimeType);
        getConnection().add( scriptURI, hasAsset, scriptBody, provenanceContext);
        log.debug("Deployed: "+extension+" asset: " + scriptName+" -> "+scriptURI.toString() + " as " + scriptType);

//        getConnection().add( scriptURI , rdfsLabel, values.createLiteral(scriptName), provenanceContext );
        describeAsset(scriptURI.toString());

//      URI scriptTypeURI = values.createURI(VOCAB.IQ+PrettyString.camelCase(extension)+"Behaviour");
//      getConnection().add(scriptURI, values.createURI(VOCAB.A), scriptTypeURI, applicationContext);
//		getConnection().add(scriptURI, a , scriptClass, applicationContext);
	}

    private void describeAsset(String baseURN) throws RepositoryException {
        String label = new Steps(baseURN).local();
        getConnection().add( values.createURI(baseURN) , rdfsLabel, values.createLiteral(label), provenanceContext );
    }


	public ClassLoader loadClasses(URL zipFile) throws ClassNotFoundException, NoSuchMethodException {
		return loadClasses(zipFile, getClass().getClassLoader());
	}

	public ClassLoader loadClasses(URL zipFile, ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException {
		URL[] jars = new URL[1];
		jars[0] = zipFile;
		return new URLClassLoader (jars, classLoader);
	}

//	public void setFinder(String uri) throws ConfigException, FactException {
//        setProvenance(uri);
//		FactSource core = this.scorpio4.getFactSource(uri);
//		if (core==null || !(core instanceof SesameFactSource)) throw new FactException("Finder is not Sesame");
//		this.connection = ((SesameFactSource)core).getConnection();
//	}

	public RepositoryConnection getConnection() {
		return connection;
	}

 	public void setConnection(RepositoryConnection connection) throws RepositoryException {
		this.connection = connection;
		this.values = connection.getValueFactory();
		this.a = values.createURI(COMMONS.RDFS_TYPE);
		this.subClassOf = values.createURI(COMMONS.RDFS_SUBCLASS);
        this.hasAsset = values.createURI(SesameAssetRegister.HAS_ASSET);
	}

    public void setProvenance(String provenance) {
        this.identity=provenance;
        this.provenanceContext = values.createURI(provenance);
    }

	public boolean isDeployingRDF() {
		return deployRDF;
	}

	public void setDeployRDF(boolean deployRDF) {
		this.deployRDF = deployRDF;
	}

	public boolean isDeployingScripts() {
		return deployScripts;
	}

	public void setDeployScripts(boolean deployScripts) {
		this.deployScripts = deployScripts;
	}

    public String toURN(String localName, String sep) {
        Steps steps = new Steps(localName);
        steps = steps.stripExtension();
        return getIdentity()+steps.toString( 1,-1,sep);
    }

	public long getSince() {
		return since;
	}

	public void setSince(long since) {
		this.since = since;
	}

	public void setSince(Date since) {
		this.since = since.getTime();
	}

	@Override
    public String getIdentity() {
        return identity;
    }
}
