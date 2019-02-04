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

import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.apache.rya.federation.cluster.sail.overlap.AccumuloOverlapList;
import org.apache.rya.federation.cluster.sail.overlap.OverlapListDbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests data load time.
 */
public class LoadDataTimeTest {
    private static final Logger log = LoggerFactory.getLogger(LoadDataTimeTest.class);

    public static void main(final String[] args) throws Exception {
        log.info("Starting " + LoadDataTimeTest.class.getSimpleName() + "...");

        final String instanceName = "dev";
        final String tableURI = "URI_index";
        final String zkServer1 = "192.168.33.50:2181";
        final String username = "root";
        final String password = "root";

        final ClusterFederationConfig config = new ClusterFederationConfig();
        config.setInstanceName(instanceName);
        config.setTableName(tableURI);
        config.setZkServer(zkServer1);
        config.setUsername(username);
        config.setPassword(password);
        config.setOverlapListDbType(OverlapListDbType.ACCUMULO);

        final long start = System.currentTimeMillis();
        try (final OverlapList ol1 = new AccumuloOverlapList(config)) {
            ol1.setup();

            final int count = ol1.getOverlaps().size();

            log.info("Result count: " + count);
            final long end = System.currentTimeMillis();
            log.info("Execution Time: " + getTimeElapsed(start, end));
        }

        log.info("Finished " + LoadDataTimeTest.class.getSimpleName());
    }
}