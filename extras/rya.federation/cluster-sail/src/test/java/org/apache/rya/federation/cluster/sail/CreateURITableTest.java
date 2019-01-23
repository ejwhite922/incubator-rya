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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;

/**
 *
 * @author vagrant
 */
public class CreateURITableTest {
    public static void main(final String[] args) throws Exception {
        final String instanceName = "dev";
        final String tableURI = "URI_index";
        final String tableSPO = "rya_spo";
        final String tableOSP = "rya_osp";

        final String zkServer1 = "192.168.33.10:2181";
        final String zkServer2 = "192.168.33.20:2181";

        final String username = "root";

        final String password = "root";

        final Set<String> list = new HashSet<String>();

        final Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
        final Connector conn1 = inst1.getConnector(username, new PasswordToken(password));

        final Instance inst2 = new ZooKeeperInstance(instanceName, zkServer2);
        final Connector conn2 = inst2.getConnector(username, new PasswordToken(password));

        final Scanner scan2SPO = conn2.createScanner(tableSPO, new Authorizations());
        final Scanner scan2OSP = conn2.createScanner(tableOSP, new Authorizations());
        final Scanner scan1SPO = conn1.createScanner(tableSPO, new Authorizations());
        final Scanner scan1OSP = conn1.createScanner(tableOSP, new Authorizations());

        final Iterator<Entry<Key, Value>> iterator1SPO = scan1SPO.iterator();
        final Iterator<Entry<Key, Value>> iterator1OSP = scan1OSP.iterator();
        final Iterator<Entry<Key, Value>> iterator2SPO = scan2SPO.iterator();
        final Iterator<Entry<Key, Value>> iterator2OSP = scan2OSP.iterator();

        final long start = System.currentTimeMillis();

        while (iterator1SPO.hasNext()) {
            final Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator1SPO.next();
            final String [] pattern = entry.getKey().getRow().toString().split("\\x00");
            list.add(pattern[0]);
        }

        while (iterator1OSP.hasNext()) {
            final Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator1OSP.next();
            final String [] pattern = entry.getKey().getRow().toString().split("\\x00");
            list.add(pattern[0]);
        }

        while (iterator2SPO.hasNext()) {
            final Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator2SPO.next();
            final String [] pattern = entry.getKey().getRow().toString().split("\\x00");
            list.add(pattern[0]);
        }

        while (iterator2OSP.hasNext()) {
            final Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator2OSP.next();
            final String [] pattern = entry.getKey().getRow().toString().split("\\x00");
            list.add(pattern[0]);
        }

        final TableOperations ops = conn1.tableOperations();
        if (!ops.exists(tableURI)) {
            ops.create(tableURI);
        }
        System.out.println("size: " + list.size());
        TestUtils.addURIs(list, conn1, tableURI);

        final long end = System.currentTimeMillis();

        System.out.println(end - start);
    }
}