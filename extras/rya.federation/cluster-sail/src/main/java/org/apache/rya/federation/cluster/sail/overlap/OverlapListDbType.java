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

import org.apache.commons.lang3.StringUtils;
import org.apache.rya.federation.cluster.sail.OverlapList;
import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;

/**
 * Enumerates the support databases types used for storing the overlap list.
 */
public enum OverlapListDbType {
    /**
     * An Accumulo based overlap list database.
     */
    ACCUMULO("Accumulo") {
        @Override
        public OverlapList createOverlapList(final ClusterFederationConfig config) {
            return new AccumuloOverlapList(config);
        }
    },
    /**
     * A MongoDB based overlap list database.
     */
    MONGO_DB("MongoDB") {
        @Override
        public OverlapList createOverlapList(final ClusterFederationConfig config) {
            return new MongoDbOverlapList(config);
        }
    };

    private final String name;

    /**
     * Creates a new {@link OverlapListDbType}.
     * @param name the name of the database. (not null)
     */
    private OverlapListDbType(final String name) {
        this.name = requireNonNull(name);
    }

    /**
     * @return the name of the database.
     */
    public String getName() {
        return name;
    }

    /**
     * Creates the overlap list that corresponds to this
     * {@link OverlapListDbType} enumeration.
     * @param config the {@link ClusterFederationConfig}. (not null)
     * @return the {@link OverlapList}.
     */
    public abstract OverlapList createOverlapList(final ClusterFederationConfig config);

    @Override
    public String toString() {
        return name;
    }

    /**
     * Finds the overlap list database type by name.
     * @param name the name to find.
     * @return the {@link OverlapListDbType} or {@code null} if none could be found.
     */
    public static OverlapListDbType fromName(final String name) {
        for (final OverlapListDbType overlapListDbType : OverlapListDbType.values()) {
            if (StringUtils.equalsIgnoreCase(overlapListDbType.toString(), name)) {
                return overlapListDbType;
            }
        }
        return null;
    }
}