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

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ClusterFederationQueryTest {
    private static final Logger log = LoggerFactory.getLogger(ClusterFederationQueryTest.class);

//    private static final boolean USE_MOCK_INSTANCE = false;
//    private static final boolean PRINT_QUERIES = false;
//    private static final String INSTANCE = "dev";
//    private static final String RYA_TABLE_PREFIX = "rya_";
//    private static final String AUTHS = "";
    //the second VM's IP is 192.168.33.20
    private static final String SESAME_SERVER_1 = "http://192.168.33.10:8080/openrdf-sesame";
    private static final String SESAME_SERVER_2 = "http://192.168.33.20:8080/openrdf-sesame";
    private static final String SESAME_SERVER_3 = "http://192.168.33.20:8080/openrdf-sesame";
    private static final String SESAME_SERVER_5 = "http://192.168.33.50:8080/openrdf-sesame";

    private static final String REPOSITORY_ID_12 = "Federation12";

    private static final String REPOSITORY_ID_34 = "Federation34";

    private static final String REPOSITORY_ID_56 = "Federation56";

    private static final String REPOSITORY_ID_123456 = "Federation12_34_56";

//    private static final String REPOSITORY_ID_CLUSTER_12 = "ClusterFederation12_sec";
//    private static final String REPOSITORY_ID_CLUSTER_34 = "ClusterFederation34_sec";

    public static void main(final String[] args) throws Exception {
        log.info("Starting " + ClusterFederationQueryTest.class.getSimpleName() + "...");
        // Federation repository 12
        final Repository repo12 = new HTTPRepository(SESAME_SERVER_1, REPOSITORY_ID_12);
        repo12.initialize();

        // Federation repository 34
        final Repository repo34 = new HTTPRepository(SESAME_SERVER_3, REPOSITORY_ID_34);
        repo34.initialize();

        // Federation repository 56
        final Repository repo56 = new HTTPRepository(SESAME_SERVER_5, REPOSITORY_ID_56);
        repo56.initialize();

        // Federation repository 123456
        final Repository repo12_34_56 = new HTTPRepository(SESAME_SERVER_2, REPOSITORY_ID_123456);
        repo12_34_56.initialize();

        RepositoryConnection con1234 = null;
        RepositoryConnection con12 = null;
        RepositoryConnection con34 = null;
        RepositoryConnection con56 = null;

        try {
            log.info("Connecting to SailRepository.");
            // Overlap list info
//            final String instanceName = "dev";
//            final String tableName = "rya_overlap";
//            final String zkServer = "localhost:2181";
//            final String username = "root";
//            final String password = "root";


//            // Federation of 12,34
//            final Federation federation12 = new Federation();
//            federation12.addMember(repo1);
//            federation12.addMember(repo2);
//
//            final Federation federation34 = new Federation();
//            federation34.addMember(repo3);
//            federation34.addMember(repo4);
//
//            sailRepo12 = new SailRepository(federation12);
//            sailRepo12.initialize();
//
//            sailRepo34 = new SailRepository(federation34);
//            sailRepo34.initialize();

            con1234 = repo12_34_56.getConnection();
            con12 = repo12.getConnection();
            con34 = repo34.getConnection();
            con56 = repo56.getConnection();

//            // Create a new repository and id
//            final RemoteRepositoryManager manager = new RemoteRepositoryManager(SESAME_SERVER_3);
//            manager.initialize();
//
//            final String repositoryId = "ClusterFederation34";
//            final RepositoryImplConfig implConfig = new HTTPRepositoryConfig(SESAME_SERVER_3);
//            final RepositoryConfig repConfig = new RepositoryConfig(repositoryId, implConfig);
//            manager.addRepositoryConfig(repConfig);
//
//            // Create a repository variable from a given id
//            final Repository repo34 = manager.getRepository(repositoryId);
//            repo34.initialize();
//            con34 = repo34.getConnection();
//
//            // Remove a repository
//            manager.removeRepository(repositoryId);

            final long start = System.currentTimeMillis();
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

            final TupleQuery tupleQuery12 = con12.prepareTupleQuery(QueryLanguage.SPARQL, query);
            final TupleQueryResult result12 = tupleQuery12.evaluate();
            final long phase_1_12 = System.currentTimeMillis();

            final TupleQuery tupleQuery34 = con34.prepareTupleQuery(QueryLanguage.SPARQL, query);
            final TupleQueryResult result34 = tupleQuery34.evaluate();
            final long phase_1_34 = System.currentTimeMillis();

            final TupleQuery tupleQuery56 = con56.prepareTupleQuery(QueryLanguage.SPARQL, query);
            final TupleQueryResult result56 = tupleQuery56.evaluate();
            final long phase_1_56 = System.currentTimeMillis();

            final TupleQuery tupleQuery1234 = con1234.prepareTupleQuery(QueryLanguage.SPARQL, query);
            final TupleQueryResult result1234 = tupleQuery1234.evaluate();
            final long end = System.currentTimeMillis();

            log.info("phase 1_12 time: " + (phase_1_12 - start));
            log.info("phase 1_34 time: " + (phase_1_34 - phase_1_12));
            log.info("phase 1_56 time: " + (phase_1_56 - phase_1_34));
            log.info("phase 2 time: " + (end - phase_1_56));

            BindingSet bindingSet = null;
            int count = 0;
            log.info("phase1_12:");
            while (result12.hasNext()) {
                bindingSet = result12.next();
                final Value valueOfX = bindingSet.getValue("X");
                log.trace("X: " + valueOfX);
                count++;
            }
            log.info("result size: " + count);
            log.info("phase1_34:");

            while (result34.hasNext()) {
                bindingSet = result34.next();
                final Value valueOfX = bindingSet.getValue("X");
                log.trace("X: " + valueOfX);
                count++;
            }
            log.info("result size: " + count);

            log.info("phase1_56:");

            while (result56.hasNext()) {
                bindingSet = result56.next();
                final Value valueOfX = bindingSet.getValue("X");
                log.trace("X: " + valueOfX);
                count++;
            }
            log.info("result size: " + count);

            log.info("phase2:");

            while (result1234.hasNext()) {
                bindingSet = result1234.next();
                final Value valueOfX = bindingSet.getValue("X");
                log.trace("X: " + valueOfX);
                count++;
            }

            log.info("result size: " + count);
        } finally {
            closeConnection(con1234);
            closeConnection(con56);
            closeConnection(con34);
            closeConnection(con12);
            closeRepository(repo12);
            closeRepository(repo34);
            closeRepository(repo56);
            closeRepository(repo12_34_56);
        }
    }
}