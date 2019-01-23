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
package org.apache.rya.federation.cluster.sail.config;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author vagrant
 */

/*
 * Licensed to Aduna under one or more contributor license agreements.
 * See the NOTICE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD
 * License (the "License"); you may not use this file except in compliance
 * with the License. See the LICENSE.txt file distributed with this work
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */


import static org.openrdf.repository.config.RepositoryImplConfigBase.create;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfigBase;

/**
 * Lists the members of a federation and which properties describe a resource
 * subject in a unique member.
 */
public class ClusterFederationConfig extends SailImplConfigBase {

    /** http://www.openrdf.org/config/sail/federation# */
    public static final String NAMESPACE = "http://www.openrdf.org/config/sail/clusterfederation#";

    public static final URI MEMBER = new URIImpl(NAMESPACE + "member");

    /**
     * For all triples with a predicate in this space, the container RDF store
     * contains all triples with that subject and any predicate in this space.
     */
    public static final URI LOCALPROPERTYSPACE = new URIImpl(NAMESPACE // NOPMD
            + "localPropertySpace");

    /**
     * If no two members contain the same statement.
     */
    public static final URI DISTINCT = new URIImpl(NAMESPACE + "distinct");

    /**
     * If the federation should not try and add statements to its members.
     */
    public static final URI READ_ONLY = new URIImpl(NAMESPACE + "readOnly");

    /**
     * The accumulo instance name.
     */
    public static final URI INSTANCE_NAME = new URIImpl(NAMESPACE + "instanceName");

    /**
     * The accumulo table name.
     */
    public static final URI TABLE_NAME = new URIImpl(NAMESPACE + "tableName");

    /**
     * The zookeeper host server.
     */
    public static final URI ZK_SERVER = new URIImpl(NAMESPACE + "zkServer");

    /**
     * The accumulo username.
     */
    public static final URI USERNAME = new URIImpl(NAMESPACE + "username");

    /**
     * The accumulo user password.
     */
    public static final URI PASSWORD = new URIImpl(NAMESPACE + "password");

    private List<RepositoryImplConfig> members = new ArrayList<RepositoryImplConfig>();

    private final Set<String> localPropertySpace = new HashSet<String>(); // NOPMD

    private boolean distinct;

    private boolean readOnly;

    private String instanceName;

    private String tableName;

    private String zkServer;

    private String username;

    private String password;

    public List<RepositoryImplConfig> getMembers() {
        return members;
    }

    public void setMembers(final List<RepositoryImplConfig> members) {
        this.members = members;
    }

    public void addMember(final RepositoryImplConfig member) {
        members.add(member);
    }

    public Set<String> getLocalPropertySpace() {
        return localPropertySpace;
    }

    public void addLocalPropertySpace(final String localPropertySpace) { // NOPMD
        this.localPropertySpace.add(localPropertySpace);
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(final boolean disjoint) {
        this.distinct = disjoint;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * @return the accumulo instance name.
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * @param instanceName the accumulo instance name. (not null)
     */
    public void setInstanceName(final String instanceName) {
        this.instanceName = checkNotNull(instanceName);
    }
    /**
     * @return the accumulo table name.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the accumulo table name. (not null)
     */
    public void setTableName(final String tableName) {
        this.tableName = checkNotNull(tableName);
    }
    /**
     * @return the zookeeper host server.
     */
    public String getZkServer() {
        return zkServer;
    }

    /**
     * @param zkServer the zookeeper host server. (not null)
     */
    public void setZkServer(final String zkServers) {
        this.zkServer = checkNotNull(zkServers);
    }
    /**
     * @return the accumulo username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the accumulo username. (not null)
     */
    public void setUsername(final String username) {
        this.username = checkNotNull(username);
    }
    /**
     * @return the accumulo user password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the accumulo user password. (not null)
     */
    public void setPassword(final String password) {
        this.password = checkNotNull(password);
    }

    @Override
    public Resource export(final Graph model) {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        final Resource self = super.export(model);
        for (final RepositoryImplConfig member : getMembers()) {
            model.add(self, MEMBER, member.export(model));
        }
        for (final String space : getLocalPropertySpace()) {
            model.add(self, LOCALPROPERTYSPACE, valueFactory.createURI(space));
        }
        model.add(self, DISTINCT, valueFactory.createLiteral(distinct));
        model.add(self, READ_ONLY, valueFactory.createLiteral(readOnly));
        model.add(self, INSTANCE_NAME, valueFactory.createLiteral(instanceName));
        model.add(self, TABLE_NAME, valueFactory.createLiteral(tableName));
        model.add(self, ZK_SERVER, valueFactory.createLiteral(zkServer));
        model.add(self, USERNAME, valueFactory.createLiteral(username));
        model.add(self, PASSWORD, valueFactory.createLiteral(password));
        return self;
    }

    @Override
    public void parse(final Graph graph, final Resource implNode)
            throws SailConfigException {
        super.parse(graph, implNode);
        final LinkedHashModel model = new LinkedHashModel(graph);
        for (final Value member : model.filter(implNode, MEMBER, null).objects()) {
            try {
                addMember(create(graph, (Resource) member));
            } catch (final RepositoryConfigException e) {
                throw new SailConfigException(e);
            }
        }
        for (final Value space : model.filter(implNode, LOCALPROPERTYSPACE, null)
                .objects()) {
            addLocalPropertySpace(space.stringValue());
        }
        try {
            Literal bool = model.filter(implNode, DISTINCT, null)
                    .objectLiteral();
            if (bool != null && bool.booleanValue()) {
                distinct = true;
            }
            bool = model.filter(implNode, READ_ONLY, null).objectLiteral();
            if (bool != null && bool.booleanValue()) {
                readOnly = true;
            }
            final Literal instanceName = model.filter(implNode, INSTANCE_NAME, null)
                    .objectLiteral();
            if (instanceName != null) {
                this.instanceName = instanceName.stringValue();
            }
            final Literal tableName = model.filter(implNode, TABLE_NAME, null)
                    .objectLiteral();
            if (tableName != null) {
                this.tableName = tableName.stringValue();
            }
            final Literal zkServer = model.filter(implNode, ZK_SERVER, null)
                    .objectLiteral();
            if (zkServer != null) {
                this.zkServer = zkServer.stringValue();
            }
            final Literal username = model.filter(implNode, USERNAME, null)
                    .objectLiteral();
            if (username != null) {
                this.username = username.stringValue();
            }
            final Literal password = model.filter(implNode, PASSWORD, null)
                    .objectLiteral();
            if (password != null) {
                this.password = password.stringValue();
            }
        } catch (final ModelException e) {
            throw new SailConfigException(e);
        }
    }

    @Override
    public void validate() throws SailConfigException {
        super.validate();
        if (members.isEmpty()) {
            throw new SailConfigException("No cluster federation members specified");
        }
        for (final RepositoryImplConfig member : members) {
            try {
                member.validate();
            } catch (final RepositoryConfigException e) {
                throw new SailConfigException(e);
            }
        }
    }
}