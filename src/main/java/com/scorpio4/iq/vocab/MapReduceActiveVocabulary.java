package com.scorpio4.iq.vocab;

import com.scorpio4.fact.FactSpace;
import com.scorpio4.oops.IQException;
import com.scorpio4.runtime.ExecutionEnvironment;
import com.scorpio4.vendor.sesame.crud.SesameCRUD;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lee on 6/09/2014.
 */
public class MapReduceActiveVocabulary extends AbstractActiveVocabulary {

	private Map<String, MapReduceExecutor> executorMap = new HashMap();

	public MapReduceActiveVocabulary(String vocabURI, ExecutionEnvironment engine, boolean useInferencing) throws Exception {
		super(vocabURI, engine, useInferencing);
	}

	public void start() throws Exception {
		super.start();
		FactSpace factSpace = new FactSpace(engine.getIdentity(), engine.getRepository());

		SesameCRUD crud = new SesameCRUD(factSpace);
		Collection<Map> prototypes = crud.read("self/mapreduce/jobs", engine.getConfig());
		register(prototypes);
		log.debug("Started "+prototypes.size()+" Mapper/Reducers");
	}

	public int register(Collection<Map> jobs) {
		int c = 0;
		for(Map job:jobs) {
			String jobURI = (String)job.get("this");
			String mapperBeanURI = (String)job.get("map");
			String reduceBeanURI = (String)job.get("reduce");
			String inputBeanURI = (String)job.get("from");
			String outputBeanURI = (String)job.get("to");
			String typeBeanURI = (String)job.get("as");
			try {
				register(jobURI, mapperBeanURI, reduceBeanURI, inputBeanURI, outputBeanURI, typeBeanURI);
				log.debug("Mapper/Reducer: "+jobURI);
			} catch(Exception e) {
				log.error("Map/Reduce ERROR: "+jobURI+" -> "+e.getMessage());
			}
		}
		return c;
	}

	public MapReduceExecutor register(String jobURI, String mapperBeanURI, String reduceBeanURI, String inputBeanURI, String outputBeanURI, String typeBeanURI) throws ClassNotFoundException {
		Class mapper = beanUriToClass(mapperBeanURI);
		Class reducer = beanUriToClass(reduceBeanURI);
		Class input = beanUriToClass(inputBeanURI);
		Class output = beanUriToClass(outputBeanURI);
		Class type = beanUriToClass(typeBeanURI);
		MapReduceExecutor executor = executorMap.put(jobURI, new MapReduceExecutor(inputBeanURI, outputBeanURI, mapper, reducer, input, output, type));
		return executor;
	}

	protected Class beanUriToClass(String uri) throws ClassNotFoundException {
		if (uri.startsWith("bean:")) {
			return Class.forName(uri.substring(5));
		}
		try {
			return Class.forName(uri);
		} catch (ClassNotFoundException cnfe) {
			log.error(uri, cnfe.getMessage());
			throw new ClassNotFoundException(uri);
		}
	}

	@Override
	public Object activate(String jobURI, Object body) throws IQException {
		try {
			MapReduceExecutor executor = executorMap.get(jobURI);
			Configuration conf = new Configuration();
			Job job = executor.job(jobURI, conf);
			FileInputFormat.addInputPath(job, new Path(executor.getInputBeanURI()));
			FileOutputFormat.setOutputPath(job, new Path(executor.getOutputBeanURI()));
			boolean completion = job.waitForCompletion(false);
			log.debug("Mapped/Reduced: " + completion+" @ "+jobURI);
			return job;
		} catch (IOException e) {
			log.debug("Failed Map/Reduce: " + e.getMessage()+" @ "+jobURI);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			log.debug("Failed Map/Reduce: " + e.getMessage()+" @ "+jobURI);
			e.printStackTrace();
		} catch (InterruptedException e) {
			log.debug("Failed Map/Reduce: " + e.getMessage()+" @ "+jobURI);
			e.printStackTrace();
		}
		return null;
	}

}

class MapReduceExecutor {
	Class<Mapper> mapper;
	Class<Reducer> reducer;
	Class<InputFormat> input;
	Class<OutputFormat> output;

	String inputBeanURI;
	String outputBeanURI;
	Class type;

	public MapReduceExecutor(String inputBeanURI, String outputBeanURI, Class<Mapper> mapper, Class<Reducer> reducer, Class<InputFormat> input, Class<OutputFormat> output, Class type) {
		this.inputBeanURI=inputBeanURI;
		this.outputBeanURI=outputBeanURI;
		this.mapper=mapper;
		this.reducer=reducer;
		this.input=input;
		this.output=output;
		this.type=type;
	}

	public String getInputBeanURI() {
		return inputBeanURI;
	}

	public String getOutputBeanURI() {
		return outputBeanURI;
	}

	public Job job(String resourceIn, Configuration conf) throws IOException, ClassNotFoundException, InterruptedException {
		Job job = new Job(conf, resourceIn);
		job.setJarByClass(mapper);
		job.setMapperClass(mapper);
		job.setReducerClass(reducer);

		job.setOutputKeyClass(type);
		job.setOutputValueClass(type);
		job.setMapOutputKeyClass(type);
		job.setMapOutputValueClass(type);

		job.setInputFormatClass(input);
		job.setOutputFormatClass(output);
		return job;
	}
}