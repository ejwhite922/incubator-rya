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

import static org.apache.rya.federation.cluster.sail.TestUtils.addTriples;
import static org.apache.rya.federation.cluster.sail.TestUtils.closeRepository;
import static org.apache.rya.federation.cluster.sail.TestUtils.createAccumuloRdfConfiguration;
import static org.apache.rya.federation.cluster.sail.TestUtils.createRyaSailRepository;

import org.openrdf.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds LUBM data to each repository.
 */
public class InsertDataTest {
    private static final Logger log = LoggerFactory.getLogger(InsertDataTest.class);

    private static final String REPOSITORY_IP_1 = "172.17.0.2";
    private static final String REPOSITORY_IP_2 = "172.17.0.3";
    private static final String REPOSITORY_IP_3 = "172.17.0.4";
    private static final String REPOSITORY_IP_4 = "172.17.0.5";

    public static void main(final String[] args) throws Exception {
        log.info("Starting " + InsertDataTest.class.getSimpleName() + "...");

        Repository repo1 = null;
        Repository repo2 = null;
        Repository repo3 = null;
        Repository repo4 = null;
        try {
            repo1 = createRyaSailRepository(createAccumuloRdfConfiguration(REPOSITORY_IP_1));
            addTriples(repo1, TestUtils.LUBM_FILE_1);

            repo2 = createRyaSailRepository(createAccumuloRdfConfiguration(REPOSITORY_IP_2));
            addTriples(repo2, TestUtils.LUBM_FILE_2);

            repo3 = createRyaSailRepository(createAccumuloRdfConfiguration(REPOSITORY_IP_3));
            addTriples(repo3, TestUtils.LUBM_FILE_3);

            repo4 = createRyaSailRepository(createAccumuloRdfConfiguration(REPOSITORY_IP_4));
            addTriples(repo4, TestUtils.LUBM_FILE_4);
        } finally {
            closeRepository(repo1);
            closeRepository(repo2);
            closeRepository(repo3);
            closeRepository(repo4);
        }

        log.info("Finished " + InsertDataTest.class.getSimpleName());
    }
}