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

import java.util.List;

import org.apache.rya.federation.cluster.sail.ClusterFederation;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;

/**
 * Echos all write operations to all members.
 */
public abstract class AbstractClusterEchoWriteConnection extends AbstractClusterFederationConnection {
    /**
     * Creates a new instance of {@link AbstractClusterEchoWriteConnection}.
     * @param federation
     * @param members
     */
    public AbstractClusterEchoWriteConnection(final ClusterFederation federation, final List<RepositoryConnection> members) {
        super(federation, members);
    }

    @Override
    public void startTransactionInternal() throws SailException {
        excute(new Procedure() {

            @Override
            public void run(final RepositoryConnection con)
                throws RepositoryException
            {
                con.begin();
            }
        });
    }

    @Override
    public void rollbackInternal()
        throws SailException
    {
        excute(new Procedure() {

            @Override
            public void run(final RepositoryConnection con)
                throws RepositoryException
            {
                con.rollback();
            }
        });
    }

    @Override
    public void commitInternal()
        throws SailException
    {
        excute(new Procedure() {

            @Override
            public void run(final RepositoryConnection con)
                throws RepositoryException
            {
                con.commit();
            }
        });
    }

    @Override
    public void setNamespaceInternal(final String prefix, final String name)
        throws SailException
    {
        excute(new Procedure() {

            @Override
            public void run(final RepositoryConnection con)
                throws RepositoryException
            {
                con.setNamespace(prefix, name);
            }
        });
    }

    @Override
    public void clearNamespacesInternal()
        throws SailException
    {
        excute(new Procedure() {

            @Override
            public void run(final RepositoryConnection con)
                throws RepositoryException
            {
                con.clearNamespaces();
            }
        });
    }

    @Override
    public void removeNamespaceInternal(final String prefix)
        throws SailException
    {
        excute(new Procedure() {

            @Override
            public void run(final RepositoryConnection con)
                throws RepositoryException
            {
                con.removeNamespace(prefix);
            }
        });
    }

    @Override
    public void removeStatementsInternal(final Resource subj, final URI pred, final Value obj,
            final Resource... contexts)
        throws SailException
    {
        excute(new Procedure() {

            @Override
            public void run(final RepositoryConnection con)
                throws RepositoryException
            {
                con.remove(subj, pred, obj, contexts);
            }
        });
    }
}