package com.scorpio4.vendor.camel.flo;

import com.github.fge.uritemplate.URITemplateParseException;
import com.scorpio4.assets.Asset;
import com.scorpio4.assets.AssetRegister;
import com.scorpio4.assets.SesameAssetRegister;
import com.scorpio4.iq.exec.Scripting;
import com.scorpio4.oops.IQException;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.util.Identifiable;
import com.scorpio4.vendor.sesame.util.RDFCollections;
import com.scorpio4.vendor.util.IRITemplate;
import com.scorpio4.vocab.COMMONS;
import org.apache.camel.*;
import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.language.LanguageExpression;
import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.semarglproject.vocab.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Scorpio (c) 2014
 * Module: com.scorpio4.vendor.camel
 * @author lee
 * Date  : 21/06/2014
 * Time  : 5:58 PM
 */
public class RDFCamelPlanner extends FLOSupport implements Identifiable {
	static protected final Logger log = LoggerFactory.getLogger(RDFCamelPlanner.class);

	int count = 0;
	RepositoryConnection connection;
	AssetRegister assetRegister = null;
	String vocabURI = COMMONS.ACTIVE_FLO;
	URI TO = null;
	private ExecutionEnvironment engine;

	public void setEngine(ExecutionEnvironment engine) {
		this.engine = engine;
	}

	boolean useInferencing = false;

	public RDFCamelPlanner(CamelContext camelContext, ExecutionEnvironment engine) throws Exception {
		super(camelContext);
		init(engine);
	}

	private void init(ExecutionEnvironment engine) throws RepositoryException {
		this.engine=engine;
		this.connection = engine.getRepository().getConnection();
		assetRegister = new SesameAssetRegister(connection);
		TO = connection.getValueFactory().createURI(getVocabURI() + "to");
	}

	public void setVocabURI(String vocabURI) {
		this.vocabURI = vocabURI;
	}

	public String getVocabURI() {
		return vocabURI;
	}

	public int plan() throws Exception {
		return plan(getIdentity());
	}

	public int plan(final String routeURI) throws Exception {
		final ValueFactory vf = connection.getValueFactory();
		String FROM = toVocabURI("from");
		log.debug("Plan Route: "+routeURI+" -> "+FROM);
		RepositoryResult<Statement> froms = connection.getStatements(vf.createURI(routeURI), vf.createURI(FROM), null, useInferencing);
		Map seen = new HashMap();
		while(froms.hasNext()){
			Statement next = froms.next();
			final Value _routeID = next.getSubject();
			final Value _from = next.getObject();
			if (seen.containsKey(_from.stringValue())) {
				break;
			}
			seen.put(_from.stringValue(), true);
			RouteBuilder routing = new org.apache.camel.builder.RouteBuilder() {
				@Override
				public void configure() throws Exception {
//					errorHandler(deadLetterChannel("mock:error"));
					String from = _from.stringValue();
					log.debug("Configure Route ("+_routeID+") -> "+from);
					if (IRITemplate.isTemplated(from)) {
						IRITemplate toTemplate = new IRITemplate(from, engine.getConfig());
						log.debug("FROM Template: "+from+" -> "+toTemplate);
						from = toTemplate.toString();
					}

					log.debug("\troute -> "+from+" @ "+_routeID);
					RouteDefinition trying = from(from);
					trying.setId(getIdentity()+"@"+from);
//					tried.doTry();
					log.debug("\ttrying: " + trying);
					ProcessorDefinition ended = fromRoute(this, trying, (Resource) _from, null);
					log.debug("\tended: " + ended+" <- "+trying);

					if (ended.getOutputs().isEmpty()) {
						log.warn("NO ENDPOINT: "+_from);
						ended.to("log:missing-endpoint");
					}

//					tried.doCatch(Exception.class).to("log:catch:" + routeURI).
//					doFinally().to("log:finally:" + routeURI).
					ended.end();
					log.debug("\tfinally:" + ended);
					count++;
				}
			};
			context.addRoutes(routing);
		}
		return count;
	}

