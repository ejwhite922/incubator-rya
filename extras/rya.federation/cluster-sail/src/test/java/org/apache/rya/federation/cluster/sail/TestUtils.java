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

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods and constant used for testing.
 */
public final class TestUtils {
    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private TestUtils() {
    }

    /**
     * Creates a new {@link BatchWriter} from the specified {@link Connector}
     * and {@code tableName}.
     * @param conn the {@link Connector}. (not null)
     * @param tableName the name of the Accumulo table. (not null)
     * @return the created {@link BatchWriter}.
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableNotFoundException
     */
    private static BatchWriter createWriter(final Connector conn, final String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        requireNonNull(conn);
        requireNonNull(tableName);
        final long memBuf = 1000000L; // bytes to store before sending a batch
        final long timeout = 1000L; // milliseconds to wait before sending
        final int numThreads = 10;
        final BatchWriterConfig config = new BatchWriterConfig();
        config.setMaxMemory(memBuf);
        config.setTimeout(timeout, TimeUnit.MILLISECONDS);
        config.setMaxWriteThreads(numThreads);
        final BatchWriter writer = conn.createBatchWriter(tableName, config);
        return writer;
    }

    /**
     *
     * @param uri
     * @param conn
     * @param tableName
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableNotFoundException
     */
    public static void addURI(final String uri, final Connector conn, final String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        addURIs(Collections.singleton(uri), conn, tableName);
    }

    /**
     *
     * @param uris
     * @param conn
     * @param tableName
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableNotFoundException
     */
    public static void addURIs(final Collection<String> uris, final Connector conn, final String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        BatchWriter writer = null;
        try {
            writer = createWriter(conn, tableName);

            for (final String uri : uris) {
                final Text rowID = new Text(uri);
                final Text colFam = new Text("   ");
                final Text colQual = new Text("   ");
//                final ColumnVisibility colVis = new ColumnVisibility("public");
                final long timestamp = System.currentTimeMillis();

                final Value tempValue = new Value("2".getBytes(StandardCharsets.UTF_8));

                final Mutation mutation = new Mutation(rowID);
                mutation.put(colFam, colQual, timestamp, tempValue);
                writer.addMutation(mutation);
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Queries the connection with the specified SPARQL and prints the binding
     * set results along with the total count.
     * @param conn the {@link RepositoryConnection}. (not null)
     * @param query the SPARQL query. (not null)
     * @return the number of binding sets returned from the query.
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws TupleQueryResultHandlerException
     * @throws QueryEvaluationException
     */
    public static int query(final RepositoryConnection conn, final String query) throws RepositoryException, MalformedQueryException, TupleQueryResultHandlerException, QueryEvaluationException {
        requireNonNull(conn);
        requireNonNull(query);
        final CountingResultHandler resultHandler = new CountingResultHandler();
        final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
        tupleQuery.evaluate(resultHandler);
        log.info("Result count : " + resultHandler.getCount());
        return resultHandler.getCount();
    }

    /**
     * Closes the {@link RepositoryConnection}.
     * @param conn the {@link RepositoryConnection}.
     */
    public static void closeConnection(final RepositoryConnection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (final RepositoryException e) {
                log.error("Error closing RepositoryConnection", e);
            }
        }
    }

    /**
     * Shuts the repository down, releasing any resources that it keeps hold of.
     * Once shut down, the repository can no longer be used until it is
     * re-initialized.
     * @param repo the {@link Repository} to close.
     */
    public static void closeRepository(final Repository repo) {
        if (repo != null && repo.isInitialized()) {
            try {
                repo.shutDown();
            } catch (final RepositoryException e) {
                log.error("Error shutting down Repository", e);
            }
        }
    }
}