package com.scorpio4.jaxrs;

import org.apache.commons.httpclient.HttpStatus;
import org.openrdf.model.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.jaxrs
 * User  : lee
 * Date  : 19/07/2014
 * Time  : 11:28 PM
 */
public class LODS {

	@GET
	@Path("{id}")
	@Produces(ContentType.MIME_HTML)
	public Response getHTML(@Context final UriInfo info, @Context HttpServletRequest request) {
		return getResponse(info, request, "getHTML()", ContentType.MIME_HTML);
	}

	@GET
	@Path("{id}")
//	@Produces(ContentType.MIME_NT)
	public Response getNT(@Context final UriInfo info, @Context HttpServletRequest request) {
		return getResponse(info, request, "getNT()", ContentType.MIME_PLAIN);
	}

	@GET
	@Path("{id}")
	@Produces(ContentType.MIME_RDFXML)
	// @Profiled(tag = "getXML")
	public Response getXML(@Context final UriInfo info, @Context HttpServletRequest request) {
		return getResponse(info, request, "getXML()", ContentType.MIME_RDFXML);
	}





	private StreamingOutput getOutput(final String mediaType, final Collection<Statement> triples) {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException {
//				PrintWriter pw = new PrintWriter(output);
//				formatter.print(triples, pw);
//				pw.close();
			}
		};
	}

//	private StreamingOutput getOutput(final String mediaType, final List<Statement> triples) {
//		return getOutput(mediaType, triples.iterator());
//	}

	private Response getResponse(UriInfo info, HttpServletRequest request, String log_head, String content_type) {
		Collection<Statement> facts = null;
		if (facts.isEmpty()) {
			return Response.status(HttpStatus.SC_NOT_FOUND).build();

		} else {
			return Response.ok(getOutput(content_type, facts)).build();
		}

//
//		try {
//
//			String url_strg = requestURI(info);
//
//			Store store = ((Store) _context.getAttribute(ConfigParams.STORE));
//			Iterator<Statement> data = store.describe(ValueFactoryImpl.getInstance().createURI(url_strg), false, Integer.MAX_VALUE, Integer.MAX_VALUE);
//
//			if (!data.hasNext()) {
//				return Response.status(HttpStatus.SC_NOT_FOUND).build();
//
//			} else {
//				return Response.ok(getOutput(content_type, data)).build();
//			}
//		} catch (final FactException exception) {
//			log.error(MessageCatalog._00025_CUMULUS_SYSTEM_INTERNAL_FAILURE, exception);
//			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity("====== StoreException ====== \n" + exception.getMessage()).build();
//		}
	}

}
