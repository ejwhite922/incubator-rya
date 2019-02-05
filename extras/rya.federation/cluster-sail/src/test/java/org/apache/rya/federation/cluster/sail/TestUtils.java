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

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.hadoop.io.Text;
import org.apache.rya.api.RdfCloudTripleStoreConfiguration;
import org.apache.rya.api.persist.RyaDAOException;
import org.apache.rya.federation.cluster.sail.overlap.AccumuloOverlapList;
import org.apache.rya.rdftriplestore.RyaSailRepository;
import org.apache.rya.rdftriplestore.inference.InferenceEngineException;
import org.apache.rya.sail.config.RyaSailFactory;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.Iterations;

/**
 * Utility methods and constant used for testing.
 */
public final class TestUtils {
    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);

    private static final ValueFactory VF = ValueFactoryImpl.getInstance();

    private static final String TEST_FILE_DIR = "src/test/resources/rdf_format_files/";

    public static final File LUBM_FILE_0 = Paths.get(TEST_FILE_DIR + "LUBM_1-0.nt").toFile();
    public static final File LUBM_FILE_1 = Paths.get(TEST_FILE_DIR + "LUBM_1-1.nt").toFile();
    public static final File LUBM_FILE_2 = Paths.get(TEST_FILE_DIR + "LUBM_1-2.nt").toFile();
    public static final File LUBM_FILE_3 = Paths.get(TEST_FILE_DIR + "LUBM_1-3.nt").toFile();

    /**
     * Private constructor to prevent instantiation.
     */
    private TestUtils() {
    }

    /**
     * Creates a new {@link BatchWriter} from the specified {@link Connector}
     * and {@code tableName}.
     * @param conn the {@link Connector}. (not null)
     * @param tableName the name of the Accumulo table. (not null)
     * @return the created {@link BatchWriter}.
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableNotFoundException
     */
    private static BatchWriter createWriter(final Connector conn, final String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        requireNonNull(conn);
        requireNonNull(tableName);
        final long memBuf = 1000000L; // bytes to store before sending a batch
        final long timeout = 1000L; // milliseconds to wait before sending
        final int numThreads = 10;
        final BatchWriterConfig config = new BatchWriterConfig();
        config.setMaxMemory(memBuf);
        config.setTimeout(timeout, TimeUnit.MILLISECONDS);
        config.setMaxWriteThreads(numThreads);
        final BatchWriter writer = conn.createBatchWriter(tableName, config);
        return writer;
    }

    /**
     *
     * @param uri
     * @param conn
     * @param tableName
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableNotFoundException
     */
    public static void addURI(final String uri, final Connector conn, final String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        addURIs(Collections.singleton(uri), conn, tableName);
    }

    /**
     *
     * @param uris
     * @param conn
     * @param tableName
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws TableNotFoundException
     */
    public static void addURIs(final Collection<String> uris, final Connector conn, final String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException {
        BatchWriter writer = null;
        try {
            writer = createWriter(conn, tableName);

            for (final String uri : uris) {
                final Text rowID = new Text(uri);
                final Text colFam = new Text("   ");
                final Text colQual = new Text("   ");
//                final ColumnVisibility colVis = new ColumnVisibility("public");
                final long timestamp = System.currentTimeMillis();

                final Value tempValue = new Value("2".getBytes(StandardCharsets.UTF_8));

                final Mutation mutation = new Mutation(rowID);
                mutation.put(colFam, colQual, timestamp, tempValue);
                writer.addMutation(mutation);
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Queries the connection with the specified SPARQL and prints the binding
     * set results along with the total count and execution time.
     * @param conn the {@link RepositoryConnection}. (not null)
     * @param query the SPARQL query. (not null)
     * @return the number of binding sets returned from the query.
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws TupleQueryResultHandlerException
     * @throws QueryEvaluationException
     */
    public static int performQuery(final RepositoryConnection conn, final String query) throws RepositoryException, MalformedQueryException, TupleQueryResultHandlerException, QueryEvaluationException {
        requireNonNull(conn);
        requireNonNull(query);

        log.info("Performing Query:\n" + query);

        final long start = System.currentTimeMillis();
        final CountingResultHandler resultHandler = new CountingResultHandler();
        final TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
        tupleQuery.evaluate(resultHandler);
        final long end = System.currentTimeMillis();

        final String timeElapsedFormatted = getTimeElapsed(start, end);
        log.info("Query Execution Time: " + timeElapsedFormatted);
        log.info("Query Result Count : " + resultHandler.getCount());

        return resultHandler.getCount();
    }

    /**
     * Creates a formatted string of the time elapsed between the start and end
     * times.
     * @param start the start time (in milliseconds)
     * @param end the end time (in milliseconds)
     * @return the formatted time elapsed string.
     */
    public static String getTimeElapsed(final long start, final long end) {
        final String timeElapsedFormatted = DurationFormatUtils.formatDuration(end - start, "mm 'mins' ss.S 'secs'");
        return timeElapsedFormatted;
    }

    /**
     * Closes the {@link RepositoryConnection}.
     * @param conn the {@link RepositoryConnection}.
     */
    public static void closeConnection(final RepositoryConnection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (final RepositoryException e) {
                log.error("Error closing RepositoryConnection", e);
            }
        }
    }

    /**
     * Shuts the repository down, releasing any resources that it keeps hold of.
     * Once shut down, the repository can no longer be used until it is
     * re-initialized.
     * @param repo the {@link Repository} to close.
     */
    public static void closeRepository(final Repository repo) {
        if (repo != null && repo.isInitialized()) {
            try {
                repo.shutDown();
            } catch (final RepositoryException e) {
                log.error("Error shutting down Repository", e);
            }
        }
    }

    /**
     * Prints out the {@link OverlapList}'s inner iterator using a
     * {@link Scanner} if it's an Accumulo based overlap list..
     * @param overlapList the {@link OverlapList}. (not null)
     * @throws TableNotFoundException
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     */
    public static void printOverlapScanner(final OverlapList overlapList) throws TableNotFoundException, AccumuloException, AccumuloSecurityException {
        requireNonNull(overlapList);
        if (overlapList instanceof AccumuloOverlapList) {
            final Scanner scanner = ((AccumuloOverlapList) overlapList).createScanner();
            final Iterator<Entry<Key, Value>> iterator = scanner.iterator();

            while (iterator.hasNext()) {
                final Entry<Key, Value> entry = iterator.next();
                final Key key = entry.getKey();
                final Value value = entry.getValue();
                log.info(key.getRow() + " ==> " + value.toString());
            }
        }
    }

    /**
     * Adds statements to the repository.
     * @param repository the {@link Repository}. (not null)
     * @param statements the {@link List} of {@link Statement}s to add.
     * (not null)
     * @throws RepositoryException
     */
    public static void addStatements(final Repository repository, final List<Statement> statements) throws RepositoryException {
        requireNonNull(repository);
        requireNonNull(statements);
        RepositoryConnection conn = null;
        try {
            conn = repository.getConnection();
            conn.add(statements);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Creates a {@link RyaSailRepository} from the specified configuration.
     * @param config the {@link RdfCloudTripleStoreConfiguration}. (not null)
     * @return the {@link RyaSailRepository}.
     * @throws SailException
     * @throws AccumuloException
     * @throws AccumuloSecurityException
     * @throws RyaDAOException
     * @throws InferenceEngineException
     */
    public static Repository createRyaSailRepository(final RdfCloudTripleStoreConfiguration config) throws SailException, AccumuloException, AccumuloSecurityException, RyaDAOException, InferenceEngineException {
        final Sail sail = RyaSailFactory.getInstance(config);
        final Repository repository = new RyaSailRepository(sail);
        return repository;
    }

    /**
     * Creates a {@link Statement} out of the subject, predicate, and object.
     * @param subject the subject of the triple.
     * @param predicate the predicate of the triple.
     * @param object the object of the triple.
     * @return the {@link Statement}.
     */
    public static Statement createStatement(final String subject, final String predicate, final String object) {
        return VF.createStatement(VF.createURI(subject), VF.createURI(predicate), VF.createURI(object));
    }

    /**
     * Gets all the statements from the specified repository including inferred
     * ones.
     * @param repository the {@link Repository}. (not null)
     * @return the {@link List} of {@link Statement}s.
     * @throws RepositoryException
     */
    public static List<Statement> getAllStatements(final Repository repository) throws RepositoryException {
        requireNonNull(repository);
        RepositoryConnection conn = null;
        try {
            conn = repository.getConnection();

            final RepositoryResult<Statement> result = conn.getStatements(null,  null,  null, true);
            final List<Statement> statements = Iterations.asList(result);
            return statements;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Adds triples to the connection from the specified triples file in
     * supplied RDF format.
     * @param conn the {@link RepositoryConnection}. (not null)
     * @param triplesFile the triples {@link File}. (not null)
     * @param rdfFormat the {@link RDFFormat}. (not null)
     * @throws RDFParseException
     * @throws RepositoryException
     * @throws IOException
     */
    public static void addTriples(final RepositoryConnection conn, final File triplesFile, final RDFFormat rdfFormat) throws RDFParseException, RepositoryException, IOException {
        requireNonNull(conn);
        requireNonNull(triplesFile);
        requireNonNull(rdfFormat);
        conn.begin();
        conn.add(triplesFile, "", rdfFormat);
        conn.commit();
    }

    /**
     * Adds triples to the repository from the specified triples file in
     * N-Triples format.
     * @param repository the {@link Repository}. (not null)
     * @param triplesFile the triples {@link File}. (not null)
     * @throws RDFParseException
     * @throws RepositoryException
     * @throws IOException
     */
    public static void addTriples(final Repository repository, final File triplesFile) throws RDFParseException, RepositoryException, IOException {
        addTriples(repository, triplesFile, RDFFormat.NTRIPLES);
    }

    /**
     * Adds triples to the repository from the specified triples file in
     * supplied RDF format.
     * @param repository the {@link Repository}. (not null)
     * @param triplesFile the triples {@link File}. (not null)
     * @param rdfFormat the {@link RDFFormat}. (not null)
     * @throws RDFParseException
     * @throws RepositoryException
     * @throws IOException
     */
    public static void addTriples(final Repository repository, final File triplesFile, final RDFFormat rdfFormat) throws RDFParseException, RepositoryException, IOException {
        requireNonNull(repository);
        requireNonNull(triplesFile);
        requireNonNull(rdfFormat);
        RepositoryConnection conn = null;
        try {
            conn = repository.getConnection();
            addTriples(conn, triplesFile, rdfFormat);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}