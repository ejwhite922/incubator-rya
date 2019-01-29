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

import static org.apache.rya.federation.cluster.sail.TestUtils.getTimeElapsed;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.accumulo.core.bloomfilter.BloomFilter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.security.tokens.PasswordToken;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.file.keyfunctor.RowFunctor;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.util.hash.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests creating bloom filters.
 */
public class CreateBloomFilterTest {
    private static final Logger log = LoggerFactory.getLogger(CreateBloomFilterTest.class);

    public static void main(final String[] args) throws Exception {
        final int vectorSize = 100000;
        final int nbHash = 500;
        final int hashType = Hash.MURMUR_HASH;
        final BloomFilter bloomFilter = new BloomFilter(vectorSize, nbHash, hashType);

        final String instanceName = "dev";
        final String tableURI = "URI_index";
        final String zkServer1 = "192.168.33.30:2181";
        final String username = "root";
        final String password = "root";

        final Instance instance = new ZooKeeperInstance(instanceName, zkServer1);
        final Connector conn = instance.getConnector(username, new PasswordToken(password));
        final Scanner scanURI = conn.createScanner(tableURI, new Authorizations());

        final long start = System.currentTimeMillis();

        final Iterator<Entry<Key, Value>> iterator = scanURI.iterator();
        while (iterator.hasNext()) {
            final Entry<Key, Value> entry = iterator.next();
            final Key accumuloKey = entry.getKey();
            final RowFunctor rowFunctor = new RowFunctor();
            final org.apache.hadoop.util.bloom.Key key = rowFunctor.transform(accumuloKey);
            bloomFilter.add(key);
        }

        try (
            final FileOutputStream fos = new FileOutputStream("/home/vagrant/share/3");
            final ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            // Method for serialization of object
            oos.writeObject(bloomFilter);
        }

        final long end = System.currentTimeMillis();

        log.info("Execution Time: " + getTimeElapsed(start, end));
    }
}