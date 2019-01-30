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

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the methods of {@link AccumuloOverlapList}.
 */
public class AccumuloOverlapListTest {
    private static final Logger log = LoggerFactory.getLogger(AccumuloOverlapListTest.class);

    public static void main(final String[] args) throws Exception {
        final String instanceName = "dev";
        final String tableName = "rya_spo";
        final String zkServer = "192.168.33.50:2181";
        final String username = "root";
        final String password = "root";

        final ClusterFederationConfig config = new ClusterFederationConfig();
        config.setInstanceName(instanceName);
        config.setTableName(tableName);
        config.setZkServer(zkServer);
        config.setUsername(username);
        config.setPassword(password);
        config.setOverlapListDbType(OverlapListDbType.ACCUMULO);

        try (final AccumuloOverlapList overlapList = new AccumuloOverlapList(config)) {
            overlapList.setup();
            final Scanner sc = overlapList.createScanner();

            final String course15 = "http://www.Department0.University0.edu/Course15";
            final String course16 = "http://www.Department0.University0.edu/Course16";
//            final String course17 = "http://www.Department0.University0.edu/GraduateCourse17";
//            final String course18 = "http://www.Department0.University0.edu/GraduateCourse18";

            final int studentID = 50;
//            final String rowID = "http://www.Department0.University0.edu/GraduateStudent19";

//            final ColumnVisibility colVis = new ColumnVisibility("public");

            // Insert data
            for (int i = 0; i < studentID; i++) {
                overlapList.addData("http://www.Department0.University2.edu/UndergraduateStudent" + i);
            }

            overlapList.addData(course15);
            overlapList.addData(course15);
            overlapList.addData(course16);
//            overlapList.addData(course17);
//            overlapList.addData(course18);
            // Delete data
            overlapList.deleteData(course16);
            // Scan data
            final Iterator<Entry<Key, Value>> iterator = sc.iterator();

            while (iterator.hasNext()) {
                final Entry<Key, Value> entry = iterator.next();
                final Key key = entry.getKey();
                final Value value = entry.getValue();
                log.info(key.getRow() + " ==> " + value.toString());
            }

            final Set<String> overlaps = overlapList.getOverlaps();

            for (final String overlap : overlaps) {
                log.info(overlap);
            }
        }
    }
}