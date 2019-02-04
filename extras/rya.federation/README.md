<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Cluster Federation


Federated RDF systems allow users to retrieve data from multiple independent sources without needing to have all the data in the same triple store. However, the performance of these systems can be poor for geographically distributed sources where network transfer costs are high. This is a Rya project that introduces novel join algorithms that take advantage of network topology to decrease the cost of processing Basic Graph Pattern SPARQL queries in a geographically distributed environment. Federation members are grouped in clusters, based on the network communication cost between the members, and the bulk of the join processing is pushed to the clusters. This supports both Accumulo and MongoDB based systems.

## Table of Contents

- [Requirements](#requirements)
- [Building](#building)
- [Using Cluster Federation](#using-cluster-federation)
- [How to Use](#how-to-use)
- [License](#license)

## Requirements
* JDK 1.8 or newer ([http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html))
* Apache Rya 3.2.13 or newer ([https://rya.apache.org/download/](https://rya.apache.org/download/))
* Apache Maven 3.1.0 or newer ([https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi))
* One of the following datastores:
    * Accumulo 1.6.4 ([https://archive.apache.org/dist/accumulo/](https://archive.apache.org/dist/accumulo/))
    * MongoDB 3.3.0 ([https://www.mongodb.com/download-center](https://www.mongodb.com/download-center))

## Building

The Rya Cluster Federation can be built using from the rya.federation.parent project or its child projects using:

`mvn clean install`

The tests can be skipped by adding `-DskipTests=true` to the Maven build.

## Using Cluster Federation

To use Rya Cluster Federation, clone the Rya project, build it, and find the resulting `jar` files from `{rya.federation.cluster.sail.project.dir}/target` and `{rya.federation.cluster.runtime.project.dir}/target` directories. The following 2 jars are produced for cluster federation:

  * rya.federation.cluster.sail-3.2.13-incubating-SNAPSHOT.jar
  * rya.federation.cluster.runtime-3.2.13-incubating-SNAPSHOT.jar
  
Copy these jars to Apache Tomcat's webapps `lib` directory for `openrdf-workbench`. Restart Tomcat.

## How to Use

There are two ways to enable the Rya cluster federation feature in openrdf. One is to add the necessary jar files to `/var/lib/tomcat7/webapps/`. Copy `create.xsl` and `create-ClusterFederation.xsl` to `/var/lib/tomcat7/webapps/openrdf-workbench/transformations/` and copy the rest three java libraries to both `/var/lib/tomcat7/webapps/openrdf-workbench/WEB-INF/lib/` and `/var/lib/tomcat7/webapps/openrdf-sesame/WEB-INF/lib/`. Then restart your Tomcat service, you can see a new cluster federation option appears on your workbench webpage.

Another way is to run the provided Java examples of cluster federation. These can be found and run from the test folder of the `rya.federation.cluster.sail` project from the Rya source code. To test cluster federation we created some experiments to compare the execution time of regular federation method and our cluster federation. We chose Lehigh University Benchmark as our benchmark dataset and test queries provided from here. You can reproduce our experiments by using our test code from the test folder.

For example, after uploading the dataset via workbench on each cluster center, you can run `CreateURITableTest`, `CreateBloomFilterTest`, `CreateNewURIIndexTest`, and `CreateOverlapTest` sequentially to generate 0-hop overlap list, then you can run `NHopOverlapTest` to generate N-hop overlap list in Accumulo table.

After creating overlap lists on each cluster center, you can run `ComparisonFederationQueryTest` to calculate execution time of regular federation on the cluster coordinator. Or `ClusterFederationQueryTest` to calculate execution time of cluster federation. You can create your own test code and datasets to run your experiments.

## License

This project is licensed under the Apache Software License, version 2.0.