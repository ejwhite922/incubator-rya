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

public class FederationTest{
    private static final Logger log = LoggerFactory.getLogger(FederationTest.class);

//    private static final boolean USE_MOCK_INSTANCE = false;
//    private static final boolean PRINT_QUERIES = false;
//    private static final String INSTANCE = "dev";
//    private static final String RYA_TABLE_PREFIX = "rya_";
//    private static final String AUTHS = "";
    //the second VM's IP is 192.168.33.20
//    private static final String SESAME_SERVER_1 = "http://192.168.33.10:8080/openrdf-sesame";
    private static final String SESAME_SERVER_2 = "http://192.168.33.20:8080/openrdf-sesame";
//    private static final String SESAME_SERVER_3 = "http://192.168.33.30:8080/openrdf-sesame";
//    private static final String SESAME_SERVER_4 = "http://192.168.33.40:8080/openrdf-sesame";
//    private static final String REPOSITORY_ID_1 = "RyaAccumulo_1";
    private static final String REPOSITORY_ID_2 = "Federation12";
//    private static final String REPOSITORY_ID_3 = "RyaAccumulo_3";
//    private static final String REPOSITORY_ID_4 = "RyaAccumulo_4";

    public static void main(final String[] args) throws Exception {
        log.info("Starting " + FederationTest.class.getSimpleName() + "...");
        // Repository 1
        final Repository repo1 = new HTTPRepository(SESAME_SERVER_2, REPOSITORY_ID_2);
        repo1.initialize();

        // Repository 2
//        final Repository repo2 = new HTTPRepository(SESAME_SERVER_2, REPOSITORY_ID_2);
//        repo2.initialize();

        // Repository 3
//        final Repository repo3 = new HTTPRepository(SESAME_SERVER_3, REPOSITORY_ID_3);
//        repo3.initialize();

        // Repository 4
//        final Repository repo4 = new HTTPRepository(SESAME_SERVER_4, REPOSITORY_ID_4);
//        repo4.initialize();

        RepositoryConnection con1234 = null;
//        SailRepository sailRepo12 = null;
//        SailRepository sailRepo34 = null;
//        SailRepository sailRepo1234 = null;
        try {
            log.info("Connecting to SailRepository.");
            // Overlap list info
//            final String instanceName = "dev";
//            final String tableName = "rya_overlap";
//            final String zkServer = "localhost:2181";
//            final String username = "root";
//            final String password = "root";

            // Cluster federation of 1, 2
//            final ClusterFederation clusterFederation12 = new ClusterFederation(zkServer);
//            clusterFederation12.addMember(repo1);
//            clusterFederation12.addMember(repo2);
            // Cluster federation of 3, 4
//            final ClusterFederation clusterFederation34 = new ClusterFederation(zkServer);
//            clusterFederation34.addMember(repo3);
//            clusterFederation34.addMember(repo4);

//            sailRepo12 = new SailRepository(clusterFederation12);
//            sailRepo12.initialize();
//            sailRepo34 = new SailRepository(clusterFederation34);
//            sailRepo34.initialize();

//            final Federation federation1234 = new Federation();
//            federation1234.addMember(sailRepo12);
//            federation1234.addMember(sailRepo34);

//            sailRepo1234 = new SailRepository(federation1234);
            con1234 = repo1.getConnection();
//            con34 = sailRepo34.getConnection();


//            // Create a new repository and id
//            final RemoteRepositoryManager manager = new RemoteRepositoryManager(SESAME_SERVER_3);
//            manager.initialize();
//
//            final String repositoryId = "test_cfs";
//            final RepositoryImplConfig implConfig = new HTTPRepositoryConfig(SESAME_SERVER_3);
//            final RepositoryConfig repConfig = new RepositoryConfig(repositoryId, implConfig);
//            manager.addRepositoryConfig(repConfig);
//
//            // create a repository variable from a given id
//            final Repository repo34 = manager.getRepository(repositoryId);
//            repo34.initialize();
//            con34 = repo34.getConnection();
//
//            test = new SailRepository(federation1234);
//
//
//            // Remove a repository
//            manager.removeRepository(repositoryId);
//
//            // Get repository IDs
//            final Iterator <String> ids = manager.getRepositoryIDs().iterator();
//            while (ids.hasNext()) {
//                log.info(ids.next());
//            }
//
//            conTest = (SailRepositoryConnection) test.getConnection();

            final long start = System.currentTimeMillis();
//            // Execute query
//            final String query =
//                "PREFIX code:<http://telegraphis.net/ontology/measurement/code#>\n" +
//                "PREFIX geographis:<http://telegraphis.net/ontology/geography/geography#>\n" +
//                "PREFIX money:<http://telegraphis.net/ontology/money/money#>\n" +
//                "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" +
//                "PREFIX gn:<http://www.geonames.org/ontology#>\n" +
//                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                "select ?x\n" +
//                "where {\n" +
//                "    ?x geographis:capital ?capital .\n" +
//                "    ?x geographis:currency ?currency .\n" +
//                "}";

//            final String query =
//                "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
//                "prefix dc: <http://purl.org/dc/elements/1.1/>\n" +
//                "prefix foaf: <http://xmlns.com/foaf/0.1/>\n" +
//                "prefix foafcorp: <http://xmlns.com/foaf/corp#>\n" +
//                "prefix vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>\n" +
//                "prefix sec: <http://www.rdfabout.com/rdf/schema/ussec/>\n" +
//                "prefix id: <http://www.rdfabout.com/rdf/usgov/sec/id/>\n" +
//                "select ?x\n" +
//                "where {\n" +
//                "    ?x rdf:type foaf:Person.\n" +
//                "    ?x vcard:ADR ?address.\n" +
//                "}";

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

            final TupleQuery tupleQuery1234 = con1234.prepareTupleQuery(QueryLanguage.SPARQL, query);
//            final TupleQuery tupleQuery1234 = clusterCon1234.prepareTupleQuery(QueryLanguage.SPARQL, query);

//            final TupleQueryResult result12 = tupleQuery12.evaluate();
            final TupleQueryResult result1234 = tupleQuery1234.evaluate();
//            final TupleQueryResult result1234 = tupleQuery1234.evaluate();

            final long end = System.currentTimeMillis();
            log.info("Execution time: " + (end - start));
            BindingSet bindingSet = null;
            int count = 0;

//            while (result1234.hasNext()) {
//                bindingSet = result1234.next();
//                final Value valueOfX = bindingSet.getValue("x");
//                log.info(valueOfX);
//            }
//
//            while (result12.hasNext()) {
//                bindingSet = result12.next();
//                final Value valueOfX = bindingSet.getValue("x");
//                log.info(valueOfX);
//            }

            while (result1234.hasNext()) {
                bindingSet = result1234.next();
                final Value valueOfX = bindingSet.getValue("X");
                final Value valueOfY = bindingSet.getValue("Y");
                final Value valueOfZ = bindingSet.getValue("Z");
                log.info("x: " + valueOfX + "\ty: " + valueOfY + "\tz: " + valueOfZ);
                count++;
            }

            log.info("result size: " + count);
//            TupleQuery tupleQuery2 = con.prepareTupleQuery(QueryLanguage.SPARQL,
//                      query2);
//            tupleQuery2.evaluate(resultHandler2);
//            log.info("Result count : " + resultHandler2.getCount());
//            sailRepo1234.shutDown();
//            sailRepo12.shutDown();
//            sailRepo34.shutDown();
            repo1.shutDown();
//            repo2.shutDown();
//            repo3.shutDown();
//            repo4.shutDown();
        } finally {
//            if (con12 != null) {
//                con12.close();
//            }
            if (con1234 != null) {
                con1234.close();
            }
        }
    }
}