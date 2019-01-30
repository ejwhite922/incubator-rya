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
import org.apache.rya.federation.cluster.sail.config.ClusterFederationConfig;
import org.apache.rya.federation.cluster.sail.overlap.AccumuloOverlapList;
import org.apache.rya.federation.cluster.sail.overlap.OverlapListDbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests overlap list.
 */
public class OverlapListQuery7 {
    private static final Logger log = LoggerFactory.getLogger(OverlapListQuery7.class);

    public static void main(final String[] args) throws Exception {
        final String instanceName = "dev";
        final String tableName = OverlapList.DEFAULT_OVERLAP_LIST_TABLE_NAME;
        final String zkServer = "localhost:2181";
        final String username = "root";
        final String password = "root";

        final ClusterFederationConfig config = new ClusterFederationConfig();
        config.setInstanceName(instanceName);
        config.setTableName(tableName);
        config.setZkServer(zkServer);
        config.setUsername(username);
        config.setPassword(password);
        config.setOverlapListDbType(OverlapListDbType.ACCUMULO);

        try (final AccumuloOverlapList overlapList = new AccumuloOverlapList(config)) {
            overlapList.setup();
            final Scanner sc = overlapList.createScanner();

            final int numDept = 5;
            final int numUnderStudent = 20;
            final int numGraduateStudent = 20;
            final String univ0 = "University0.edu";
            final String univ4 = "University4.edu";
            final String univ2 = "University2.edu";
//            final String univ5 = "University5.edu";
            final String univ3 = "University3.edu";
            final String univ1 = "University1.edu";
            final String dept0 = "Department0";
            final String dept1 = "Department1";
            final String dept2 = "Department2";
            final String dept3 = "Department3";
            final String course1 = "CourseU1";
            final String course2 = "GraduateCourseU1";
            final String course3 = "CourseU2";
            final String course4 = "GraduateCourseU2";
            final String course5 = "CourseU3";
            final String course6 = "GraduateCourseU3";
            final String course7 = "CourseU4";
            final String course8 = "GraduateCourseU4";
            final String course15 = "Course15";
            final String course16 = "Course16";
            final String course17 = "GraduateCourse17";
            final String course18 = "GraduateCourse18";

            final String professor = "FullProfessor1";
            final String professor0 = "AssociateProfessor0";
            final String professor1 = "AssociateProfessor1";
            final String professor2 = "AssociateProfessor2";
            final String studentIDU001 = "118";
            final String studentIDU002 = "275";
            final String studentIDU003 = "303";
            final String studentIDU004 = "382";
            final String studentIDU005 = "392";
            final String studentIDU006 = "472";
            final String studentIDU007 = "98";
            final String studentIDU008 = "130";

            final String studentIDU011 = "26";
            final String studentIDU012 = "281";
            final String studentIDU013 = "283";
            final String studentIDU014 = "293";
            final String studentIDU015 = "384";
            final String studentIDU016 = "36";
            final String studentIDU017 = "44";
            final String studentIDU018 = "50";
            final String studentIDU019 = "92";

            final String studentIDU021 = "157";
            final String studentIDU022 = "374";
            final String studentIDU023 = "68";
            final String studentIDU024 = "103";
            final String studentIDU025 = "106";

            final String studentIDU031 = "29";
            final String studentIDU032 = "33";
            final String studentIDU033 = "142";
            final String studentIDU034 = "160";
            final String studentIDU035 = "179";
            final String studentIDU036 = "24";
            final String studentIDU037 = "56";
            final String studentIDU038 = "62";
            final String studentIDU039 = "100";

            final String studentIDU401 = "17";
            final String studentIDU402 = "24";
            final String studentIDU403 = "27";
            final String studentIDU404 = "48";
            final String studentIDU405 = "65";
            final String studentIDU406 = "76";
            final String studentIDU407 = "127";

            final String studentIDU411 = "7";
            final String studentIDU412 = "343";
            final String studentIDU413 = "62";
            final String studentIDU414 = "78";
            final String studentIDU415 = "82";
            final String studentIDU416 = "84";
            final String studentIDU417 = "88";

            final String studentIDU421 = "154";
            final String studentIDU422 = "293";
            final String studentIDU423 = "407";
            final String studentIDU424 = "56";
            final String studentIDU425 = "67";
            final String studentIDU426 = "71";
            final String studentIDU427 = "88";
            final String studentIDU428 = "102";
            final String studentIDU429 = "114";

            final String studentIDU431 = "6";
            final String studentIDU432 = "26";
            final String studentIDU433 = "236";
            final String studentIDU434 = "307";
            final String studentIDU435 = "8";
            final String studentIDU436 = "17";
            final String studentIDU437 = "93";
            final String studentIDU438 = "109";


//            final String rowValue = "2";

            // Insert data

            //query2
            overlapList.addData("http://www." + univ0);
            overlapList.addData("http://www." + univ4);
            overlapList.addData("http://www." + dept0 + "." + univ0);
            overlapList.addData("http://www." + dept0 + "." + univ4);

            for (int i = 0; i < 15; i++) {
                overlapList.addData("http://www." + dept0 + "." + univ4 + "/"+"GraduateStudent" + i);
            }
            for (int i = 0; i < 15; i++) {
                overlapList.addData("http://www." + dept0 + "." + univ0 + "/"+"GraduateStudent" + i);
            }
            //query4
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + professor0);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + professor1);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + professor2);
            //query7
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + course15);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + course16);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + course17);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + course18);
            for (int i = 0; i < numUnderStudent; i++) {
                overlapList.addData("http://www." + dept0 + "." + univ2 + "/" + "UndergraduateStudent" + i);
            }
            for (int i = 0; i < numUnderStudent; i++) {
                overlapList.addData("http://www." + dept0 + "." + univ4 + "/" + "UndergraduateStudent" + i);
            }
            for (int i = 0; i < numGraduateStudent; i++) {
                overlapList.addData("http://www." + dept0 + "." + univ2 + "/" + "GraduateStudent" + i);
            }
            for (int i = 0; i < numGraduateStudent; i++) {
                overlapList.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + i);
            }
            //query9

            for (int i = 0; i < numDept; i++) {
                overlapList.addData("http://www." + "Department" + i + "." + univ0 + "/" + professor);
            }
            for (int i = 0; i < numDept; i++) {
                overlapList.addData("http://www." + "Department" + i + "." + univ4 + "/" + professor);
            }

            overlapList.addData("http://www." + dept0 + "." + univ3 + "/" + course1);
            overlapList.addData("http://www." + dept0 + "." + univ3 + "/" + course2);
            overlapList.addData("http://www." + dept0 + "." + univ1 + "/" + course1);
            overlapList.addData("http://www." + dept0 + "." + univ1 + "/" + course2);
            overlapList.addData("http://www." + dept1 + "." + univ3 + "/" + course3);
            overlapList.addData("http://www." + dept1 + "." + univ3 + "/" + course4);
            overlapList.addData("http://www." + dept1 + "." + univ1 + "/" + course3);
            overlapList.addData("http://www." + dept1 + "." + univ1 + "/" + course4);
            overlapList.addData("http://www." + dept2 + "." + univ3 + "/" + course5);
            overlapList.addData("http://www." + dept2 + "." + univ3 + "/" + course6);
            overlapList.addData("http://www." + dept2 + "." + univ1 + "/" + course5);
            overlapList.addData("http://www." + dept2 + "." + univ1 + "/" + course6);
            overlapList.addData("http://www." + dept3 + "." + univ3 + "/" + course7);
            overlapList.addData("http://www." + dept3 + "." + univ3 + "/" + course8);
            overlapList.addData("http://www." + dept3 + "." + univ1 + "/" + course7);
            overlapList.addData("http://www." + dept3 + "." + univ1 + "/" + course8);
            overlapList.addData("http://www." + dept2 + "." + univ1);

            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU001);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU002);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU003);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU004);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU005);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU006);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + "GraduateStudent" + studentIDU007);
            overlapList.addData("http://www." + dept0 + "." + univ0 + "/" + "GraduateStudent" + studentIDU008);

            overlapList.addData("http://www." + dept1 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU011);
            overlapList.addData("http://www." + dept1 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU012);
            overlapList.addData("http://www." + dept1 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU013);
            overlapList.addData("http://www." + dept1 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU014);
            overlapList.addData("http://www." + dept1 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU015);
            overlapList.addData("http://www." + dept1 + "." + univ0 + "/" + "GraduateStudent" + studentIDU016);
            overlapList.addData("http://www." + dept1 + "." + univ0 + "/" + "GraduateStudent" + studentIDU017);
            overlapList.addData("http://www." + dept1 + "." + univ0 + "/" + "GraduateStudent" + studentIDU018);
            overlapList.addData("http://www." + dept1 + "." + univ0 + "/" + "GraduateStudent" + studentIDU019);

            overlapList.addData("http://www." + dept2 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU021);
            overlapList.addData("http://www." + dept2 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU022);
            overlapList.addData("http://www." + dept2 + "." + univ0 + "/" + "GraduateStudent" + studentIDU023);
            overlapList.addData("http://www." + dept2 + "." + univ0 + "/" + "GraduateStudent" + studentIDU024);
            overlapList.addData("http://www." + dept2 + "." + univ0 + "/" + "GraduateStudent" + studentIDU025);

            overlapList.addData("http://www." + dept3 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU031);
            overlapList.addData("http://www." + dept3 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU032);
            overlapList.addData("http://www." + dept3 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU033);
            overlapList.addData("http://www." + dept3 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU034);
            overlapList.addData("http://www." + dept3 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU035);
            overlapList.addData("http://www." + dept3 + "." + univ0 + "/" + "GraduateStudent" + studentIDU036);
            overlapList.addData("http://www." + dept3 + "." + univ0 + "/" + "GraduateStudent" + studentIDU037);
            overlapList.addData("http://www." + dept3 + "." + univ0 + "/" + "GraduateStudent" + studentIDU038);
            overlapList.addData("http://www." + dept3 + "." + univ0 + "/" + "GraduateStudent" + studentIDU039);

            overlapList.addData("http://www." + dept0 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU401);
            overlapList.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU402);
            overlapList.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU403);
            overlapList.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU404);
            overlapList.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU405);
            overlapList.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU406);
            overlapList.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU407);

            overlapList.addData("http://www." + dept1 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU411);
            overlapList.addData("http://www." + dept1 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU412);
            overlapList.addData("http://www." + dept1 + "." + univ4 + "/" + "GraduateStudent" + studentIDU413);
            overlapList.addData("http://www." + dept1 + "." + univ4 + "/" + "GraduateStudent" + studentIDU414);
            overlapList.addData("http://www." + dept1 + "." + univ4 + "/" + "GraduateStudent" + studentIDU415);
            overlapList.addData("http://www." + dept1 + "." + univ4 + "/" + "GraduateStudent" + studentIDU416);
            overlapList.addData("http://www." + dept1 + "." + univ4 + "/" + "GraduateStudent" + studentIDU417);

            overlapList.addData("http://www." + dept2 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU421);
            overlapList.addData("http://www." + dept2 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU422);
            overlapList.addData("http://www." + dept2 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU423);
            overlapList.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU424);
            overlapList.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU425);
            overlapList.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU426);
            overlapList.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU427);
            overlapList.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU428);
            overlapList.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU429);

            overlapList.addData("http://www." + dept3 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU431);
            overlapList.addData("http://www." + dept3 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU432);
            overlapList.addData("http://www." + dept3 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU433);
            overlapList.addData("http://www." + dept3 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU434);
            overlapList.addData("http://www." + dept3 + "." + univ4 + "/" + "GraduateStudent" + studentIDU435);
            overlapList.addData("http://www." + dept3 + "." + univ4 + "/" + "GraduateStudent" + studentIDU436);
            overlapList.addData("http://www." + dept3 + "." + univ4 + "/" + "GraduateStudent" + studentIDU437);
            overlapList.addData("http://www." + dept3 + "." + univ4 + "/" + "GraduateStudent" + studentIDU438);

            // Delete data
//            overlapList.deleteData(rowID);
            // Scan data
            final Iterator<Entry<Key, Value>> iterator = sc.iterator();
//            final Set<String> result = new HashSet<>();

            while (iterator.hasNext()) {
                final Entry<Key, Value> entry = iterator.next();
                final Key key = entry.getKey();
                final Value value = entry.getValue();
                log.info(key.getRow()+ " ==> " + value.toString());
            }
        }
    }
}