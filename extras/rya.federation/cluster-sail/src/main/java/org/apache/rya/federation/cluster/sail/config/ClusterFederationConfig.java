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

import static java.util.Objects.requireNonNull;
import static org.openrdf.repository.config.RepositoryImplConfigBase.create;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.rya.api.RdfCloudTripleStoreConfiguration;
import org.apache.rya.federation.cluster.sail.overlap.OverlapListDbType;
import org.apache.rya.rdftriplestore.RyaSailRepository;
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

    /**
     * The overlap list database type.
     */
    public static final URI OVERLAP_LIST_DB_TYPE = new URIImpl(NAMESPACE + "overlapListDbType");

    /**
     * The mongodb hostname.
     */
    public static final URI MONGO_HOSTNAME = new URIImpl(NAMESPACE + "mongoHostname");

    /**
     * The mongodb port.
     */
    public static final URI MONGO_PORT = new URIImpl(NAMESPACE + "mongoPort");

    private List<RepositoryImplConfig> members = new ArrayList<>();

    private final Set<String> localPropertySpace = new HashSet<>(); // NOPMD

    private boolean distinct;

    private boolean readOnly;

    private RdfCloudTripleStoreConfiguration rdfCloudTripleStoreConfiguration;

    private String instanceName;

    private String tableName;

    private String zkServer;

    private String username;

    private String password;

    private OverlapListDbType overlapListDbType;

    private String mongoHostname;

    private int mongoPort;

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
     * @return the {@link RdfCloudTripleStoreConfiguration}.
     */
    public RdfCloudTripleStoreConfiguration getRdfCloudTripleStoreConfiguration() {
        return rdfCloudTripleStoreConfiguration;
    }

    /**
     * @param rdfCloudTripleStoreConfiguration the {@link RdfCloudTripleStoreConfiguration}. (not null)
     */
    public void setRdfCloudTripleStoreConfiguration(final RdfCloudTripleStoreConfiguration rdfCloudTripleStoreConfiguration) {
        this.rdfCloudTripleStoreConfiguration = requireNonNull(rdfCloudTripleStoreConfiguration);
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
        this.instanceName = requireNonNull(instanceName);
    }

    /**
     * @return the accumulo or mongodb table/collection name.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the accumulo or mongodb table/collection name. (not null)
     */
    public void setTableName(final String tableName) {
        this.tableName = requireNonNull(tableName);
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
        this.zkServer = requireNonNull(zkServers);
    }

    /**
     * @return the accumulo or mongodb username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the accumulo or mongodb username. (not null)
     */
    public void setUsername(final String username) {
        this.username = requireNonNull(username);
    }

    /**
     * @return the accumulo or mongodb user password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the accumulo or mongodb user password. (not null)
     */
    public void setPassword(final String password) {
        this.password = requireNonNull(password);
    }

    /**
     * @return the overlap list database type.
     */
    public OverlapListDbType getOverlapListDbType() {
        return overlapListDbType;
    }

    /**
     * @param overlapListDbType the overlap list database type. (not null)
     */
    public void setOverlapListDbType(final OverlapListDbType overlapListDbType) {
        this.overlapListDbType = requireNonNull(overlapListDbType);
    }

    /**
     * @return the mongo hostname.
     */
    public String getMongoHostname() {
        return mongoHostname;
    }

    /**
     * @param mongoHostname the mongo hostname. (not null)
     */
    public void setMongoHostname(final String mongoHostname) {
        this.mongoHostname = requireNonNull(mongoHostname);
    }

    /**
     * @return the mongo port.
     */
    public int getMongoPort() {
        return mongoPort;
    }

    /**
     * @param mongoPort the mongo port.
     */
    public void setMongoPort(final int mongoPort) {
        this.mongoPort = mongoPort;
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
        model.add(self, OVERLAP_LIST_DB_TYPE, valueFactory.createLiteral(overlapListDbType.toString()));
        model.add(self, MONGO_HOSTNAME, valueFactory.createLiteral(mongoHostname));
        model.add(self, MONGO_PORT, valueFactory.createLiteral(mongoPort));
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
            final Literal overlapListDbType = model.filter(implNode, OVERLAP_LIST_DB_TYPE, null)
                    .objectLiteral();
            if (overlapListDbType != null) {
                this.overlapListDbType = OverlapListDbType.fromName(overlapListDbType.stringValue());
            }
            final Literal mongoHostname = model.filter(implNode, MONGO_HOSTNAME, null)
                    .objectLiteral();
            if (mongoHostname != null) {
                this.mongoHostname = mongoHostname.stringValue();
            }
            final Literal mongoPort = model.filter(implNode, MONGO_PORT, null)
                    .objectLiteral();
            if (mongoPort != null) {
                this.mongoPort = mongoPort.intValue();
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
                if (member instanceof RyaSailRepository) {
                    throw new SailConfigException("Cluster member must be a Rya Sail Repository");
                }
                member.validate();
            } catch (final RepositoryConfigException e) {
                throw new SailConfigException(e);
            }
        }
    }
}