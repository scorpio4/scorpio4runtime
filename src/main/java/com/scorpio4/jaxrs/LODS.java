package com.scorpio4.jaxrs;

import com.scorpio4.assets.Asset;
import com.scorpio4.assets.AssetHelper;
import com.scorpio4.assets.SesameAssetRegister;
import com.scorpio4.vendor.sesame.util.RDFScalars;
import com.scorpio4.vocab.COMMONS;
import org.apache.commons.httpclient.HttpStatus;
import org.openrdf.model.*;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.jaxrs
 * User  : lee
 * Date  : 19/07/2014
 * Time  : 11:28 PM
 */
public class LODS {
	static protected final Logger log = LoggerFactory.getLogger(LODS.class);

	@GET
	@Path("{internalURI}")
	@Produces(MediaType.TEXT_HTML)
	public Response getHTML(@Context final UriInfo info, @Context HttpServletRequest request) throws Exception {
		return doGet(info, request, "getHTML()", MediaType.TEXT_HTML);
	}

	@GET
	@Path("{internalURI}")
//	@Produces(ContentType.MIME_NT)
	public Response getNT(@Context final UriInfo info, @Context HttpServletRequest request) throws Exception {
		return doGet(info, request, "getNT()", ContentType.MIME_PLAIN);
	}

	@GET
	@Path("{internalURI}")
	@Produces(ContentType.MIME_XML)
	public Response getXML(@Context final UriInfo info, @Context HttpServletRequest request) throws Exception {
		return doGet(info, request, "getXML()", ContentType.MIME_PLAIN);
	}

	@GET
	@Path("{internalURI}")
	@Produces(ContentType.MIME_RDFXML)
	// @Profiled(tag = "getXML")
	public Response getRDFXML(@Context final UriInfo info, @Context HttpServletRequest request) throws Exception{
		return doGet(info, request, "getRDF/XML()", ContentType.MIME_RDFXML);
	}

	@GET
	@Path("{internalURI}")
	@Produces(ContentType.MIME_JSON_LD)
	public Response getJSONLD(@Context final UriInfo info, @Context HttpServletRequest request) throws Exception{
		return doGet(info, request, "getJSON-LD()", ContentType.MIME_JSON_LD);
	}

	private Response doGet(UriInfo info, HttpServletRequest request, String debug, String mimeType) throws Exception {
		// shorten URI mime-types
		if (mimeType.startsWith(COMMONS.MIME_TYPE)) mimeType = mimeType.substring(COMMONS.MIME_TYPE.length());
		log.debug(debug+" "+mimeType +" @ "+info.getRequestUri());

		// Separate LOD and internal URI at "#"
		String lodURI = null;
		String uri = null;
		// TODO: inject Repository
		Repository repository = null;
		if (repository == null) {
			log.error("Repository Not Found");
			return Response.status(HttpStatus.SC_FAILED_DEPENDENCY).build();
		}

		RepositoryConnection connection = repository.getConnection();
		ValueFactory vf = connection.getValueFactory();
		// resolve ASQ or simple Describe
		RDFScalars rdf = new RDFScalars(connection);
		boolean isASQ = rdf.isTypeOf(vf.createURI(uri), vf.createURI(COMMONS.CORE + "asq/Query"));
		GraphQueryResult results = null;

		// execute query
		if (isASQ) {
			SesameAssetRegister assetRegister = new SesameAssetRegister(connection);
			Asset asset = assetRegister.getAsset(uri, COMMONS.MIME_SPARQL);
			if (asset != null) {
				Map params = null;
				try {
					GraphQuery query = connection.prepareGraphQuery(QueryLanguage.SPARQL, AssetHelper.getAsset(asset, params).getContent().toString());
					results = query.evaluate();
				} catch(IOException e) {
					log.error("Asset Template Error: "+uri,e);
				}
			}
		}

		if (results == null) {
			GraphQuery query = connection.prepareGraphQuery(QueryLanguage.SPARQL, "DESCRIBE <" + uri + ">");
			results = query.evaluate();
		}

		Response response = null;
		if (!results.hasNext()) {
			response = Response.status(HttpStatus.SC_NOT_FOUND).build();

		} else {
			response = Response.ok(getLOD(lodURI, mimeType, results)).build();
		}
		try {
			results.close();
			connection.close();
		} catch(Exception e) {
			log.error("Sesame Connection Failed. LOD client OK.", e);
		}
		return response;
	}

	private StreamingOutput getLOD(final String lodURI, final String mimeType, final GraphQueryResult facts) {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException {
				RDFFormat dataFormat = RDFFormat.forMIMEType(mimeType);
				if (dataFormat==null) throw new IOException("Rio choked. Ask someone to register "+dataFormat);
				RDFWriter writer = Rio.createWriter(dataFormat, output);
				try {
					writer.startRDF();
					while(facts.hasNext()) {
						writer.handleStatement(toLOD(lodURI, facts.next()));
					}
					writer.endRDF();
				} catch (RDFHandlerException e) {
					e.printStackTrace();
				} catch (QueryEvaluationException e) {
					e.printStackTrace();
				}
			}

		};
	}

	private Statement toLOD(String lodURI, Statement next) {
		// re-write statement into LOD space
		return new StatementImpl((Resource) toLOD(lodURI, next.getSubject()), (URI) toLOD(lodURI, next.getPredicate()), toLOD(lodURI, next.getObject()));
	}

	private Value toLOD(String lodURI, Value v) {
		if (v instanceof Literal) return v;
		if (v instanceof BNode) return v;
		if (v.stringValue().startsWith("http:")||v.stringValue().startsWith("https:")) {
			return v;
		}
		// re-write URI into LOD space
		return new URIImpl(lodURI+"#"+v.stringValue());
	}
}
