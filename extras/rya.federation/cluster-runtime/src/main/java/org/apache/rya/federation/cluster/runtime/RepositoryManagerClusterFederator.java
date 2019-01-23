/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rya.federation.cluster.runtime;

import java.net.MalformedURLException;
import java.util.Collection;

import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.apache.rya.federation.cluster.sail.config.ClusterFederationFactory;
import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.http.config.HTTPRepositoryConfig;
import org.openrdf.repository.http.config.HTTPRepositoryFactory;
import org.openrdf.repository.http.config.HTTPRepositorySchema;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.sail.config.ProxyRepositoryFactory;
import org.openrdf.repository.sail.config.ProxyRepositorySchema;
import org.openrdf.repository.sail.config.SailRepositoryFactory;
import org.openrdf.repository.sail.config.SailRepositorySchema;
import org.openrdf.repository.sparql.config.SPARQLRepositoryConfig;
import org.openrdf.repository.sparql.config.SPARQLRepositoryFactory;
import org.openrdf.runtime.RepositoryManagerFederator;
import org.openrdf.sail.config.SailConfigSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vagrant
 */
public class RepositoryManagerClusterFederator extends RepositoryManagerFederator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryManagerClusterFederator.class);

    private final RepositoryManager manager;

    private final ValueFactory valueFactory;

    /**
     * Create an instance capable of federating "user repositories" within the
     * given {@link org.openrdf.repository.manager.RepositoryManager}.
     *
     * @param manager
     *        must manage the repositories to be added to new federations, and
     *        becomes the manager of any created federations
     */
    public RepositoryManagerClusterFederator(final RepositoryManager manager) {
        super(manager);
        this.manager = manager;
        this.valueFactory = manager.getSystemRepository().getValueFactory();
    }

    /**
     * Adds a new repository to the
     * {@link org.openrdf.repository.manager.RepositoryManager}, which is a
     * federation of the given repository id's, which must also refer to
     * repositories already managed by the
     * {@link org.openrdf.repository.manager.RepositoryManager}.
     *
     * @param fedID
     *        the desired identifier for the new cluster federation repository
     * @param description
     *        the desired description for the new cluster federation repository
     * @param members
     *        the identifiers of the repositories to cluster federate, which must already
     *        exist and be managed by the
     *        {@link org.openrdf.repository.manager.RepositoryManager}
     * @param readonly
     *        whether the cluster federation is read-only
     * @param distinct
     *        whether the cluster federation enforces distinct results from its members
     * @throws MalformedURLException
     *         if the {@link org.openrdf.repository.manager.RepositoryManager}
     *         has a malformed location
     * @throws OpenRDFException
     *         if a problem otherwise occurs while creating the federation
     */
    @Override
    public void addFed(final String fedID, final String description, final Collection<String> members, final boolean readonly,
            final boolean distinct)
        throws MalformedURLException, OpenRDFException
    {
        if (members.contains(fedID)) {
            throw new RepositoryConfigException(
                    "A cluster federation member may not have the same ID as the federation.");
        }
        final Graph graph = new LinkedHashModel();
        final BNode fedRepoNode = valueFactory.createBNode();
        LOGGER.debug("Cluster Federation repository root node: {}", fedRepoNode);
        addToGraph(graph, fedRepoNode, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
        addToGraph(graph, fedRepoNode, RepositoryConfigSchema.REPOSITORYID, valueFactory.createLiteral(fedID));
        addToGraph(graph, fedRepoNode, RDFS.LABEL, valueFactory.createLiteral(description));
        final RepositoryConnection con = manager.getSystemRepository().getConnection();
        try {
            addImplementation(members, graph, fedRepoNode, con, readonly, distinct);
        }
        finally {
            con.close();
        }
        final RepositoryConfig fedConfig = RepositoryConfig.create(graph, fedRepoNode);
        fedConfig.validate();
        manager.addRepositoryConfig(fedConfig);
    }

    private void addImplementation(final Collection<String> members, final Graph graph, final BNode fedRepoNode,
            final RepositoryConnection con, final boolean readonly, final boolean distinct)
        throws OpenRDFException, MalformedURLException
    {
        final BNode implRoot = valueFactory.createBNode();
        addToGraph(graph, fedRepoNode, RepositoryConfigSchema.REPOSITORYIMPL, implRoot);
        addToGraph(graph, implRoot, RepositoryConfigSchema.REPOSITORYTYPE,
                valueFactory.createLiteral(SailRepositoryFactory.REPOSITORY_TYPE));
        addSail(members, graph, implRoot, con, readonly, distinct);
    }

    private void addSail(final Collection<String> members, final Graph graph, final BNode implRoot, final RepositoryConnection con,
            final boolean readonly, final boolean distinct)
        throws OpenRDFException, MalformedURLException
    {
        final BNode sailRoot = valueFactory.createBNode();
        addToGraph(graph, implRoot, SailRepositorySchema.SAILIMPL, sailRoot);
        addToGraph(graph, sailRoot, SailConfigSchema.SAILTYPE,
                valueFactory.createLiteral(ClusterFederationFactory.SAIL_TYPE));
        addToGraph(graph, sailRoot, ClusterFederationConfig.READ_ONLY, valueFactory.createLiteral(readonly));
        addToGraph(graph, sailRoot, ClusterFederationConfig.DISTINCT, valueFactory.createLiteral(distinct));
        for (final String member : members) {
            addMember(graph, sailRoot, member, con);
        }
    }

    private void addMember(final Graph graph, final BNode sailRoot, final String identifier, final RepositoryConnection con)
        throws OpenRDFException, MalformedURLException
    {
        LOGGER.debug("Adding member: {}", identifier);
        final BNode memberNode = valueFactory.createBNode();
        addToGraph(graph, sailRoot, ClusterFederationConfig.MEMBER, memberNode);
        String memberRepoType = manager.getRepositoryConfig(identifier).getRepositoryImplConfig().getType();
        if (!(SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType) || HTTPRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)))
        {
            memberRepoType = ProxyRepositoryFactory.REPOSITORY_TYPE;
        }
        addToGraph(graph, memberNode, RepositoryConfigSchema.REPOSITORYTYPE,
                valueFactory.createLiteral(memberRepoType));
        addToGraph(graph, memberNode, getLocationPredicate(memberRepoType),
                getMemberLocator(identifier, con, memberRepoType));
        LOGGER.debug("Added member {}: ", identifier);
    }

    private URI getLocationPredicate(final String memberRepoType) {
        URI predicate;
        if (SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)) {
            predicate = SPARQLRepositoryConfig.QUERY_ENDPOINT;
        }
        else if (HTTPRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)) {
            predicate = HTTPRepositorySchema.REPOSITORYURL;
        }
        else {
            predicate = ProxyRepositorySchema.PROXIED_ID;
        }
        return predicate;
    }

    private Value getMemberLocator(final String identifier, final RepositoryConnection con, final String memberRepoType)
        throws MalformedURLException, RepositoryConfigException, OpenRDFException
    {
        Value locator;
        if (HTTPRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)) {
            locator = valueFactory.createURI(((HTTPRepositoryConfig)manager.getRepositoryConfig(identifier).getRepositoryImplConfig()).getURL());
        }
        else if (SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)) {
            locator = valueFactory.createURI(((SPARQLRepositoryConfig)manager.getRepositoryConfig(identifier).getRepositoryImplConfig()).getQueryEndpointUrl());
        }
        else {
            locator = valueFactory.createLiteral(identifier);
        }
        return locator;
    }

    private static void addToGraph(final Graph graph, final Resource subject, final URI predicate, final Value object) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(subject + " " + predicate + " " + object);
        }
        graph.add(subject, predicate, object);
    }
}