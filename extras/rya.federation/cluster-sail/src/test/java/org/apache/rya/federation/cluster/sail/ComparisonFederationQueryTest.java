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

/**
 *
 * @author vagrant
 */
public class ComparisonFederationQueryTest {
    private static final Logger log = LoggerFactory.getLogger(ComparisonFederationQueryTest.class);

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

//    private static final String REPOSITORY_ID_1 = "RyaAccumulo_1_sec";
//    private static final String REPOSITORY_ID_2 = "RyaAccumulo_2_sec";
//    private static final String REPOSITORY_ID_3 = "RyaAccumulo_3_sec";
//    private static final String REPOSITORY_ID_4 = "RyaAccumulo_4_sec";
//    private static final String REPOSITORY_ID_1234 = "Federation1234";
    private static final String REPOSITORY_ID_123456 = "Federation123456";

    public static void main(final String[] args) throws Exception {
        log.info("Starting " + ComparisonFederationQueryTest.class.getSimpleName() + "...");
//        // Repository 1
//        final Repository repo1 = new HTTPRepository(SESAME_SERVER_1, REPOSITORY_ID_1);
//        repo1.initialize();
//
//        // Repository 2
//        final Repository repo2 = new HTTPRepository(SESAME_SERVER_2, REPOSITORY_ID_2);
//        repo2.initialize();
//
//        // Repository 3
//        final Repository repo3 = new HTTPRepository(SESAME_SERVER_3, REPOSITORY_ID_3);
//        repo3.initialize();
//
//        // Repository 4
//        final Repository repo4 = new HTTPRepository(SESAME_SERVER_4, REPOSITORY_ID_4);
//        repo4.initialize();
//
//        final Repository repo1234 = new HTTPRepository(SESAME_SERVER_2, REPOSITORY_ID_1234);
//        final RepositoryConnection con1234 = null;

        final Repository repo123456 = new HTTPRepository(SESAME_SERVER_2, REPOSITORY_ID_123456);
        RepositoryConnection con123456 = null;

//        repo1234.initialize();
        repo123456.initialize();
        try {
            log.info("Connecting to SailRepository.");

//            // Federation of 1, 2, 3, 4
//            final Federation federation1234 = new Federation();
//            federation1234.addMember(repo1);
//            federation1234.addMember(repo2);
//            federation1234.addMember(repo3);
//            federation1234.addMember(repo4);
//
//            con1234 = repo1234.getConnection();
            con123456 = repo123456.getConnection();

            final long start = System.currentTimeMillis();
            // Execute query
            final String query =
                "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix daml: <http://www.daml.org/2001/03/daml+oil#>\n" +
                "prefix ub: <https://rya.apache.org#>\n" +
                "SELECT ?X ?Y ?Z\n" +
                "WHERE {\n" +
                "    ?Z ub:subOrganizationOf ?Y .\n" +
                "    ?X ub:memberOf ?Z .\n" +
                "    ?X ub:undergraduateDegreeFrom ?Y .\n" +
                "}";

            final TupleQuery tupleQuery = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query);

            final TupleQueryResult result = tupleQuery.evaluate();

            final long end = System.currentTimeMillis();

            log.info("" + (end - start));

            long count = 0;

            BindingSet bindingSet = null;

            while (result.hasNext()) {
                bindingSet = result.next();
                final Value valueOfX = bindingSet.getValue("X");
                log.trace("X: " + valueOfX);
                count++;
            }

            log.info("result size: " + count);

            repo123456.shutDown();
        } finally {
            if (con123456 != null) {
                con123456.close();
            }
        }
    }
}