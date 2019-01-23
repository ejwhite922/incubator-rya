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
//        final String log4jConfPath = "/home/vagrant/accumulo-1.7.1/conf/log4j.properties";
//        PropertyConfigurator.configure(log4jConfPath);
        log.info("Starting " + QueryTest.class.getSimpleName() + "...");
        // Repository 1
        final Repository repo1 = new HTTPRepository(SESAME_SERVER_1, REPOSITORY_ID_1);
        repo1.initialize();

        RepositoryConnection con1 = null;

        try {
            log.info("Connecting to SailRepository.");

            con1 = repo1.getConnection();

            final long start = System.nanoTime();
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
            final TupleQuery tupleQuery12 = con1.prepareTupleQuery(QueryLanguage.SPARQL, query);
//            final TupleQuery tupleQuery34 = con34.prepareTupleQuery(QueryLanguage.SPARQL, query);
//            final TupleQuery tupleQuery1234 = clusterCon1234.prepareTupleQuery(QueryLanguage.SPARQL, query);

            final TupleQueryResult result12 = tupleQuery12.evaluate();
//            final TupleQueryResult result34 = tupleQuery34.evaluate();
//            final TupleQueryResult result1234 = tupleQuery1234.evaluate();

            final long end = System.nanoTime();

            log.info("" + (end - start));
            BindingSet bindingSet = null;

//            while (result1234.hasNext()) {
//                bindingSet = result1234.next();
//                final Value valueOfX = bindingSet.getValue("x");
//                log.info(valueOfX);
//            }

            while (result12.hasNext()) {
                bindingSet = result12.next();
                final Value valueOfX = bindingSet.getValue("x");
                log.info("" + valueOfX);
            }

//            while (result34.hasNext()) {
//                bindingSet = result34.next();
//                final Value valueOfX = bindingSet.getValue("x");
//                log.info(valueOfX);
//            }


//            TupleQuery tupleQuery_2 = con.prepareTupleQuery(QueryLanguage.SPARQL,
//                   query_2);
//            tupleQuery_2.evaluate(resultHandler_2);
//            log.info("Result count : " + resultHandler_2.getCount());
            repo1.shutDown();
        } finally {
            if (con1 != null) {
                con1.close();
            }
        }
    }
}