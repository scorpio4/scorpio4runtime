package com.scorpio4.vendor.sesame;
/*
 *   Scorpio4 - Apache Licensed
 *   Copyright (c) 2009-2014 Lee Curtis, All Rights Reserved.
 *
 *
 */

import com.scorpio4.util.Identifiable;
import com.scorpio4.util.string.PrettyString;
import org.openrdf.repository.DelegatingRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.*;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.SystemRepository;
import org.openrdf.repository.sail.config.RepositoryResolverClient;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.federation.Federation;
import org.openrdf.sail.inferencer.fc.config.ForwardChainingRDFSInferencerConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;
import org.openrdf.sail.nativerdf.config.NativeStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;

/**
 * Scorpio4 (c) 2010-2013
 * @author lee
 * Date: 18/01/13
 * Time: 7:07 PM
 * <p/>
 * This code does something useful
 */
public class RepositoryManager extends LocalRepositoryManager implements Identifiable {
	private static final Logger log = LoggerFactory.getLogger(RepositoryManager.class);

	public RepositoryManager(File home) throws RepositoryException, MalformedURLException {
		super(home);
		initialize();
		SystemRepository systemRepository = createSystemRepository();

		log.debug("SystemRepository() " + systemRepository+" @ "+systemRepository.isInitialized()+" & "+ systemRepository.isWritable());
	}

	public Repository newRepository(String repositoryId) throws RepositoryException, RepositoryConfigException {
		log.debug("newRepository() "+repositoryId);
		RepositoryConfig repositoryConfig = newDiskRepositoryConfig(repositoryId, true);
		addRepositoryConfig(repositoryConfig);
		return createRepository(repositoryId, repositoryConfig.getRepositoryImplConfig());
	}

	public Repository createRepository(String repositoryId) throws RepositoryException, RepositoryConfigException {
		log.debug("createRepository() "+repositoryId);
		RepositoryConfig repConfig = newMemoryRepositoryConfig(repositoryId, true, false);
		addRepositoryConfig(repConfig);
		return createRepository(repositoryId, repConfig.getRepositoryImplConfig());
	}

//	public Repository createRepository(boolean infer) {
//		if (infer)
//			return new SailRepository(new ForwardChainingRDFSInferencer( new MemoryStore() ) );
//		else
//			return new SailRepository( new MemoryStore() );
//	}

	public Federation getFederation(String repositoryId) {
		return getFederation(repositoryId, null);
	}

	public Federation getFederation(String repositoryId, Collection<Repository> repositories) {
		log.debug("getFederation() "+repositoryId+" -> "+repositories);
		Federation federation = new Federation();
		federation.setDistinct(true);
		if (repositories==null || repositories.isEmpty()) return federation;

		for(Repository repo: repositories) {
			federation.addMember(repo);
		}
		return federation;
	}

	public RepositoryConfig newMemoryRepositoryConfig(String repositoryId, boolean persist, boolean infer) {
		SailImplConfig backendConfig = new MemoryStoreConfig(persist);
		if (infer) {
			backendConfig = new ForwardChainingRDFSInferencerConfig(backendConfig);
		}
		log.debug("newMemoryRepository() "+repositoryId+" -> "+backendConfig);
		SailRepositoryConfig repositoryTypeSpec = new SailRepositoryConfig(backendConfig);
		return new RepositoryConfig(repositoryId, repositoryTypeSpec);
	}

	public RepositoryConfig newDiskRepositoryConfig(String repositoryId, boolean infer) {
		SailImplConfig backendConfig = new NativeStoreConfig();
		if (infer) {
			backendConfig = new ForwardChainingRDFSInferencerConfig(backendConfig);
		}
		log.debug("newDiskRepository() "+repositoryId+" -> "+backendConfig);
		SailRepositoryConfig repositoryTypeSpec = new SailRepositoryConfig(backendConfig);
		return new RepositoryConfig(repositoryId, repositoryTypeSpec);
	}

	// copied from Sesame's LocalRepositoryManager.java
	protected Repository createRepository(String repositoryId, RepositoryImplConfig config) throws RepositoryConfigException, RepositoryException {
		RepositoryFactory factory = RepositoryRegistry.getInstance().get(config.getType());
		if (factory == null) {
			throw new RepositoryConfigException("Unsupported repository type: " + config.getType());
		}
		if (factory instanceof RepositoryResolverClient) {
			((RepositoryResolverClient)factory).setRepositoryResolver(this);
		}
		File repositoryDir = getRepositoryDir(PrettyString.sanitize(repositoryId));
		Repository repository = factory.getRepository(config);
		repository.setDataDir(repositoryDir);
		repository.initialize();
		if (config instanceof DelegatingRepositoryImplConfig) {
			RepositoryImplConfig delegateConfig = ((DelegatingRepositoryImplConfig)config).getDelegate();
			Repository delegate = createRepository(repositoryId, delegateConfig);
			try {
				((DelegatingRepository)repository).setDelegate(delegate);
			}
			catch (ClassCastException e) {
				throw new RepositoryConfigException( "Delegate specified for repository that is not a DelegatingRepository: " + delegate.getClass(), e);
			}
		}
		return repository;
	}

	@Override
	public String getIdentity() {
		try {
			return getLocation().toExternalForm();
		} catch (MalformedURLException e) {
			return "file://"+getBaseDir().getAbsolutePath();
		}
	}
}
