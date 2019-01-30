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

import static org.apache.rya.federation.cluster.sail.TestUtils.closeConnection;
import static org.apache.rya.federation.cluster.sail.TestUtils.closeRepository;
import static org.apache.rya.federation.cluster.sail.TestUtils.performQuery;

import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.federation.Federation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests cluster federation.
 */
public class ClusterFederationTest {
    private static final Logger log = LoggerFactory.getLogger(ClusterFederationTest.class);

//    private static final boolean USE_MOCK_INSTANCE = false;
//    private static final boolean PRINT_QUERIES = false;
//    private static final String INSTANCE = "dev";
//    private static final String RYA_TABLE_PREFIX = "rya_";
//    private static final String AUTHS = "";
    //the second VM's IP is 192.168.33.20
    private static final String SESAME_SERVER_1 = "http://192.168.33.10:8080/openrdf-sesame";
    private static final String SESAME_SERVER_2 = "http://192.168.33.20:8080/openrdf-sesame";
    private static final String SESAME_SERVER_3 = "http://192.168.33.50:8080/openrdf-sesame";
    private static final String SESAME_SERVER_4 = "http://192.168.33.60:8080/openrdf-sesame";
    private static final String REPOSITORY_ID_12 = "ClusterFederation12";
    private static final String REPOSITORY_ID_34 = "ClusterFederation56";
    private static final String REPOSITORY_ID_1 = "RyaAccumulo_1";
    private static final String REPOSITORY_ID_2 = "RyaAccumulo_2";
    private static final String REPOSITORY_ID_3 = "RyaAccumulo_5";
    private static final String REPOSITORY_ID_4 = "RyaAccumulo_6";

    public static void main(final String[] args) throws Exception {
        log.info("Starting " + ClusterFederationTest.class.getSimpleName() + "...");
        // Repository 1
        final Repository repo1 = new HTTPRepository(SESAME_SERVER_1, REPOSITORY_ID_1);
        repo1.initialize();

        // Repository 2
        final Repository repo2 = new HTTPRepository(SESAME_SERVER_2, REPOSITORY_ID_2);
        repo2.initialize();

        // Repository 3
        final Repository repo3 = new HTTPRepository(SESAME_SERVER_3, REPOSITORY_ID_3);
        repo3.initialize();

        // Repository 4
        final Repository repo4 = new HTTPRepository(SESAME_SERVER_4, REPOSITORY_ID_4);
        repo4.initialize();

        // Federation repository 12
        final Repository repo12 = new HTTPRepository(SESAME_SERVER_1, REPOSITORY_ID_12);
        repo12.initialize();

        // Federation repository 34
        final Repository repo34 = new HTTPRepository(SESAME_SERVER_3, REPOSITORY_ID_34);
        repo34.initialize();

        SailRepository sailRepo12 = null;
        RepositoryConnection con12_34 = null;
//        final RepositoryConnection clusterCon12 = null;
//        final RepositoryConnection con12 = null;
//        final RepositoryConnection con34 = null;

        SailRepository sailRepo34 = null;
        SailRepository sailRepo1234 = null;
        try {
            log.info("Connecting to SailRepository.");
            // Overlap list info
            final ClusterFederationConfig config = new ClusterFederationConfig();
            final String instanceName = "dev";
            final String tableName = OverlapList.DEFAULT_OVERLAP_LIST_TABLE_NAME;
            final String zkServer = "localhost:2181";
            final String username = "root";
            final String password = "root";
            config.setInstanceName(instanceName);
            config.setTableName(tableName);
            config.setZkServer(zkServer);
            config.setUsername(username);
            config.setPassword(password);

            // Federation of 1,2
//            final Federation federation12 = new Federation();
//            federation12.addMember(repo1);
//            federation12.addMember(repo2);

            // Cluster federation of 1,2
            final ClusterFederation clusterFederation12 = new ClusterFederation(config);
//            final Federation clusterFederation12 = new Federation();
            clusterFederation12.addMember(repo1);
            clusterFederation12.addMember(repo2);

            // Cluster federation of 3, 4
            final ClusterFederation clusterFederation34 = new ClusterFederation(config);
//            final Federation clusterFederation34 = new Federation();
            clusterFederation34.addMember(repo3);
            clusterFederation34.addMember(repo4);

            sailRepo12 = new SailRepository(clusterFederation12);
            sailRepo34 = new SailRepository(clusterFederation34);
            sailRepo12.initialize();
            sailRepo34.initialize();

            final Federation federation = new Federation();
            federation.addMember(repo12);
            federation.addMember(repo34);

            sailRepo1234 = new SailRepository(federation);
            sailRepo1234.initialize();
            con12_34 = sailRepo1234.getConnection();

            // Execute query
            final String query =
                "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix daml: <http://www.daml.org/2001/03/daml+oil#>\n" +
                "prefix ub: <https://rya.apache.org#>\n" +
                "SELECT ?X ?Y ?Z\n" +
                "WHERE\n" +
                "{\n" +
                "    ?Y ub:teacherOf ?Z .\n" +
                "    ?X ub:advisor ?Y .\n" +
                "    ?X ub:takesCourse ?Z.\n" +
                "}";

            performQuery(con12_34, query);
        } finally {
            closeConnection(con12_34);
            closeRepository(sailRepo1234);
        }
    }
}