	protected ProcessorDefinition fromRoute(RouteBuilder routeBuilder, final ProcessorDefinition fromRoute, final Resource from, String defaultAction) throws RepositoryException, CamelException, ClassNotFoundException, IOException, URITemplateParseException, IQException {
		log.debug("flo @ "+from);

		RepositoryResult<Statement> plannedRoutes = connection.getStatements(from, null, null, useInferencing);
		ProcessorDefinition _from = fromRoute;
		while(plannedRoutes.hasNext()) {
			Statement next = plannedRoutes.next();
			String action = next.getPredicate().stringValue();
			Value to = next.getObject();
			// look for known predicates
			if (action.startsWith(getVocabURI()) ) {
				_from = toRoutes(routeBuilder, _from, to, action.substring(getVocabURI().length()));
			} else if ( defaultAction!=null && action.equals(RDF.FIRST)) {
				_from = toRoutes(routeBuilder, _from, to, defaultAction);
			} else {
				log.debug("Ignored Predicate: " + action);
			}
		}
		return _from;
	}

	private ProcessorDefinition toRoutes(RouteBuilder routeBuilder, ProcessorDefinition from, Value _to, String action) throws RepositoryException, CamelException, ClassNotFoundException, IOException, URITemplateParseException, IQException {
		if ( isTemplated(_to.stringValue()) ) {
//			from.recipientList().
		}
		if (_to instanceof Resource) {
			RDFCollections collection = new RDFCollections(this.connection);

			// singleton
			if (!collection.isList(_to)) {
				return toRoute(routeBuilder, from, _to, action);
			}

			// process as a pipeline
			Collection<Value> pipeline = collection.getList((Resource)_to);
			log.debug("\tTO pipeline: "+_to+" x "+pipeline.size()+" -> "+ Arrays.toString(pipeline.toArray()));
			for(Value to: pipeline) {
				from = toRoute(routeBuilder, from, to, action);
			}
			return from;

		}
		return tryAction(routeBuilder, from, _to, action);
	}

	protected ProcessorDefinition toRoute(RouteBuilder routeBuilder, ProcessorDefinition from, Value _to, String action) throws RepositoryException, ClassNotFoundException, URITemplateParseException, CamelException, IOException, IQException {
		if (_to instanceof BNode) {
			// blank nodes are skipped ... recursively resolve
			return fromRoute(routeBuilder, from, (BNode) _to, action);
		}
		if (_to instanceof URI) {
			// if reference a bean ...
			String to = _to.stringValue();
			Object bean = context.getRegistry().lookupByName(to);
			if (bean!=null) {
				log.debug("\tTO bean: "+_to);
				return from.bean(bean);
			}

			log.debug("flo:"+action+"\t"+_to);
			// is the route just an internal placeholder?
			if (action!=null && action.equals("to") && (to.startsWith("urn:") || isSimpleIORoute(_to) ) )  {
				return fromRoute(routeBuilder, from, (URI) _to, action);
			}
			return fromRoute(routeBuilder, from.to(to), (URI) _to, action);
		}
		if (_to instanceof Literal) {
			log.debug("\tTO script: "+_to);
			return from.process( toScriptProcessor(_to, "simple") );
		}
		return from;
	}

