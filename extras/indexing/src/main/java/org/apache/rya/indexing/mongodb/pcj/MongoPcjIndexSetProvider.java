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
package org.apache.rya.indexing.mongodb.pcj;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;

import org.apache.rya.api.instance.RyaDetailsRepository;
import org.apache.rya.api.instance.RyaDetailsRepository.RyaDetailsRepositoryException;
import org.apache.rya.indexing.external.tupleSet.ExternalTupleSet;
import org.apache.rya.indexing.pcj.matching.provider.AbstractPcjIndexSetProvider;
import org.apache.rya.indexing.pcj.storage.PrecomputedJoinStorage;
import org.apache.rya.indexing.pcj.storage.PrecomputedJoinStorage.PCJStorageException;
import org.apache.rya.indexing.pcj.storage.mongo.MongoPcjDocuments;
import org.apache.rya.indexing.pcj.storage.mongo.MongoPcjStorage;
import org.apache.rya.mongodb.StatefulMongoDBRdfConfiguration;
import org.apache.rya.mongodb.instance.MongoRyaInstanceDetailsRepository;
import org.openrdf.query.MalformedQueryException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.MongoClient;

/**
 * Implementation of {@link AbstractPcjIndexSetProvider} for MongoDB.
 */
public class MongoPcjIndexSetProvider extends AbstractPcjIndexSetProvider {
    /**
     * Creates a new {@link MongoPcjIndexSetProvider}.
     * @param conf - The configuration for this provider. (not null)
     */
    public MongoPcjIndexSetProvider(final StatefulMongoDBRdfConfiguration conf) {
        super(conf);
    }

    /**
     * Creates a new {@link MongoPcjIndexSetProvider}.
     * @param conf - The configuration for this provider.
     * @param indices - The predefined indicies on this provider.
     * @param client - The {@link MongoClient} used to connect to mongo.
     */
    public MongoPcjIndexSetProvider(final StatefulMongoDBRdfConfiguration conf, final List<ExternalTupleSet> indices) {
        super(conf, indices);
    }

    @Override
    protected List<ExternalTupleSet> getIndices() throws PcjIndexSetException {
        requireNonNull(conf);
        try {
            final StatefulMongoDBRdfConfiguration mongoConf = (StatefulMongoDBRdfConfiguration) conf;
            final MongoClient client = mongoConf.getMongoClient();
            final MongoPcjDocuments pcjDocs = new MongoPcjDocuments(client, mongoConf.getRyaInstanceName());
            List<String> tables = null;

            tables = mongoConf.getPcjTables();
            // this maps associates pcj table name with pcj sparql query
            final Map<String, String> indexTables = Maps.newLinkedHashMap();

            try(final PrecomputedJoinStorage storage = new MongoPcjStorage(client, mongoConf.getRyaInstanceName())) {

                final boolean tablesProvided = tables != null && !tables.isEmpty();

                if (tablesProvided) {
                    // if tables provided, associate table name with sparql
                    for (final String table : tables) {
                        indexTables.put(table, storage.getPcjMetadata(table).getSparql());
                    }
                } else if (hasRyaDetails()) {
                    // If this is a newer install of Rya, and it has PCJ Details,
                    // then
                    // use those.
                    final List<String> ids = storage.listPcjs();
                    for (final String id : ids) {
                        indexTables.put(id, storage.getPcjMetadata(id).getSparql());
                    }
                } else {
                    // Otherwise figure it out by getting document IDs.
                    tables = pcjDocs.listPcjDocuments();
                    for (final String table : tables) {
                        if (table.startsWith("INDEX")) {
                            indexTables.put(table, pcjDocs.getPcjMetadata(table).getSparql());
                        }
                    }
                }
            }

            final List<ExternalTupleSet> index = Lists.newArrayList();
            if (indexTables.isEmpty()) {
                log.info("No Index found");
            } else {
                for (final String table : indexTables.keySet()) {
                    final String indexSparqlString = indexTables.get(table);
                    index.add(new PcjQueryNode(indexSparqlString, table, pcjDocs));
                }
            }
            return index;
        } catch (final PCJStorageException | MalformedQueryException e) {
            throw new PcjIndexSetException("Failed to get indicies for this PCJ index.", e);
        }
    }

    private boolean hasRyaDetails() {
        final StatefulMongoDBRdfConfiguration mongoConf = (StatefulMongoDBRdfConfiguration) conf;
        final RyaDetailsRepository detailsRepo = new MongoRyaInstanceDetailsRepository(mongoConf.getMongoClient(), mongoConf.getRyaInstanceName());
        try {
            detailsRepo.getRyaInstanceDetails();
            return true;
        } catch (final RyaDetailsRepositoryException e) {
            return false;
        }
    }
}
