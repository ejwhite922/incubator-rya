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
import java.util.Random;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;
import org.openrdf.sail.federation.AbstractClusterEchoWriteConnection;

/**
 * Statements are only written to a single member. Statements that have a
 * {@link IllegalStatementException} throw when added to a member are tried
 * against all other members until it is accepted. If no members accept a
 * statement the original exception is re-thrown.
 */
class WritableClusterConnection extends AbstractClusterEchoWriteConnection {
    private int addIndex;

    /**
     * Creates a new instance of {@link WritableClusterConnection}.
     * @param federation the {@link ClusterFederation} to connect to.
     * @param members the {@link List} of {@link RepositoryConnection}s that
     * are members of the cluster.
     * @throws SailException
     */
    public WritableClusterConnection(final ClusterFederation federation,
            final List<RepositoryConnection> members) throws SailException {
        super(federation, members);

        final int size = members.size();
        final int rnd = (new Random().nextInt() % size + size) % size;
        // use round-robin to distribute the load
        for (int i = rnd, n = rnd + size; i < n; i++) {
            try {
                if (members.get(i % size).getRepository().isWritable()) {
                    addIndex = i % size;
                }
            } catch (final RepositoryException e) {
                throw new SailException(e);
            }
        }
    }

    @Override
    public void addStatementInternal(final Resource subj, final URI pred, final Value obj, final Resource... contexts)
        throws SailException
    {
        add(members.get(addIndex), subj, pred, obj, contexts);
    }

    private void add(final RepositoryConnection member, final Resource subj, final URI pred, final Value obj, final Resource... contexts)
        throws SailException
    {
        try {
            member.add(subj, pred, obj, contexts);
        } catch (final RepositoryException e) {
            throw new SailException(e);
        }
    }

    @Override
    protected void clearInternal(final Resource... contexts) throws SailException {
        removeStatementsInternal(null, null, null, contexts);
    }
}