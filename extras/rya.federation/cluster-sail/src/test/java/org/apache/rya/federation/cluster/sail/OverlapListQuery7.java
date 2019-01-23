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
public class OverlapListQuery7 {
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

        final int numDept = 5;
        final int numUnderStudent = 20;
        final int numGraduateStudent = 20;
        final String univ0 = "University0.edu";
        final String univ4 = "University4.edu";
        final String univ2 = "University2.edu";
//        final String univ5 = "University5.edu";
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


        final String rowValue = "2";

        // Insert data

        //query2
        at.addData("http://www." + univ0, rowValue);
        at.addData("http://www." + univ4, rowValue);
        at.addData("http://www." + dept0 + "." + univ0, rowValue);
        at.addData("http://www." + dept0 + "." + univ4, rowValue);

        for (int i = 0; i < 15; i++){
            at.addData("http://www." + dept0 + "." + univ4 + "/"+"GraduateStudent" + i, rowValue);
        }
        for (int i = 0; i < 15; i++){
            at.addData("http://www." + dept0 + "." + univ0 + "/"+"GraduateStudent" + i, rowValue);
        }
        //query4
        at.addData("http://www." + dept0 + "." + univ0 + "/" + professor0, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + professor1, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + professor2, rowValue);
        //query7
        at.addData("http://www." + dept0 + "." + univ0 + "/" + course15, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + course16, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + course17, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + course18, rowValue);
        for (int i = 0; i < numUnderStudent; i++){
            at.addData("http://www." + dept0 + "." + univ2 + "/" + "UndergraduateStudent" + i, rowValue);
        }
        for (int i = 0; i < numUnderStudent; i++){
            at.addData("http://www." + dept0 + "." + univ4 + "/" + "UndergraduateStudent" + i, rowValue);
        }
        for (int i = 0; i < numGraduateStudent; i++){
            at.addData("http://www." + dept0 + "." + univ2 + "/" + "GraduateStudent" + i, rowValue);
        }
        for (int i = 0; i < numGraduateStudent; i++){
            at.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + i, rowValue);
        }
        //query9

        for (int i = 0; i < numDept; i++){
            at.addData("http://www." + "Department" + i + "." + univ0 + "/" + professor, rowValue);
        }
        for (int i = 0; i < numDept; i++){
            at.addData("http://www." + "Department" + i + "." + univ4 + "/" + professor, rowValue);
        }

        at.addData("http://www." + dept0 + "." + univ3 + "/" + course1, rowValue);
        at.addData("http://www." + dept0 + "." + univ3 + "/" + course2, rowValue);
        at.addData("http://www." + dept0 + "." + univ1 + "/" + course1, rowValue);
        at.addData("http://www." + dept0 + "." + univ1 + "/" + course2, rowValue);
        at.addData("http://www." + dept1 + "." + univ3 + "/" + course3, rowValue);
        at.addData("http://www." + dept1 + "." + univ3 + "/" + course4, rowValue);
        at.addData("http://www." + dept1 + "." + univ1 + "/" + course3, rowValue);
        at.addData("http://www." + dept1 + "." + univ1 + "/" + course4, rowValue);
        at.addData("http://www." + dept2 + "." + univ3 + "/" + course5, rowValue);
        at.addData("http://www." + dept2 + "." + univ3 + "/" + course6, rowValue);
        at.addData("http://www." + dept2 + "." + univ1 + "/" + course5, rowValue);
        at.addData("http://www." + dept2 + "." + univ1 + "/" + course6, rowValue);
        at.addData("http://www." + dept3 + "." + univ3 + "/" + course7, rowValue);
        at.addData("http://www." + dept3 + "." + univ3 + "/" + course8, rowValue);
        at.addData("http://www." + dept3 + "." + univ1 + "/" + course7, rowValue);
        at.addData("http://www." + dept3 + "." + univ1 + "/" + course8, rowValue);
        at.addData("http://www." + dept2 + "." + univ1, rowValue);

        at.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU001, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU002, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU003, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU004, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU005, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU006, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + "GraduateStudent" + studentIDU007, rowValue);
        at.addData("http://www." + dept0 + "." + univ0 + "/" + "GraduateStudent" + studentIDU008, rowValue);

        at.addData("http://www." + dept1 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU011, rowValue);
        at.addData("http://www." + dept1 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU012, rowValue);
        at.addData("http://www." + dept1 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU013, rowValue);
        at.addData("http://www." + dept1 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU014, rowValue);
        at.addData("http://www." + dept1 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU015, rowValue);
        at.addData("http://www." + dept1 + "." + univ0 + "/" + "GraduateStudent" + studentIDU016, rowValue);
        at.addData("http://www." + dept1 + "." + univ0 + "/" + "GraduateStudent" + studentIDU017, rowValue);
        at.addData("http://www." + dept1 + "." + univ0 + "/" + "GraduateStudent" + studentIDU018, rowValue);
        at.addData("http://www." + dept1 + "." + univ0 + "/" + "GraduateStudent" + studentIDU019, rowValue);

        at.addData("http://www." + dept2 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU021, rowValue);
        at.addData("http://www." + dept2 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU022, rowValue);
        at.addData("http://www." + dept2 + "." + univ0 + "/" + "GraduateStudent" + studentIDU023, rowValue);
        at.addData("http://www." + dept2 + "." + univ0 + "/" + "GraduateStudent" + studentIDU024, rowValue);
        at.addData("http://www." + dept2 + "." + univ0 + "/" + "GraduateStudent" + studentIDU025, rowValue);

        at.addData("http://www." + dept3 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU031, rowValue);
        at.addData("http://www." + dept3 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU032, rowValue);
        at.addData("http://www." + dept3 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU033, rowValue);
        at.addData("http://www." + dept3 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU034, rowValue);
        at.addData("http://www." + dept3 + "." + univ0 + "/" + "UndergraduateStudent" + studentIDU035, rowValue);
        at.addData("http://www." + dept3 + "." + univ0 + "/" + "GraduateStudent" + studentIDU036, rowValue);
        at.addData("http://www." + dept3 + "." + univ0 + "/" + "GraduateStudent" + studentIDU037, rowValue);
        at.addData("http://www." + dept3 + "." + univ0 + "/" + "GraduateStudent" + studentIDU038, rowValue);
        at.addData("http://www." + dept3 + "." + univ0 + "/" + "GraduateStudent" + studentIDU039, rowValue);

        at.addData("http://www." + dept0 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU401, rowValue);
        at.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU402, rowValue);
        at.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU403, rowValue);
        at.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU404, rowValue);
        at.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU405, rowValue);
        at.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU406, rowValue);
        at.addData("http://www." + dept0 + "." + univ4 + "/" + "GraduateStudent" + studentIDU407, rowValue);

        at.addData("http://www." + dept1 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU411, rowValue);
        at.addData("http://www." + dept1 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU412, rowValue);
        at.addData("http://www." + dept1 + "." + univ4 + "/" + "GraduateStudent" + studentIDU413, rowValue);
        at.addData("http://www." + dept1 + "." + univ4 + "/" + "GraduateStudent" + studentIDU414, rowValue);
        at.addData("http://www." + dept1 + "." + univ4 + "/" + "GraduateStudent" + studentIDU415, rowValue);
        at.addData("http://www." + dept1 + "." + univ4 + "/" + "GraduateStudent" + studentIDU416, rowValue);
        at.addData("http://www." + dept1 + "." + univ4 + "/" + "GraduateStudent" + studentIDU417, rowValue);

        at.addData("http://www." + dept2 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU421, rowValue);
        at.addData("http://www." + dept2 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU422, rowValue);
        at.addData("http://www." + dept2 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU423, rowValue);
        at.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU424, rowValue);
        at.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU425, rowValue);
        at.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU426, rowValue);
        at.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU427, rowValue);
        at.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU428, rowValue);
        at.addData("http://www." + dept2 + "." + univ4 + "/" + "GraduateStudent" + studentIDU429, rowValue);

        at.addData("http://www." + dept3 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU431, rowValue);
        at.addData("http://www." + dept3 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU432, rowValue);
        at.addData("http://www." + dept3 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU433, rowValue);
        at.addData("http://www." + dept3 + "." + univ4 + "/" + "UndergraduateStudent" + studentIDU434, rowValue);
        at.addData("http://www." + dept3 + "." + univ4 + "/" + "GraduateStudent" + studentIDU435, rowValue);
        at.addData("http://www." + dept3 + "." + univ4 + "/" + "GraduateStudent" + studentIDU436, rowValue);
        at.addData("http://www." + dept3 + "." + univ4 + "/" + "GraduateStudent" + studentIDU437, rowValue);
        at.addData("http://www." + dept3 + "." + univ4 + "/" + "GraduateStudent" + studentIDU438, rowValue);

        // Delete data
//        at.deleteData(rowID, rowValue);
        // Scan data
        final Iterator<Entry<Key, Value>> iterator = sc.iterator();
//        final Set<String> result = new HashSet<String>();

        while (iterator.hasNext()) {
            final Entry<Key, Value> entry = iterator.next();
            final Key key = entry.getKey();
            final Value value = entry.getValue();
            System.out.println(key.getRow()+ " ==> " + value.toString());
        }
    }
}