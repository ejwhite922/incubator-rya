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
package org.apache.rya.federation.cluster.sail.config;

import org.apache.rya.federation.cluster.sail.ClusterFederation;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.sail.config.RepositoryResolver;
import org.openrdf.repository.sail.config.RepositoryResolverClient;
import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;


/**
 * Creates a cluster federation based on its configuration.
 */
public class ClusterFederationFactory implements SailFactory, RepositoryResolverClient {
    public static final String SAIL_TYPE = "openrdf:ClusterFederation";
    private RepositoryResolver resolver;

    @Override
    public String getSailType() {
        return SAIL_TYPE;
    }

    @Override
    public SailImplConfig getConfig() {
        return new ClusterFederationConfig();
    }

    @Override
    public Sail getSail(final SailImplConfig config) throws SailConfigException {
        if (!SAIL_TYPE.equals(config.getType())) {
            throw new SailConfigException("Invalid Sail type: "
                    + config.getType());
        }
        assert config instanceof ClusterFederationConfig;
        final ClusterFederationConfig cfg = (ClusterFederationConfig) config;
        final ClusterFederation sail = new ClusterFederation(cfg);
        for (final RepositoryImplConfig member : cfg.getMembers()) {
            final RepositoryFactory factory = RepositoryRegistry.getInstance().get(
                    member.getType());
            if (factory == null) {
                throw new SailConfigException("Unsupported repository type: "
                        + config.getType());
            }
            if (factory instanceof RepositoryResolverClient) {
                ((RepositoryResolverClient) factory)
                        .setRepositoryResolver(resolver);
            }
            try {
                sail.addMember(factory.getRepository(member));
            } catch (final RepositoryConfigException e) {
                throw new SailConfigException(e);
            }
        }
        sail.setLocalPropertySpace(cfg.getLocalPropertySpace());
        sail.setDistinct(cfg.isDistinct());
        sail.setReadOnly(cfg.isReadOnly());
        return sail;
    }

    @Override
    public void setRepositoryResolver(final RepositoryResolver resolver) {
        this.resolver = resolver;
    }
}