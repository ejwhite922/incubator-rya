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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
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

/**
 * The overlap list is the set of subject or object values {@link IRI}s at each
 * each cluster center that appear as subjects or objects in other clusters too.
 */
public class OverlapList {
    private final String instanceName;
    private BatchWriterConfig config;
    private BatchWriter bw;
    private Scanner scan;
    private Connector conn;
    private String tableName;
    private final String zkServer;
    private Instance instance;

    /**
     * Creates a new instance of {@link OverlapList}.
     * @param zkServer the zookeeper host server. (not null)
     * @param instanceName the accumulo instance name. (not null)
     */
    public OverlapList(final String zkServer, final String instanceName) {
        this.zkServer = requireNonNull(zkServer);
        this.instanceName = requireNonNull(instanceName);
//        this.tableName = tableName;
    }

    /**
     * Select table, if table not exist, then create.
     * @param tableName the name of the table to select.
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableExistsException
     */
    public void selectTable(final String tableName) throws AccumuloException, AccumuloSecurityException, TableExistsException {
        final TableOperations ops = conn.tableOperations();
        if (!ops.exists(tableName)) {
            ops.create(tableName);
        }
        this.tableName = tableName;
    }

    /**
     * Creates the config.
     * @return the {@link BatchWriterConfig}.
     */
    public BatchWriterConfig createConfig() {
        config = new BatchWriterConfig();
        config.setMaxLatency(1, TimeUnit.MINUTES);
        config.setMaxMemory(10000000);
        config.setMaxWriteThreads(10);
        config.setTimeout(10, TimeUnit.MINUTES);
        return config;
    }

    /**
     * Get connection
     *
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
    public BatchWriter createWriter() throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        bw = conn.createBatchWriter(tableName, config);
        return bw;
    }

    /**
     * Create Scan
     * @return
     * @throws TableNotFoundException
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     */
    public Scanner createScanner() throws TableNotFoundException, AccumuloException, AccumuloSecurityException {
        scan = conn.createScanner(tableName, new Authorizations());
        return scan;
    }

    /**
     * Insert data into table
     * @param rowId
     * @param value
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableNotFoundException
     */
    public void addData(final String rowId, final String value) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        BatchWriter writer = null;
        try {
            writer = createWriter();
            final Text rowID = new Text(rowId);
            final Text colFam = new Text("   ");
            final Text colQual = new Text("   ");
    //        final ColumnVisibility colVis = new ColumnVisibility("public");
            final long timestamp = System.currentTimeMillis();

            final Value tempValue = new Value(value.getBytes(StandardCharsets.UTF_8));

            final Mutation mutation = new Mutation(rowID);
            mutation.put(colFam, colQual, timestamp, tempValue);

            writer.addMutation(mutation);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     *
     * @param rowId
     * @param value
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableNotFoundException
     */
    public void deleteData(final String rowId, final String value) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        BatchWriter writer = null;
        try {
            writer = createWriter();
            final Text rowID = new Text(rowId);
            final Text colFam = new Text("   ");
            final Text colQual = new Text("   ");
    //        final ColumnVisibility colVis = new ColumnVisibility("public");
            final long timestamp = System.currentTimeMillis();

//            final Value tempValue = new Value(value.getBytes(StandardsCharsets.UTF_8));

            final Mutation mutation = new Mutation(rowID);
            mutation.putDelete(colFam, colQual, timestamp);

            writer.addMutation(mutation);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        final String instanceName = "dev";
        final String tableName = "rya_spo";
        final String zkServer = "192.168.33.50:2181";
        final OverlapList at = new OverlapList(zkServer, instanceName);

        final String username = "root";
        final String password = "root";

        at.createConnection(username, password);
        at.selectTable(tableName);
        final Scanner sc = at.createScanner();

        final String course15 = "http://www.Department0.University0.edu/Course15";
//        final String course16 = "http://www.Department0.University0.edu/Course16";
//        final String course17 = "http://www.Department0.University0.edu/GraduateCourse17";
//        final String course18 = "http://www.Department0.University0.edu/GraduateCourse18";

        final int studentID = 50;
//        final String rowID = "http://www.Department0.University0.edu/GraduateStudent19";
        final String rowValue = "2";
//        final ColumnVisibility colVis = new ColumnVisibility("public");

        // Insert data
        for (int i = 0; i < studentID; i++) {
            at.addData("http://www.Department0.University2.edu/UndergraduateStudent" + i, rowValue);
        }

        at.addData(course15, rowValue);
//        at.addData(course16, rowValue);
//        at.addData(course17, rowValue);
//        at.addData(course18, rowValue);
        // Delete data
//        at.deleteData(rowID, rowValue);
        // Scan data
        final Iterator<Map.Entry<Key,Value>> iterator = sc.iterator();
//       final Set<String> result = new HashSet<>();

        while (iterator.hasNext()) {
            final Map.Entry<Key,Value> entry = iterator.next();
            final Key key = entry.getKey();
            final Value value = entry.getValue();
            System.out.println(key.getRow() + " ==> " + value.toString());
        }
    }
}