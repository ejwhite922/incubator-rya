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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;

/**
 *
 */
public class CreateOverlapTest {
    public static void main(final String[] args) throws Exception {
        final String instanceName = "dev";
        final String tableURI = "URI_index";
        final String tableNewURI31 = "new_URI_index_31";
        final String tableNewURI51 = "new_URI_index_51";
        final String tableOverlap = "rya_overlap";

        final String zkServer1 = "192.168.33.10:2181";
        final String zkServer3 = "192.168.33.30:2181";
        final String zkServer5 = "192.168.33.50:2181";

        final String username = "root";

        final String password = "root";

        final long start = System.currentTimeMillis();

        final Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
        final Connector conn1 = inst1.getConnector(username, new PasswordToken(password));

        final Instance inst3 = new ZooKeeperInstance(instanceName, zkServer3);
        final Connector conn3 = inst3.getConnector(username, new PasswordToken(password));

        final Instance inst5 = new ZooKeeperInstance(instanceName, zkServer5);
        final Connector conn5 = inst5.getConnector(username, new PasswordToken(password));

        final List<String>overlap = new ArrayList<>();

        final Scanner scan1 = conn1.createScanner(tableURI, new Authorizations());
        final Scanner scan31 = conn3.createScanner(tableNewURI31, new Authorizations());
        final Scanner scan51 = conn5.createScanner(tableNewURI51, new Authorizations());

        final Iterator<Entry<Key, Value>> iterator31 = scan31.iterator();
        final Iterator<Entry<Key, Value>> iterator51 = scan51.iterator();

        while (iterator31.hasNext()) {
            final Entry<Key, Value> entry31 = iterator31.next();
            final String key31 = entry31.getKey().getRow().toString();
            scan1.setRange(Range.exact(key31));
            final Iterator<Entry<Key, Value>> iterator1 = scan1.iterator();
            if (iterator1.hasNext()) {
                if(key31.contains("http")&& !(key31.contains("org"))) {
                    overlap.add(key31);
                }
            }
        }

        while (iterator51.hasNext()) {
            final Entry<Key, Value> entry51 = iterator51.next();
            final String key51 = entry51.getKey().getRow().toString();
            scan1.setRange(Range.exact(key51));
            final Iterator<Entry<Key, Value>> iterator1 = scan1.iterator();
            if (iterator1.hasNext()) {
                if (key51.contains("http") && !key51.contains("org")) {
                    overlap.add(key51);
                }
            }
        }
        System.out.println("size: " + overlap.size());
        final TableOperations ops = conn1.tableOperations();
        if (!ops.exists(tableOverlap)) {
            ops.create(tableOverlap);
        }
        TestUtils.addURIs(overlap, conn1, tableOverlap);

        final long end = System.currentTimeMillis();

        System.out.println(end - start);
    }
}