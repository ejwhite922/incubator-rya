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
package org.apache.rya.federation.cluster.runtime;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openrdf.OpenRDFException;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * Tests the methods of {@link RepositoryManagerClusterFederator}.
 */
public class RepositoryManagerClusterFederatorTest {
    private RepositoryManagerClusterFederator clusterFederator;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        final RepositoryManager manager = mock(RepositoryManager.class);
        final Repository system = mock(Repository.class);
        when(system.getValueFactory()).thenReturn(ValueFactoryImpl.getInstance());
        when(manager.getSystemRepository()).thenReturn(system);
        clusterFederator = new RepositoryManagerClusterFederator(manager);
    }

    @Test
    public final void testDirectRecursiveAddThrowsException() throws MalformedURLException, OpenRDFException {
        thrown.expect(is(instanceOf(RepositoryConfigException.class)));
        thrown.expectMessage(is(equalTo("A cluster federation member may not have the same ID as the federation.")));
        final String id = "clusterfedtest";
        clusterFederator.addFed(id, "Cluster Federation Test", Arrays.asList(new String[] { id, "ignore" }), true, false);
    }
}