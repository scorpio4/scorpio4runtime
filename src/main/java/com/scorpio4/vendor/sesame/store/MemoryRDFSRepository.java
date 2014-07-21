package com.scorpio4.vendor.sesame.store;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * scorpio4-oss (c) 2014
 * Module: com.scorpio4.vendor.sesame.store
 * @author lee
 * Date  : 3/07/2014
 * Time  : 11:41 AM
 *
 *
 */
public class MemoryRDFSRepository extends SailRepository {
	private static final Logger log = LoggerFactory.getLogger(MemoryRDFSRepository.class);

	public MemoryRDFSRepository() throws RepositoryException {
		super(new ForwardChainingRDFSInferencer( new MemoryStore() ));
		initialize();
		log.debug("MemoryRDFSRepository initialized");
	}

}