	protected ProcessorDefinition tryAction(RouteBuilder routeBuilder, ProcessorDefinition from, Value _to, String action) throws RepositoryException, CamelException, ClassNotFoundException, IOException, URITemplateParseException, IQException {
		String to = _to.stringValue();
		if (_to instanceof URI && IRITemplate.isTemplated(to)) {
			IRITemplate toTemplate = new IRITemplate(to, engine.getConfig());
			to = toTemplate.toString();
		}

		if (to.equals(toVocabURI("stop"))) return from.stop();
		if (to.equals(toVocabURI("end"))) return from.end();
		if (to.equals(toVocabURI("endChoice"))) return from.endChoice();

		if (action.equals("to")) {
			// handle HTTP(s) and FILE as direct: routes
			from = toRoute(routeBuilder, from, normalizeTo(_to), action);
		} else	if (action.equals("io") || action.equals("do")) {
			// HTTP(s) and FILE produce messages act as true Camel routes
			from = toRoute(routeBuilder, from, _to, action);
		} else if (action.equals("bean") ) {
			log.info("to-bean: "+to);
			from = from.beanRef(to);
		} else if (action.startsWith("setBody") || action.startsWith("body")) {
			from = from.setBody(toExpression(connection, _to, action));
		} else if (action.startsWith("setFaultBody") || action.startsWith("fault") ) {
			from = from.setFaultBody(toExpression(connection, _to, action));
		} else if (action.startsWith("aggregate")) {
			from = from.aggregate(toExpression(connection, _to, action));
		} else if (action.startsWith("validate")) {
			from = from.validate(toExpression(connection, _to, action));
		} else if (action.equals("multicast")) {
			from = from.multicast().to(to);
		} else if (action.equals("log")) {
			from = from.log(LoggingLevel.DEBUG, to);
		} else if (action.equals("loadbalance")) {
			from = from.loadBalance().to(to);
		} else if (action.equals("route")) {
			throw new IQException("Bean-based RouteBuilder Not Implemented.");
		} else if (action.startsWith("script")) {
			from = from.process( toScriptProcessor(_to, action) );
		} else if (action.startsWith("split")) {
			if (_to instanceof Literal && _to.stringValue().equals("")) {
				from = from.split(routeBuilder.body()).shareUnitOfWork();
			} else {
				from = from.split(toExpression(connection, _to, action));
			}
		} else if (action.startsWith("marshal:")) {
			from = doMarshal(action, from);
		} else if (action.startsWith("unmarshal:")) {
			from = doUnmarshal(action, from);
		} else if (action.startsWith("filter")) {
			from = from.filter(toPredicate(connection, _to, action));
		} else if (action.startsWith("sort")) {
			from = from.sort(toExpression(connection, _to, action));
		} else if (action.equals("threads")) {
			from = from.threads().to(to);
		} else if (action.equals("onCompletion")) {
			from = from.onCompletion().to(to);
		} else if (action.equals("transacted")) {
			from = from.transacted().to(to);
		} else if (action.equals("parallelProcessing")) {
			from.multicast().parallelProcessing().aggregationStrategyRef(to);
		} else if (action.startsWith("resequence")) {
			from = from.resequence(toExpression(connection, _to, action));
		} else if (action.equals("convertBodyTo") && action.equals("as")) {
			// "as" is a FLO synonym for convertBodyTo
			from = doConvertBodyTo(from, to);
		} else if (action.startsWith("recipientList")) {
			from = from.recipientList(toExpression(connection, _to, action));
		} else if (action.startsWith("loop")) {
			from = from.loop(toExpression(connection, _to, action));
		} else if (action.startsWith("delay")) {
			from = from.delay(toExpression(connection, _to, action));
		} else if (action.equals("choice")) {
			from = doChoice(routeBuilder,  from.choice(), action, _to);
		} else if (action.equals("when") && from instanceof ChoiceDefinition) {
			from = doChoice(routeBuilder, (ChoiceDefinition) from, action, _to);
		} else if (action.equals("otherwise") && from instanceof ChoiceDefinition) {
			ChoiceDefinition choice = (ChoiceDefinition)from;
			from = choice.otherwise().to(to);
		} else if (action.equals("if")) {
			from = doChoice(routeBuilder, from.choice(), action, _to);
		} else if (action.startsWith("transform")) {
			from = from.transform(toExpression(connection, _to, action));
		} else {
			log.warn("??? action: " + action);
			return from;
		}

		return from;
	}

	private Value normalizeTo(Value to) {
		if (to instanceof URI) {
			// preserve concepts of linked data ... don't trigger Camel http: scheme
			if (isSimpleIORoute(to)) {
				ValueFactory vf = connection.getValueFactory();
				return vf.createURI("direct:"+to.stringValue());
			}
		}
		return to;
	}

	private boolean isSimpleIORoute(Value to) {
		return to.stringValue().startsWith("http:") || to.stringValue().startsWith("https:") || to.stringValue().startsWith("file:");
	}

