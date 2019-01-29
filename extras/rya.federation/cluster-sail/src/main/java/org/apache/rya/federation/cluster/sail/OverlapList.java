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

import java.util.Set;

import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.apache.rya.federation.cluster.sail.exception.OverlapListException;

/**
 * The overlap list is the set of subject or object values URIs at each cluster
 * center that appear as subjects or objects in other clusters too.
 */
public interface OverlapList extends AutoCloseable {
    /**
     * Setup the Overlap list for use.
     * @throws OverlapListException
     */
    public void setup() throws OverlapListException;

    /**
     * @return {@code true} if the database for the overlap list is initialized.
     * {@code false} otherwise.
     */
    public boolean isInitialized();

    /**
     * @return the {@link ClusterFederationConfig}.
     */
    public ClusterFederationConfig getConfig();

    /**
     * Insert data into the list.
     * @param value the value to add.
     * @throws OverlapListException
     */
    public void addData(final String value) throws OverlapListException;

    /**
     * Deletes data from the list.
     * @param value the value to delete.
     * @throws OverlapListException
     */
    public void deleteData(final String value) throws OverlapListException;

    /**
     * @return the {@link Set} of overlapping items.
     * @throws OverlapListException
     */
    public Set<String> getOverlaps() throws OverlapListException;
}