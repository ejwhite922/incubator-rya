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

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests repository query.
 */
public class QueryTest {
    private static final Logger log = LoggerFactory.getLogger(QueryTest.class);

//    private static final boolean USE_MOCK_INSTANCE = false;
//    private static final boolean PRINT_QUERIES = false;
//    private static final String INSTANCE = "dev";
//    private static final String RYA_TABLE_PREFIX = "rya_";
//    private static final String AUTHS = "";
    // The second VM's IP is 192.168.33.20
    private static final String SESAME_SERVER_1 = "http://192.168.33.10:8080/openrdf-sesame";

    private static final String REPOSITORY_ID_1 = "large";

    public static void main(final String[] args) throws Exception {
        log.info("Starting " + QueryTest.class.getSimpleName() + "...");

        // Repository 1
        final Repository repo1 = new HTTPRepository(SESAME_SERVER_1, REPOSITORY_ID_1);
        repo1.initialize();

        RepositoryConnection con1 = null;

        try {
            log.info("Connecting to SailRepository.");

            con1 = repo1.getConnection();

            // Execute query
            final String query =
                "PREFIX code:<http://telegraphis.net/ontology/measurement/code#>\n" +
                "PREFIX geographis:<http://telegraphis.net/ontology/geography/geography#>\n" +
                "PREFIX money:<http://telegraphis.net/ontology/money/money#>\n" +
                "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" +
                "PREFIX gn:<http://www.geonames.org/ontology#>\n" +
                "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "select ?x\n" +
                "where {\n" +
                "    ?x geographis:capital ?capital .\n" +
                "    ?x geographis:currency ?currency .\n" +
                "}";

            performQuery(con1, query);
        } finally {
            closeConnection(con1);
            closeRepository(repo1);
        }

        log.info("Finished " + QueryTest.class.getSimpleName());
    }
}