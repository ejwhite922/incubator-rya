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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;

/**
 *
 */
public class NHopOverlapTest {
    public static void computeClosure(final Connector conn1, final Connector conn2, final String tableSPO, final Set<String> newOverlap) throws TableNotFoundException {
        final Scanner scanSPO1 = conn1.createScanner(tableSPO, new Authorizations());
        final Scanner scanSPO2 = conn2.createScanner(tableSPO, new Authorizations());

        String key = null;
        final Iterator<Entry<Key, Value>> iteratorSPO1 = scanSPO1.iterator();
        while (iteratorSPO1.hasNext()) {
            final Entry<Key, Value> entry = iteratorSPO1.next();
            key = entry.getKey().getRow().toString();
            final String [] pattern = key.split("\\x00");
            final String subject = pattern[0];
            String object = pattern[2].replaceAll("\\x01\\x02", "");
            object = object.replaceAll("\\x01\\x03", "");
            if (newOverlap.contains(subject) && !key.contains("type")  && !key.contains("DegreeFrom") && !key.contains("Publication")) {
                newOverlap.add(object);
            }
            if (newOverlap.contains(object) && !key.contains("type")  && !key.contains("DegreeFrom") && !key.contains("Publication")) {
                newOverlap.add(subject);
            }
        }

        final Iterator<Entry<Key, Value>> iteratorSPO2 = scanSPO2.iterator();
        while (iteratorSPO2.hasNext()) {
            final Entry<Key, Value> entry = iteratorSPO2.next();
            key = entry.getKey().getRow().toString();
            final String [] pattern = key.split("\\x00");
            final String subject = pattern[0];
            String object = pattern[2].replaceAll("\\x01\\x02", "");
            object = object.replaceAll("\\x01\\x03", "");
            if (newOverlap.contains(subject)  && !(key.contains("type"))){
                newOverlap.add(object);
            }
            if (newOverlap.contains(object)  && !(key.contains("type"))){
                newOverlap.add(subject);
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        final int N = 1;

        final String instanceName = "dev";
        final String tableSPO = "rya_spo";

        final String tableOverlap = "rya_overlap";

        final String zkServer1 = "192.168.33.10:2181";
        final String zkServer2 = "192.168.33.20:2181";


        final String username = "root";

        final String password = "root";

        final Set<String> overlap = new HashSet<>();
        final Set<String> newOverlap = new HashSet<>();

        final Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
        final Connector conn1 = inst1.getConnector(username, new PasswordToken(password));
        final Scanner scanSPO1 = conn1.createScanner(tableSPO, new Authorizations());

        final Scanner scanOverlap = conn1.createScanner(tableOverlap, new Authorizations());

        final Instance inst2 = new ZooKeeperInstance(instanceName, zkServer2);
        final Connector conn2 = inst2.getConnector(username, new PasswordToken(password));
        final Scanner scanSPO2 = conn2.createScanner(tableSPO, new Authorizations());

        String key = null;

        final Iterator<Entry<Key, Value>> iteratorOverlap = scanOverlap.iterator();
        while (iteratorOverlap.hasNext()) {
            final Entry<Key, Value> entry = iteratorOverlap.next();
            key = entry.getKey().getRow().toString();
            overlap.add(key);
        }

        System.out.println("size: " + overlap.size());

        final long start = System.currentTimeMillis();

//        final Map<String, List<String>> so = new HashMap<>();
        final Iterator<Entry<Key, Value>> iteratorSPO1 = scanSPO1.iterator();
        while (iteratorSPO1.hasNext()) {
            final Entry<Key,Value> entry = iteratorSPO1.next();
            key = entry.getKey().getRow().toString();
            final String [] pattern = key.split("\\x00");
            final String subject = pattern[0];
            String object = pattern[2].replaceAll("\\x01\\x02", "");
            object = object.replaceAll("\\x01\\x03", "");

            if (overlap.contains(subject) && !(key.contains("type"))){
                newOverlap.add(object);
            }
            if (overlap.contains(object) && !(key.contains("type")) && !(key.contains("DegreeFrom")) && !(key.contains("Publication")) ){
                newOverlap.add(subject);
            }
        }

        final Iterator<Entry<Key, Value>> iteratorSPO2 = scanSPO2.iterator();
        while (iteratorSPO2.hasNext()){
            final Entry<Key, Value> entry = iteratorSPO2.next();
            key = entry.getKey().getRow().toString();
            final String [] pattern = key.split("\\x00");
            final String subject = pattern[0];
            String object = pattern[2].replaceAll("\\x01\\x02", "");
            object = object.replaceAll("\\x01\\x03", "");

            if (overlap.contains(subject) && !key.contains("type")  && !key.contains("DegreeFrom") && !key.contains("Publication")) {
                newOverlap.add(object);
            }
            if (overlap.contains(object) && !key.contains("type") && !key.contains("DegreeFrom") && !key.contains("Publication")) {
               newOverlap.add(subject);
            }
        }

        for (int hop = 1; hop < N; hop++) {
            final List<String>contents = new ArrayList<>();
            for (final String content: newOverlap){
                if (content.contains("org")) {
                    contents.add(content);
                }
            }
            newOverlap.removeAll(contents);
            System.out.println("1st: " + newOverlap.size());
//            computeClosure(conn1, conn2, tableSPO, newOverlap);
        }

        System.out.println("2nd: " + newOverlap.size());
        TestUtils.addURIs(newOverlap, conn1, tableOverlap);

        final long end = System.currentTimeMillis();

        System.out.println(end - start);
    }
}