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

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.rya.federation.cluster.sail.OverlapList;

/**
 *
 * @author vagrant
 */
public class LoadDataTimeTest {
    public static void main(final String[] args) throws Exception {
        final String instanceName = "dev";
        final String tableURI = "URI_index";
        final String zkServer1 = "192.168.33.50:2181";
        final String username = "root";
        final String password = "root";

//        final Iterator<Entry<Key, Value>> iterator1;
//        final Iterator<Entry<Key, Value>> iterator2;

        final OverlapList ol1 = new OverlapList(zkServer1, instanceName);
        ol1.createConnection(username, password);
        ol1.selectTable(tableURI);

        final Scanner sc1 = ol1.createScanner();
        final Iterator<Entry<Key, Value>> iterator1 = sc1.iterator();
        long count = 0;
        while (iterator1.hasNext()) {
            count++;
            iterator1.next();
        }
        System.out.println(count);
    }
}