	private boolean isTemplated(String s) {
		return (s.contains("{") && s.contains("}"));
	}

	protected ProcessorDefinition doChoice(RouteBuilder routeBuilder, ChoiceDefinition from, String action, Value to) throws RepositoryException, CamelException, ClassNotFoundException, IOException, URITemplateParseException, IQException {
		if (to instanceof BNode) {
			return fromRoute(routeBuilder, from, (Resource) to, action).endChoice();
		} else if (to instanceof Resource) {
			ChoiceDefinition when = from.when(toPredicate(connection, to, action));
			ProcessorDefinition end = fromRoute(routeBuilder, when, (Resource) to, action);
			return end.endChoice();
		} else if (to instanceof Literal) {
			return from.when(new LanguageExpression(getActionLanguage(action), to.stringValue())).endChoice();
		}
		// we should never get here
		return from;
	}

	private ProcessorDefinition doConvertBodyTo(ProcessorDefinition from, String to) throws ClassNotFoundException {
		if (to.startsWith("bean:")) {
			String type = to.substring(5);
			from = from.convertBodyTo(Class.forName(type));
		} else if (to.startsWith("classpath:")) {
			String type = to.substring(9);
			from = from.convertBodyTo(Class.forName(type));
		}
		return from.to(to);
	}

	private ProcessorDefinition doUnmarshal(String action, ProcessorDefinition from) {
		String type = action.substring("unmarshal:".length());
		DataFormatClause unmarshal = from.unmarshal();
		log.info("Marshall: "+type+" -> "+unmarshal);
		switch(type) {
			case "csv": from = unmarshal.csv(); break;
			case "avro": from = unmarshal.avro(); break;
			case "base64": from = unmarshal.base64(); break;
			case "castor": from = unmarshal.castor(); break;
			case "gzip": from = unmarshal.gzip(); break;
			case "jaxb": from = unmarshal.jaxb(); break;
			case "hl7": from = unmarshal.hl7(); break;
			case "jibx": from = unmarshal.jibx(); break;
			case "protobuf": from = unmarshal.protobuf(); break;
			case "rss": from = unmarshal.rss(); break;
			case "secureXML": from = unmarshal.secureXML(); break;
			case "serialization": from = unmarshal.serialization(); break;
			case "soapjaxb": from = unmarshal.soapjaxb(); break;
			case "string": from = unmarshal.string(); break;
			case "syslog": from = unmarshal.syslog(); break;
			case "tidyMarkup": from = unmarshal.tidyMarkup(); break;
			case "xmlBeans": from = unmarshal.xmlBeans(); break;
			case "xmljson": from = unmarshal.xmljson(); break;
			case "zip": from = unmarshal.zip(); break;
			case "zipFile": from = unmarshal.zipFile(); break;
		}
		return from;
	}

	private ProcessorDefinition doMarshal(String action, ProcessorDefinition from) {
		String type = action.substring("marshal:".length());
		DataFormatClause marshal = from.marshal();
		log.info("Marshall: "+type+" -> "+marshal);
		switch(type) {
			case "csv": from = marshal.csv(); break;
			case "avro": from = marshal.avro(); break;
			case "base64": from = marshal.base64(); break;
			case "castor": from = marshal.castor(); break;
			case "gzip": from = marshal.gzip(); break;
			case "jaxb": from = marshal.jaxb(); break;
			case "hl7": from = marshal.hl7(); break;
			case "jibx": from = marshal.jibx(); break;
			case "protobuf": from = marshal.protobuf(); break;
			case "rss": from = marshal.rss(); break;
			case "secureXML": from = marshal.secureXML(); break;
			case "serialization": from = marshal.serialization(); break;
			case "soapjaxb": from = marshal.soapjaxb(); break;
			case "string": from = marshal.string(); break;
			case "syslog": from = marshal.syslog(); break;
			case "tidyMarkup": from = marshal.tidyMarkup(); break;
			case "xmlBeans": from = marshal.xmlBeans(); break;
			case "xmljson": from = marshal.xmljson(); break;
			case "zip": from = marshal.zip(); break;
			case "zipFile": from = marshal.zipFile(); break;
		}
		return from;
	}

