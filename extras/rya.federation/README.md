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
- [Running from Workbench](#running-from-workbench)
- [License](#license)

## Requirements
* JDK 1.8 or newer ([http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html))
* Apache Rya 3.2.13 or newer ([https://rya.apache.org/download/](https://rya.apache.org/download/))
* Apache Maven 3.1.0 or newer ([https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi))
* One of the following datastores:
    * Accumulo 1.6.4 ([https://archive.apache.org/dist/accumulo/](https://archive.apache.org/dist/accumulo/))
    * MongoDB 3.3.0 ([https://www.mongodb.com/download-center](https://www.mongodb.com/download-center))
* Docker ([https://docs.docker.com/install/](https://docs.docker.com/install/))
* Uno ([https://github.com/apache/fluo-uno#installation](https://github.com/apache/fluo-uno#installation))

## Building

The Rya Cluster Federation can be built using from the rya.federation.parent project or its child projects using:

`mvn clean install`

The tests can be skipped by adding `-DskipTests=true` to the Maven build.

## Using Cluster Federation

### Setup

Cluster federation uses Docker to provide an example of a running cluster. To
recreate the environment for this example:

1.  Download and install Docker: [https://docs.docker.com/install/](https://docs.docker.com/install/)
2.  Run the below command to download uno:  (Further instructions can be found here: [https://github.com/apache/fluo-uno#installation](https://github.com/apache/fluo-uno#installation)): 
```
    git clone https://github.com/apache/fluo-uno.git
```

3.  Get the Dockerfile (`feduno`) and `start.sh` from `src/test/resources/` and put them in the same folder (such as `/vm/`)

4.  Run:
```
    docker build -t feduno /vm/
```

5. Run as many instances as you need in separate terminal/cmd windows, changing the host name for each: 
```
    docker run --rm -P --hostname accumulo01 -v /pathToUno/fluo-uno/:/unoShare  feduno /start.sh
    docker run --rm -P --hostname accumulo02 -v /pathToUno/fluo-uno/:/unoShare  feduno /start.sh
    docker run --rm -P --hostname accumulo03 -v /pathToUno/fluo-uno/:/unoShare  feduno /start.sh
    docker run --rm -P --hostname accumulo04 -v /pathToUno/fluo-uno/:/unoShare  feduno /start.sh
```

6. Add to your `/etc/hosts` file: 
```
172.17.0.2 accumulo01 
172.17.0.3 accumulo02 
172.17.0.4 accumulo03
172.17.0.5 accumulo04
```

7.  Run the example below

### Running Cluster Federation Examples

Some Java examples are provided for cluster federation. These can be found and run from the test folder of the `rya.federation.cluster.sail` project from the Rya source code. To test cluster federation we created some experiments to compare the execution time of regular federation method and our cluster federation. We chose Lehigh University Benchmark as our benchmark dataset and test queries provided from here. You can reproduce our experiments by using our test code from the test folder.

For example, first add the data to the clusters with `InsertDataTest`. This can be done from command line by:
```
    mvn exec:java -o -pl extras/rya.federation/cluster-sail \
-Dexec.mainClass="org.apache.rya.federation.cluster.sail.InsertDataTest" \
-Dexec.classpathScope=test
```

After uploading the dataset on each cluster center, you can run `CreateURITableTest`, `CreateBloomFilterTest`, `CreateNewURIIndexTest`, and `CreateOverlapTest` sequentially to generate 0-hop overlap list, then you can run `NHopOverlapTest` to generate N-hop overlap list in Accumulo table.

After creating overlap lists on each cluster center, you can run `ComparisonFederationQueryTest` to calculate execution time of regular federation on the cluster coordinator. Or `ClusterFederationQueryTest` to calculate execution time of cluster federation. You can create your own test code and datasets to run your experiments.


## Running from Workbench

Another way to use Rya Cluster Federation is from the openrdf workbench, clone the Rya project, build it, and find the resulting `jar` files from `{rya.federation.cluster.sail.project.dir}/target` and `{rya.federation.cluster.runtime.project.dir}/target` directories. The following 2 jars are produced for cluster federation:

  * rya.federation.cluster.sail-3.2.13-incubating-SNAPSHOT.jar
  * rya.federation.cluster.runtime-3.2.13-incubating-SNAPSHOT.jar
  
Copy these jars to Apache Tomcat's `/var/lib/tomcat7/webapps/`. Copy `create.xsl` and `create-ClusterFederation.xsl` to `/var/lib/tomcat7/webapps/openrdf-workbench/transformations/` and copy the rest three java libraries to both `/var/lib/tomcat7/webapps/openrdf-workbench/WEB-INF/lib/` and `/var/lib/tomcat7/webapps/openrdf-sesame/WEB-INF/lib/`. Then restart your Tomcat service, you can see a new cluster federation option appears on your workbench webpage.


## License

This project is licensed under the Apache Software License, version 2.0.