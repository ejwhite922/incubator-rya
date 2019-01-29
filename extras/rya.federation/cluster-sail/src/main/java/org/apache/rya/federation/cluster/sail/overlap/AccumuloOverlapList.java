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
package org.apache.rya.federation.cluster.sail.overlap;

import static java.util.Objects.requireNonNull;
import static org.apache.rya.api.RdfCloudTripleStoreConstants.EMPTY_TEXT;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;
import org.apache.rya.federation.cluster.sail.OverlapList;
import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.apache.rya.federation.cluster.sail.exception.OverlapListException;

/**
 * The overlap list is the set of subject or object values URIs at each cluster
 * center that appear as subjects or objects in other clusters too.
 */
public class AccumuloOverlapList implements OverlapList {
    private static final Text ROW_ID = new Text("overlap_list_item");
    private static final Text COL_FAMILY = EMPTY_TEXT;
    private static final Text COL_QUALIFIER = EMPTY_TEXT;

    private Connector conn;
    private Instance instance;

    private final ClusterFederationConfig config;
    private final String instanceName;
    private String tableName;
    private final String zkServer;
    private final String username;
    private final String password;

    private final AtomicBoolean isInitialized = new AtomicBoolean();

    /**
     * Creates a new instance of {@link AccumuloOverlapList}.
     * @param config the {@link ClusterFederationConfig}. (not null)
     */
    public AccumuloOverlapList(final ClusterFederationConfig config) {
        this.config = requireNonNull(config);

        this.instanceName = config.getInstanceName();
        this.tableName = config.getTableName();
        this.zkServer = config.getZkServer();
        this.username = config.getUsername();
        this.password = config.getPassword();
    }

    /**
     * Selects the specified table. If the table does not exist, then it is
     * created.
     * @param tableName the name of the table to select. (not null)
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableExistsException
     */
    public void selectTable(final String tableName) throws AccumuloException, AccumuloSecurityException, TableExistsException {
        requireNonNull(tableName);
        final TableOperations ops = conn.tableOperations();
        if (!ops.exists(tableName)) {
            ops.create(tableName);
        }
        this.tableName = tableName;
    }

    /**
     * Creates the {@link BatchWriter} config.
     * @return the {@link BatchWriterConfig}.
     */
    private static BatchWriterConfig createBatchWriterConfig() {
        final BatchWriterConfig batchWriterConfig = new BatchWriterConfig();
        batchWriterConfig.setMaxLatency(1, TimeUnit.MINUTES);
        batchWriterConfig.setMaxMemory(10000000);
        batchWriterConfig.setMaxWriteThreads(10);
        batchWriterConfig.setTimeout(10, TimeUnit.MINUTES);
        return batchWriterConfig;
    }

    /**
     * Creates connection to Accumulo using the specified username and password.
     * @param username a valid accumulo user.
     * @param password a UTF-8 encoded password.
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     */
    public void createConnection(final String username, final String password) throws AccumuloException, AccumuloSecurityException {
        instance = new ZooKeeperInstance(instanceName, zkServer);
        conn = instance.getConnector(username, new PasswordToken(password));
    }

    /**
     * Creates a new batch writer from the table name and config associated with
     * this overlap list.
     * @return the {@link BatchWriter}.
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableNotFoundException
     */
    private BatchWriter createWriter() throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        final BatchWriterConfig batchWriterConfig = createBatchWriterConfig();
        final BatchWriter bw = conn.createBatchWriter(tableName, batchWriterConfig);
        return bw;
    }

    /**
     * Creates a {@link Scanner} for the table.
     * @return the {@link Scanner}.
     * @throws TableNotFoundException
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     */
    public Scanner createScanner() throws TableNotFoundException, AccumuloException, AccumuloSecurityException {
        final Scanner scanner = conn.createScanner(tableName, new Authorizations());
        return scanner;
    }

    @Override
    public void addData(final String value) throws OverlapListException {
        BatchWriter writer = null;
        try {
            writer = createWriter();

            final long timestamp = System.currentTimeMillis();

            final Value tempValue = new Value(value.getBytes(StandardCharsets.UTF_8));

            final Mutation mutation = new Mutation(ROW_ID);
            mutation.put(COL_FAMILY, COL_QUALIFIER, timestamp, tempValue);

            writer.addMutation(mutation);
        } catch (final AccumuloException | AccumuloSecurityException | TableNotFoundException e) {
            throw new OverlapListException("Failed to add data", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (final MutationsRejectedException e) {
                    throw new OverlapListException("Failed to close writer while adding data", e);
                }
            }
        }
    }

    @Override
    public void deleteData(final String value) throws OverlapListException {
        BatchWriter writer = null;
        try {
            writer = createWriter();

            final long timestamp = System.currentTimeMillis();

            final Mutation mutation = new Mutation(ROW_ID);
            mutation.putDelete(COL_FAMILY, COL_QUALIFIER, timestamp);

            writer.addMutation(mutation);
        } catch (final AccumuloException | AccumuloSecurityException | TableNotFoundException e) {
            throw new OverlapListException("Failed to delete data", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (final MutationsRejectedException e) {
                    throw new OverlapListException("Failed to close writer while deleting data", e);
                }
            }
        }
    }

    @Override
    public void setup() throws OverlapListException {
        try {
            if (!isInitialized.get()) {
                createConnection(username, password);
                selectTable(tableName);
                isInitialized.set(true);
            }
        } catch (final AccumuloException | AccumuloSecurityException | TableExistsException e) {
            throw new OverlapListException("Failed to setup OverlapList", e);
        }
    }

    @Override
    public boolean isInitialized() {
        return isInitialized.get();
    }

    @Override
    public ClusterFederationConfig getConfig() {
        return config;
    }

    @Override
    public Set<String> getOverlaps() throws OverlapListException {
        final Set<String> overlaps = new HashSet<>();
        Scanner scanner = null;
        try {
            scanner = createScanner();
            final Iterator<Entry<Key, Value>> iterator = scanner.iterator();

            while (iterator.hasNext()) {
                final Entry<Key, Value> entry = iterator.next();
                final String value = entry.getValue().toString();
                overlaps.add(value);
            }
        } catch (final TableNotFoundException | AccumuloException | AccumuloSecurityException | NoSuchElementException e) {
            throw new OverlapListException("Failed to create overlap list iterator", e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return overlaps;
    }

    @Override
    public void close() throws Exception {
        isInitialized.set(false);
    }
}