	// *** Make it So ***

	private Predicate toPredicate(RepositoryConnection connection, Value to, String action) {
		if (to instanceof Literal) {
			return new LanguageExpression(getActionLanguage(action), to.stringValue());
		}
		return new RDFBasedPredicate(connection, to.stringValue());
	}

	private Processor toScriptProcessor(final Value to, final String language) throws IOException {
		Asset asset = null;
		if (to instanceof Resource) {
			asset = assetRegister.getAsset(to.stringValue(), null);
		} else if (to instanceof Literal) {
			final Literal literal = (Literal)to;
			URI dataType = literal.getDatatype();
			log.debug("\tScript: "+language+"\n"+literal);
			if (dataType==null) {
				// default to Simple Expression, if data-type is missing
				return new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						Expression expression = new LanguageExpression(getActionLanguage(language), literal.stringValue()).createExpression(context);
						Object evaluate = expression.evaluate(exchange, Object.class);
						exchange.getIn().setBody(evaluate);
					}
				};
			}
			asset = new Asset(to.stringValue(), to.stringValue(), dataType.stringValue());
		}

		final Scripting scripting = new Scripting();
		return new ScriptProcessor(scripting, asset);
	}

	private String getActionLanguage(String language) {
		int ix = language.indexOf(":");
		if (ix<0) return "simple";
		return language.substring(ix+1);
	}

	Expression toExpression(RepositoryConnection connection, Value to, String action) throws RepositoryException, CamelException, ClassNotFoundException {
		if (to instanceof Literal) {
			return new LanguageExpression(getActionLanguage(action), to.stringValue());
		}
		return new RDFBasedExpression(connection,to.stringValue());
	}

	private String toVocabURI(String localName) {
		return getVocabURI()+localName;
	}

	private String getLocalNameOrNull(String globalName) {
		if (globalName.startsWith(getVocabURI())) return globalName.substring(getVocabURI().length());
		return null;
	}

	public boolean isUseInferencing() {
		return useInferencing;
	}

	public void setUseInferencing(boolean useInferencing) {
		this.useInferencing = useInferencing;
	}

	public ExecutionEnvironment getEngine() {
		return engine;
	}

	@Override
	public String getIdentity() {
		return engine.getIdentity();
	}
}
class RDFBasedPredicate implements Predicate {
	RepositoryConnection connection;
	Expression refExpression;

	public RDFBasedPredicate(RepositoryConnection connection, String to) {
		this.connection=connection;
		refExpression = ExpressionBuilder.refExpression(to);
		RDFCamelPlanner.log.debug("Predicate: "+to+" -> "+refExpression);
	}

	@Override
	public boolean matches(Exchange exchange) {
		RDFCamelPlanner.log.debug("refExpression: "+refExpression);
		if (refExpression==null) return false;
		Predicate predicate = PredicateBuilder.toPredicate(refExpression);
		RDFCamelPlanner.log.debug("Predicate: "+predicate);
		return predicate==null?false:predicate.matches(exchange);
	}

}

class RDFBasedExpression implements Expression {
	RepositoryConnection connection;
	Expression expression = null;

	public RDFBasedExpression(RepositoryConnection connection, String to) {
		this.connection=connection;
		expression = ExpressionBuilder.refExpression(to);
	}

	@Override
	public <T> T evaluate(Exchange exchange, Class<T> tClass) {
		RDFCamelPlanner.log.debug("Expression: "+expression+" -> "+expression.getClass()+" -> "+tClass);
		if (expression==null) return null;
		return expression.evaluate(exchange,tClass);
	}
}

class ScriptProcessor implements Processor {
	Scripting scripting;
	Asset asset;

	ScriptProcessor(final Scripting scripting, final Asset asset) {
		this.scripting=scripting;
		this.asset=asset;
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		Future body =  scripting.execute(asset, exchange.getIn().getHeaders());
		exchange.getOut().setBody(body.get());
		exchange.getOut().setHeaders(exchange.getIn().getHeaders());
		exchange.getOut().setAttachments(exchange.getIn().getAttachments());

	}
}