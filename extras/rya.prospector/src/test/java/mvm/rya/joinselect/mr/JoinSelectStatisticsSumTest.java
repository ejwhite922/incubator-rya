package mvm.rya.joinselect.mr;

/*
 * #%L
 * mvm.rya.rya.prospector
 * %%
 * Copyright (C) 2014 - 2015 Rya
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;

import mvm.rya.joinselect.mr.JoinSelectStatisticsSum;
import mvm.rya.joinselect.mr.utils.CardList;
import mvm.rya.joinselect.mr.utils.TripleEntry;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Test;

public class JoinSelectStatisticsSumTest {

  @Test
  public void testFullTripleEntry() throws InterruptedException, IOException {

    TripleEntry te1 = new TripleEntry(new Text("urn:gem:etype#1234"), new Text("urn:gem#pred"), new Text("subject"), new Text("predicate"), new Text("object"));
    CardList cl = new CardList(34, 52, 63, 0, 0, 0);
    TripleEntry te2 = new TripleEntry(new Text("urn:gem:etype#1234"), new Text(""), new Text("subject"), new Text(""), new Text("object"));
    TripleEntry te3 = new TripleEntry(new Text("urn:gem#pred"), new Text(""), new Text("predicate"), new Text(""), new Text("object"));

    new MapDriver<TripleEntry,CardList,TripleEntry,CardList>().withMapper(new JoinSelectStatisticsSum.CardinalityIdentityMapper()).withInput(te1, cl)
        .withOutput(te2, cl).withOutput(te3, cl).withOutput(te1, cl).runTest();

  }

  @Test
  public void testPartialTripleEntry() throws InterruptedException, IOException {

    TripleEntry te1 = new TripleEntry(new Text("urn:gem:etype#1234"), new Text(""), new Text("subject"), new Text(""), new Text("object"));
    CardList cl = new CardList(34, 52, 63, 0, 0, 0);

    new MapDriver<TripleEntry,CardList,TripleEntry,CardList>().withMapper(new JoinSelectStatisticsSum.CardinalityIdentityMapper()).withInput(te1, cl)
        .withOutput(te1, cl).runTest();

  }

}
