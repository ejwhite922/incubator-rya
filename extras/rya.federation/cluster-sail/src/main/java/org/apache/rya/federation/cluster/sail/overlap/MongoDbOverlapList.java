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
package org.apache.rya.federation.cluster.sail.overlap;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.rya.federation.cluster.sail.OverlapList;
import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.apache.rya.federation.cluster.sail.exception.OverlapListException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

/**
 * The overlap list is the set of subject or object values URIs at each cluster
 * center that appear as subjects or objects in other clusters too.
 */
public class MongoDbOverlapList implements OverlapList {
    private static final Logger log = LoggerFactory.getLogger(MongoDbOverlapList.class);

    private static final String KEY = "overlap_list_item";

    private final String instanceName;
    private final ClusterFederationConfig config;

    private MongoClient client;
    private MongoDatabase db;
    private MongoCollection<Document> collection;
    private String collectionName;
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;

    private final AtomicBoolean isInitialized = new AtomicBoolean();

    /**
     * Creates a new instance of {@link MongoDbOverlapList}.
     * @param config the {@link ClusterFederationConfig}. (not null)
     */
    public MongoDbOverlapList(final ClusterFederationConfig config) {
        this.config = requireNonNull(config);

        this.instanceName = config.getInstanceName();
        this.collectionName = StringUtils.defaultString(config.getTableName(), OverlapList.DEFAULT_OVERLAP_LIST_TABLE_NAME);
        this.hostname = config.getMongoHostname();
        this.port = config.getMongoPort();
        this.username = config.getUsername();
        this.password = config.getPassword();
    }

    /**
     * Selects the specified collection. If the collection does not exist, then
     * it is created.
     * @param collectionName the name of the collection to select. (not null)
     */
    public void selectCollection(final String collectionName) {
        requireNonNull(collectionName);
        this.collection = db.getCollection(collectionName);
        this.collectionName = collectionName;
        createIndex();
    }

    private void createIndex() {
        final Document index = new Document(KEY, 1);
        if (!hasIndex(index)) {
            collection.createIndex(index, new IndexOptions().unique(true));
        }
    }

    private boolean hasIndex(final Document index) {
        boolean hasIndex = false;
        final List<Document> indexes = collection.listIndexes().into(new ArrayList<>());
        for (final Document indexDoc : indexes) {
            final Document keyDoc = indexDoc.get("key", Document.class);
            if (keyDoc != null && keyDoc.equals(index)) {
                hasIndex = true;
                break;
            }
        }
        return hasIndex;
    }


    /**
     * Creates the MongoDB client.
     * @param username a valid MongoDB user.
     * @param password a UTF-8 encoded password.
     */
    public void createClient(final String username, final String password) {
        this.client = new MongoClient(hostname, port);
        this.db = client.getDatabase(instanceName);
    }

    @Override
    public void addData(final String value) throws OverlapListException {
        try {
            final Document document = new Document(KEY, value);
            collection.insertOne(document);
        } catch (final MongoException e) {
            if (ErrorCategory.fromErrorCode(e.getCode()) == ErrorCategory.DUPLICATE_KEY) {
                // Absorb duplicate key exception
                log.trace(e.getMessage());
            } else {
                throw new OverlapListException("Failed to add data", e);
            }
        }
    }

    @Override
    public void deleteData(final String value) throws OverlapListException {
        try {
            final Document document = new Document(KEY, value);
            collection.deleteMany(document);
        } catch (final MongoException e) {
            throw new OverlapListException("Failed to delete data", e);
        }
    }

    @Override
    public void setup() throws OverlapListException {
        try {
            if (!isInitialized.get()) {
                createClient(username, password);
                selectCollection(collectionName);
                isInitialized.set(true);
            }
        } catch (final Exception e) {
            throw new OverlapListException("Failed to setup OverlapList", e);
        }
    }

    @Override
    public boolean isInitialized() {
        return isInitialized.get();
    }

    @Override
    public ClusterFederationConfig getConfig() {
        return config;
    }

    @Override
    public Set<String> getOverlaps() throws OverlapListException {
        final Set<String> overlaps = new HashSet<>();
        try {
            final FindIterable<Document> documents = collection.find();
            final MongoCursor<Document> cursor = documents.iterator();
            while (cursor.hasNext()) {
                final Document document = cursor.next();
                final String value = document.getString(KEY);
                overlaps.add(value);
            }
        } catch (final Exception e) {
            throw new OverlapListException("Failed to create overlap list iterator", e);
        }
        return overlaps;
    }

    @Override
    public void close() throws Exception {
        isInitialized.set(false);
    }
}