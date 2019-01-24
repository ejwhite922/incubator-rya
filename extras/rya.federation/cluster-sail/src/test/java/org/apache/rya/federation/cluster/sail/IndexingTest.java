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

import java.io.File;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class IndexingTest {
    private static final Logger log = LoggerFactory.getLogger(IndexingTest.class);

    private static final String SESAME_SERVER = "http://192.168.33.20:8080/openrdf-sesame";
    private static final String REPOSITORY_ID = "RyaAccumulo_2";

    public static void main(final String[] args) throws Exception {
        log.info("Starting " + IndexingTest.class.getSimpleName() + "...");

        final Repository repo = new HTTPRepository(SESAME_SERVER, REPOSITORY_ID);
        repo.initialize();

        final File file = new File("/home/vagrant/Downloads/university_1/University1_0.daml");
        final String baseURI = "file://University1_0.daml";

        RepositoryConnection con = null;

        try {
            log.info("Connecting to SailRepository.");

            con = repo.getConnection();

            con.add(file, baseURI, RDFFormat.RDFXML);

            final String query =
                "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix daml: <http://www.daml.org/2001/03/daml+oil#>\n" +
                "prefix ub: <https://rya.apache.org#>\n" +
                "SELECT ?X ?Y ?Z\n" +
                "WHERE {\n" +
                "    ?Z rdf:type ub:Department .\n" +
                "    ?Z ub:subOrganizationOf ?W .\n" +
                "    ?Y rdf:type ub:University .\n" +
                "    ?X ub:memberOf ?Z .\n" +
                "    ?X ub:undergraduateDegreeFrom ?Y .\n" +
                "    ?X rdf:type ub:GraduateStudent .\n" +
                "}";

            final TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
            final TupleQueryResult result = tupleQuery.evaluate();

            BindingSet bindingSet = null;
            int count = 0;

            while(result.hasNext()){
                bindingSet = result.next();
                final Value valueOfX = bindingSet.getValue("X");
                final Value valueOfY = bindingSet.getValue("Y");
                final Value valueOfZ = bindingSet.getValue("Z");
                count++;
                System.out.println("X: " + valueOfX);
                System.out.println("Y: " + valueOfY);
                System.out.println("Z: " + valueOfZ);
                System.out.println(bindingSet);
            }
            System.out.println("result size: " + count);
        } finally {
            closeConnection(con);
            closeRepository(repo);
        }
    }
}