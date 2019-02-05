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

import static org.apache.rya.federation.cluster.sail.TestUtils.addURIs;
import static org.apache.rya.federation.cluster.sail.TestUtils.getTimeElapsed;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests creating URI table.
 */
public class CreateURITableTest {
    private static final Logger log = LoggerFactory.getLogger(CreateURITableTest.class);

    private static Set<String> scan(final Connector conn, final String tableName) throws TableNotFoundException {
        final Set<String> list = new HashSet<>();

        Scanner scanner = null;
        try {
            scanner = conn.createScanner(tableName, new Authorizations());

            final Iterator<Entry<Key, Value>> iterator = scanner.iterator();
            while (iterator.hasNext()) {
                final Entry<Key, Value> entry = iterator.next();
                final String[] pattern = entry.getKey().getRow().toString().split("\\x00");
                list.add(pattern[0]);
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return list;
    }

    public static void main(final String[] args) throws Exception {
        log.info("Starting " + CreateURITableTest.class.getSimpleName() + "...");

        final String instanceName = "dev";
        final String tableURI = "URI_index";
        final String tableSPO = "rya_spo";
        final String tableOSP = "rya_osp";

        final String zkServer1 = "192.168.33.10:2181";
        final String zkServer2 = "192.168.33.20:2181";

        final String username = "root";

        final String password = "root";

        final Set<String> list = new HashSet<>();

        final Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
        final Connector conn1 = inst1.getConnector(username, new PasswordToken(password));

        final Instance inst2 = new ZooKeeperInstance(instanceName, zkServer2);
        final Connector conn2 = inst2.getConnector(username, new PasswordToken(password));

        final long start = System.currentTimeMillis();

        list.addAll(scan(conn1, tableSPO));
        list.addAll(scan(conn1, tableOSP));
        list.addAll(scan(conn2, tableSPO));
        list.addAll(scan(conn2, tableOSP));

        final TableOperations ops = conn1.tableOperations();
        if (!ops.exists(tableURI)) {
            ops.create(tableURI);
        }
        log.info("size: " + list.size());
        addURIs(list, conn1, tableURI);

        final long end = System.currentTimeMillis();

        log.info("Execution Time: " + getTimeElapsed(start, end));
        log.info("Finished " + CreateURITableTest.class.getSimpleName());
    }
}