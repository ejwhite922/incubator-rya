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

import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailReadOnlyException;
import org.openrdf.sail.federation.AbstractClusterFederationConnection;

/**
 * Finishes the {@link AbstractClusterFederationConnection} by throwing
 * {@link SailReadOnlyException}s for all write operations except for setting
 * and clearing the internal namespaces.
 */
class ReadOnlyClusterConnection extends AbstractClusterFederationConnection {
    /**
     * Creates a new instance of {@link ReadOnlyClusterConnection}.
     * @param federation the {@link ClusterFederation} to connect to.
     * @param members the {@link List} of {@link RepositoryConnection}s that
     * are members of the cluster.
     */
    public ReadOnlyClusterConnection(final ClusterFederation federation, final List<RepositoryConnection> members) {
        super(federation, members);
    }

    @Override
    public void setNamespaceInternal(final String prefix, final String name) throws SailException {
    }

    @Override
    public void clearNamespacesInternal() throws SailException {
    }

    @Override
    public void removeNamespaceInternal(final String prefix)
        throws SailException
    {
        throw new SailReadOnlyException("");
    }

    @Override
    public void addStatementInternal(final Resource subj, final URI pred, final Value obj, final Resource... contexts)
        throws SailException
    {
        throw new SailReadOnlyException("");
    }

    @Override
    public void removeStatementsInternal(final Resource subj, final URI pred, final Value obj, final Resource... context)
        throws SailException
    {
        throw new SailReadOnlyException("");
    }

    @Override
    protected void clearInternal(final Resource... contexts) throws SailException {
        throw new SailReadOnlyException("");
    }

    @Override
    protected void commitInternal() throws SailException {
    }

    @Override
    protected void rollbackInternal() throws SailException {
    }

    @Override
    protected void startTransactionInternal() throws SailException {
    }
}