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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.apache.rya.rdftriplestore.RyaSailRepository;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.federation.Federation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Union multiple (possibly remote) Repositories into a single RDF store.
 */
public class ClusterFederation extends Federation {
    private static final Logger log = LoggerFactory.getLogger(ClusterFederation.class);

    private final List<Repository> members = new ArrayList<>();
    private final ClusterFederationConfig config;

    /**
     * Creates a new instance of {@link ClusterFederation}.
     */
    public ClusterFederation() {
        this(new ClusterFederationConfig());
    }

    /**
     * Creates a new instance of {@link ClusterFederation}.
     * @param config the {@link ClusterFederationConfig}. (not null)
     */
    public ClusterFederation(final ClusterFederationConfig config) {
        this.config = requireNonNull(config);
    }

    /**
     * @return the {@link ClusterFederationConfig}.
     */
    public ClusterFederationConfig getConfig() {
        return config;
    }

    @Override
    public void addMember(final Repository member) {
        members.add(member);
    }

    @Override
    public void initialize() throws SailException {
        for (final Repository member : members) {
            try {
                if (!(member instanceof RyaSailRepository)) {
                    throw new SailException("The Cluster Federation only supports Rya Sail Repositories");
                }
                member.initialize();
            } catch (final RepositoryException e) {
                throw new SailException(e);
            }
        }
    }

    @Override
    public void shutDown() throws SailException {
        for (final Repository member : members) {
            try {
                member.shutDown();
            } catch (final RepositoryException e) {
                throw new SailException(e);
            }
        }
        super.shutDown();
    }

    @Override
    public SailConnection getConnection() throws SailException {
        log.debug("cluster federation get connection");
        final List<RepositoryConnection> connections = new ArrayList<>(members.size());
        try {
            for (final Repository member : members) {
                connections.add(member.getConnection());
            }

            return isReadOnly() ? new ReadOnlyClusterConnection(this, connections)
                    : new WritableClusterConnection(this, connections);
        } catch (final RepositoryException e) {
            throw new SailException(e);
        }
    }
}