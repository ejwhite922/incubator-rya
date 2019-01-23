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

/**
 * Utility methods and constant used for testing.
 */
public final class TestUtils {
    /**
     * Private constructor to prevent instantiation.
     */
    private TestUtils() {
    }

    /**
     *
     * @param conn
     * @param tableName
     * @return
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableNotFoundException
     */
    private static BatchWriter createWriter(final Connector conn, final String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
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
}