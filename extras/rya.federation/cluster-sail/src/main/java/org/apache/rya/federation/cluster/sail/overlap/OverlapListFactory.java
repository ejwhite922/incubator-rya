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

import org.apache.rya.federation.cluster.sail.OverlapList;
import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.apache.rya.federation.cluster.sail.exception.OverlapListException;

/**
 * Factory for creating {@link OverlapList} types.
 */
public class OverlapListFactory {
    /**
     * Private constructor to prevent instantiation.
     */
    private OverlapListFactory() {
    }

    /**
     * Holds the singleton instance.
     */
    private static class InstanceHolder {
        private static OverlapListFactory INSTANCE = new OverlapListFactory();
    }

    /**
     * @return the singleton instance of {@link OverlapListFactory}.
     */
    public static OverlapListFactory getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Creates the overlap list based on the database type name specified in the
     * config.
     * @param config the {@link ClusterFederationConfig}. (not null)
     * @return the {@link OverlapList}.
     * @throws OverlapListException
     */
    public OverlapList createOverlapList(final ClusterFederationConfig config) throws OverlapListException {
        final OverlapListDbType overlapListDbType = requireNonNull(config).getOverlapListDbType();

        if (overlapListDbType != null) {
            return overlapListDbType.createOverlapList(config);
        } else {
            throw new OverlapListException("Unsupported overlap list database type: " + overlapListDbType);
        }
    }
}