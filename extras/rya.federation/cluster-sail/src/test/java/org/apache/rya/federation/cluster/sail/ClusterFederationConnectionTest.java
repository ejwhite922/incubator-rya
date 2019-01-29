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
package org.apache.rya.federation.cluster.sail;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.rya.accumulo.AccumuloRdfConfiguration;
import org.apache.rya.accumulo.AccumuloRyaDAO;
import org.apache.rya.api.RdfCloudTripleStoreConfiguration;
import org.apache.rya.rdftriplestore.RdfCloudTripleStore;
import org.apache.rya.rdftriplestore.RyaSailRepository;
import org.apache.rya.rdftriplestore.inference.InferenceEngine;
import org.apache.rya.rdftriplestore.namespace.NamespaceManager;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.federation.Federation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests repository connections for cluster federation.
 */
public class ClusterFederationConnectionTest extends RepositoryConnectionTest {
    private static final Logger log = LoggerFactory.getLogger(ClusterFederationConnectionTest.class);

    @Override
    protected Repository createRepository() throws Exception {
        final Federation sail = new ClusterFederation();
        sail.addMember(createRyaSailRepository());
        sail.addMember(createRyaSailRepository());
        sail.addMember(createRyaSailRepository());
        return new SailRepository(sail);
    }

    private static RyaSailRepository createRyaSailRepository() throws Exception {
        final RdfCloudTripleStore<? extends RdfCloudTripleStoreConfiguration> store = new MockRdfCloudStore();
        final NamespaceManager nm = new NamespaceManager(store.getRyaDAO(), store.getConf());
        store.setNamespaceManager(nm);
        final RyaSailRepository repository = new RyaSailRepository(store);
        repository.initialize();
        return repository;
    }

    /**
     * Creates a mock Accumulo based {@link  RdfCloudTripleStore} for testing.
     */
    public static class MockRdfCloudStore extends RdfCloudTripleStore<AccumuloRdfConfiguration> {
        /**
         * Creates a new instance of {@link MockRdfCloudStore}.
         */
        public MockRdfCloudStore() {
            super();
            final Instance instance = new MockInstance();
            try {
                final AccumuloRdfConfiguration conf = new AccumuloRdfConfiguration();
                conf.setInfer(true);
                conf.setAccumuloUser("root");
                conf.setAccumuloPassword("root");
                setConf(conf);
                final Connector connector = instance.getConnector(conf.getAccumuloUser(), new PasswordToken(conf.getAccumuloPassword()));
                final AccumuloRyaDAO dao = new AccumuloRyaDAO();
                dao.setConf(conf);
                dao.setConnector(connector);
                setRyaDAO(dao);
                inferenceEngine = new InferenceEngine();
                inferenceEngine.setRyaDAO(dao);
                inferenceEngine.setRefreshGraphSchedule(5000); //every 5 sec
                inferenceEngine.setConf(conf);
                setInferenceEngine(inferenceEngine);
            } catch (final Exception e) {
                log.error("Failed to create MockRdfCloudStore", e);
            }
        }
    }
}