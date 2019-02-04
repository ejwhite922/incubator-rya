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

import java.util.Set;

import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.apache.rya.federation.cluster.sail.overlap.AccumuloOverlapList;
import org.apache.rya.federation.cluster.sail.overlap.OverlapListDbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests overlap list.
 */
public class OverlapListQuery2 {
    private static final Logger log = LoggerFactory.getLogger(OverlapListQuery2.class);

    public static void main(final String[] args) throws Exception {
        log.info("Starting " + OverlapListQuery2.class.getSimpleName() + "...");

        final String instanceName = "dev";
        final String tableName = OverlapList.DEFAULT_OVERLAP_LIST_TABLE_NAME;
        final String zkServer = "localhost:2181";
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

            final int numDept = 10;
            final String univ0 = "http://www.University0.edu";
            final int studentID = 100;
            final String rowID = "http://www.Department0.University0.edu/GraduateStudent19";

            // Insert data
            for (int i = 0; i < numDept; i++) {
                overlapList.addData("http://www.Department" + i + ".University0.edu");
                for (int j = 0; j < studentID; j++) {
                    overlapList.addData("http://www.Department" + i + ".University0.edu" + "/GraduateStudent" + j);
                }
            }

            overlapList.addData(univ0);

            // Delete data
            overlapList.deleteData(rowID);

            // Scan data
            TestUtils.printOverlapScanner(overlapList);

            final Set<String> overlaps = overlapList.getOverlaps();

            for (final String overlap : overlaps) {
                log.info(overlap);
            }

            log.info("Finished " + OverlapListQuery2.class.getSimpleName());
        }
    }
}