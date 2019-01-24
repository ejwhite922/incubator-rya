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

/**
 *
 */
public class OverlapListQuery2 {
    public static void main(final String[] args) throws Exception {
        final String instanceName = "dev";
        final String tableName = "rya_overlap";
        final String zkServer = "localhost:2181";
        final OverlapList at = new OverlapList(zkServer, instanceName);

        final String username = "root";
        final String password = "root";

        at.createConnection(username, password);
        at.selectTable(tableName);
        final Scanner sc = at.createScanner();

        final int numDept = 10;
        final String univ0 = "http://www.University0.edu";
//        final  String univ2 = "http://www.University2.edu";
//        final String predicate = "type";
        final int studentID = 100;
//        final String rowID = "http://www.Department0.University0.edu/GraduateStudent19";
        final String rowValue = "2";
//        final ColumnVisibility colVis = new ColumnVisibility("public");

        // Insert data
        for (int i = 0; i < numDept; i++) {
            at.addData("http://www.Department" + i + ".University0.edu", rowValue);
            for (int j = 0; j < studentID; j++) {
                at.addData("http://www.Department" + i + ".University0.edu" + "/GraduateStudent" + j, rowValue);
            }
        }

        at.addData(univ0, rowValue);

        // Delete data
//        at.deleteData(rowID, rowValue);
        // Scan data
        final Iterator<Entry<Key, Value>> iterator = sc.iterator();
//        final Set<String> result = new HashSet<>();

        while (iterator.hasNext()) {
            final Entry<Key, Value> entry = iterator.next();
            final Key key = entry.getKey();
            final Value value = entry.getValue();
            System.out.println(key.getRow()+ " ==> " + value.toString());
        }
    }
}