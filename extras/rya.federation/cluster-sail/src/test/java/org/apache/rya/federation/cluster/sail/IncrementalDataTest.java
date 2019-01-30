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

import static org.apache.rya.federation.cluster.sail.TestUtils.addURI;
import static org.apache.rya.federation.cluster.sail.TestUtils.addURIs;
import static org.apache.rya.federation.cluster.sail.TestUtils.getTimeElapsed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests incremental data addition.
 */
public class IncrementalDataTest {
    private static final Logger log = LoggerFactory.getLogger(IncrementalDataTest.class);

    public static boolean lookUp(final String uri, final Scanner scan) throws TableNotFoundException {
        scan.setRange(Range.exact(uri));
        final Iterator<Entry<Key, Value>> iterator = scan.iterator();
        return iterator.hasNext();
    }

    public static void intersectOverlap(final String uri, final Connector conn, final Connector conn2, final String tableOverlap) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        addURI(uri, conn, tableOverlap);
        addURI(uri, conn2, tableOverlap);
    }

    public static void intersectData(final Scanner uriScanner1, final Scanner uriScanner2, final Connector conn, final String tableOverlap) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        final List<String> overlapData = new ArrayList<>();
        final Iterator<Entry<Key, Value>> iterator1 = uriScanner1.iterator();
        while (iterator1.hasNext()) {
            final Entry<Key, Value> entry1 = iterator1.next();
            final String key1 = entry1.getKey().getRow().toString();
            uriScanner2.setRange(Range.exact(key1));
            final Iterator<Entry<Key, Value>> iterator2 = uriScanner2.iterator();
            if (iterator2.hasNext()) {
                overlapData.add(key1);
            }
        }
        if (!overlapData.isEmpty()) {
            addURIs(overlapData, conn, tableOverlap);
        }
    }

    public static void main(final String[] args) throws Exception {
        final List<String> triples = new ArrayList<>();
        for (int i = 12; i < 13; i++) {
            for (int j = 270; j < 271; j++) {
                triples.add("http://www.Department" + i + ".University2.edu/UndergraduateStudent" + j);
                triples.add("http://www.Department" + i + ".University3.edu/UndergraduateStudent" + j);
                triples.add("http://www.Department" + i + ".University4.edu/UndergraduateStudent" + j);
                triples.add("http://www.Department" + i + ".University5.edu/UndergraduateStudent" + j);
            }
        }

        final String instanceName = "dev";
        final String tableURI = "URI_index";
        final String tableNewURI = "new_URI_index";
        final String tableOverlap = OverlapList.DEFAULT_OVERLAP_LIST_TABLE_NAME;

        final String zkServer1 = "192.168.33.10:2181";
        final String zkServer3 = "192.168.33.30:2181";
        final String zkServer5 = "192.168.33.50:2181";

        final String username = "root";
        final String password = "root";

        final Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
        final Connector conn1 = inst1.getConnector(username, new PasswordToken(password));
        final Scanner scan1NewURI = conn1.createScanner(tableNewURI, new Authorizations());
        final Scanner scan1URI = conn1.createScanner(tableURI, new Authorizations());

        final Instance inst3 = new ZooKeeperInstance(instanceName, zkServer3);
        final Connector conn3 = inst3.getConnector(username, new PasswordToken(password));
        final Scanner scan3URI = conn3.createScanner(tableURI, new Authorizations());

        final Instance inst5 = new ZooKeeperInstance(instanceName, zkServer5);
        final Connector conn5 = inst5.getConnector(username, new PasswordToken(password));
        final Scanner scan5URI = conn5.createScanner(tableURI, new Authorizations());

        final List<String> incrementalData = new ArrayList<>();
        try {
            final long start = System.currentTimeMillis();

            for (final String triple : triples) {
                if (!lookUp(triple, scan1URI)) {
                    log.info("new URI: " + triple);
                    incrementalData.add(triple);
                }
            }

            if (!incrementalData.isEmpty()) {
                addURIs(incrementalData, conn1, tableURI);
                addURIs(incrementalData, conn1, tableNewURI);
            }
            final long phase1 = System.currentTimeMillis();
            log.info("phase 1 execution time: " + getTimeElapsed(start, phase1));

            intersectData(scan1NewURI, scan3URI, conn3, tableOverlap);
            intersectData(scan1NewURI, scan5URI, conn5, tableOverlap);

            final long end = System.currentTimeMillis();
            log.info("phase 2 execution time: " + getTimeElapsed(phase1, end));
        } catch (final TableNotFoundException e) {
            log.error("Table not found", e);
        }
    }
}