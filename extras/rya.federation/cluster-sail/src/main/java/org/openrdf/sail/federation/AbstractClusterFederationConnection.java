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
package org.openrdf.sail.federation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.rya.federation.cluster.sail.ClusterFederation;
import org.apache.rya.federation.cluster.sail.IntersectOverlapList;
import org.apache.rya.federation.cluster.sail.OverlapList;
import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.apache.rya.federation.cluster.sail.exception.OverlapListException;
import org.apache.rya.federation.cluster.sail.overlap.OverlapListFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.DistinctIteration;

/**
 * Unions the results from multiple {@link RepositoryConnection} into one
 * {@link SailConnection}.
 */
public abstract class AbstractClusterFederationConnection extends AbstractFederationConnection {
    private static final Logger log = LoggerFactory.getLogger(AbstractClusterFederationConnection.class);

    private final ClusterFederationConfig config;

    private final Set<String> includeSet = new HashSet<>();

    private final OverlapList overlapList;

    /**
     * Creates a new instance of {@link AbstractClusterFederationConnection}.
     * @param clusterFederation the {@link ClusterFederation} to connect to.
     * @param members the {@link List} of {@link RepositoryConnection}s that
     * are members of the cluster.
     * @throws SailException
     */
    public AbstractClusterFederationConnection(final ClusterFederation clusterFederation,
            final List<RepositoryConnection> members) throws SailException {
        super(clusterFederation, members);

        this.config = clusterFederation.getConfig();

        try {
            this.overlapList = OverlapListFactory.getInstance().createOverlapList(config);
        } catch (final OverlapListException e) {
            throw new SailException("Unable to create overlap list", e);
        }
    }

    @Override
    public CloseableIteration<? extends Statement, SailException> getStatementsInternal(
            final Resource subj, final URI pred, final Value obj,
            final boolean includeInferred, final Resource... contexts)
            throws SailException {

        log.debug("cluster federation get statement internal");

        try {
            overlapList.setup();
            includeSet.addAll(overlapList.getOverlaps());
        } catch (final OverlapListException e) {
            throw new SailException("Failed to find overlaps", e);
        }

//        CloseableIteration<? extends Statement, SailException> cursor = union(new Function<Statement>() {
//
//            public CloseableIteration<? extends Statement, RepositoryException> call(
//                    RepositoryConnection member) throws RepositoryException {
//                return member.getStatements(subj, pred, obj, includeInferred,
//                        contexts);
//            }
//        });

        CloseableIteration<? extends Statement, SailException> cursor = super.getStatementsInternal(subj, pred, obj, includeInferred, contexts);
        if (cursor instanceof DistinctIteration) {
            cursor = new IntersectOverlapList<>(cursor, includeSet);
        }

        return cursor;
    }
}