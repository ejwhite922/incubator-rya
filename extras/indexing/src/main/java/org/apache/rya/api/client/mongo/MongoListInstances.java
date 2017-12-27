/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rya.api.client.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.rya.api.client.ListInstances;
import org.apache.rya.api.client.RyaClientException;
import org.apache.rya.mongodb.instance.MongoRyaInstanceDetailsRepository;

import com.mongodb.MongoClient;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * An Mongo implementation of the {@link ListInstances} command.
 */
@DefaultAnnotation(NonNull.class)
public class MongoListInstances extends MongoCommand implements ListInstances {
    /**
     * Constructs an instance of {@link MongoListInstances}.
     *
     * @param connectionDetails
     *            - Details about the values that were used to create the connector to the cluster. (not null)
     * @param client
     *            - Provides programatic access to the instance of Mongo
     *            that hosts Rya instance. (not null)
     */
    public MongoListInstances(final MongoConnectionDetails connectionDetails, final MongoClient client) {
        super(connectionDetails, client);
    }

    @Override
    public List<String> listInstances() throws RyaClientException {
        final MongoClient client = super.getClient();
        final List<String> dbNames = client.getDatabaseNames();
        final List<String> ryaInstances = new ArrayList<>();
        for (final String db : dbNames) {
            final Set<String> collNames = client.getDB(db).getCollectionNames();
            for (final String coll : collNames) {
                if (coll.equals(MongoRyaInstanceDetailsRepository.INSTANCE_DETAILS_COLLECTION_NAME)) {
                    ryaInstances.add(db);
                    break;
                }
            }
        }
        return ryaInstances;
    }
}