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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.commons.lang.StringUtils;
import org.apache.rya.federation.cluster.sail.ClusterFederation;
import org.apache.rya.federation.cluster.sail.IntersectOverlapList;
import org.apache.rya.federation.cluster.sail.OverlapList;
import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
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

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractClusterFederationConnection.class);

    private final ClusterFederationConfig config;

    private final Set<String> includeSet = new HashSet<String>();

    private Iterator<Entry<Key, org.apache.accumulo.core.data.Value>> iterator;

    /**
     * Creates a new instance of {@link AbstractClusterFederationConnection}.
     * @param federation the {@link ClusterFederation} to connect to.
     * @param members the {@link List} of {@link RepositoryConnection}s that
     * are members of the cluster.
     */
    public AbstractClusterFederationConnection(final ClusterFederation federation,
            final List<RepositoryConnection> members) {
        super(federation, members);

        this.config = federation.getConfig();

        final String instanceName = StringUtils.defaultIfEmpty(config.getInstanceName(), "dev");
        final String tableName = StringUtils.defaultIfEmpty(config.getTableName(), "rya_overlap");
        final String zkServer = StringUtils.defaultIfEmpty(config.getZkServer(), "localhost:2181");
        final String username = StringUtils.defaultIfEmpty(config.getUsername(), "root");
        final String password = StringUtils.defaultIfEmpty(config.getPassword(), "root");

        final OverlapList at = new OverlapList(zkServer, instanceName);

        try {
            at.createConnection(username, password);
            at.selectTable(tableName);
            final Scanner sc = at.createScanner();
            iterator = sc.iterator();
        } catch (final TableNotFoundException e) {
            LOGGER.error("Failed to create overlap list iterator", e);
        } catch (final AccumuloException e) {
            LOGGER.error("Failed to create overlap list iterator", e);
        } catch (final AccumuloSecurityException e) {
            LOGGER.error("Failed to create overlap list iterator", e);
        } catch (final TableExistsException e) {
            LOGGER.error("Failed to create overlap list iterator", e);
        }

        while (iterator.hasNext()) {
            final Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator.next();
            includeSet.add(entry.getKey().getRow().toString());
        }
    }

    @Override
    public CloseableIteration<? extends Statement, SailException> getStatementsInternal(
            final Resource subj, final URI pred, final Value obj,
            final boolean includeInferred, final Resource... contexts)
            throws SailException {

        LOGGER.debug("cluster federation get statement internal");

        CloseableIteration<? extends Statement, SailException> cursor = super.getStatementsInternal(subj, pred, obj, includeInferred, contexts);
        if (cursor instanceof DistinctIteration) {
            cursor = new IntersectOverlapList<Statement, SailException>(cursor, includeSet);
        }

        return cursor;
    }
}