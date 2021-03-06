package mvm.rya.accumulo.mr.utils;

/*
 * #%L
 * mvm.rya.accumulo.rya
 * %%
 * Copyright (C) 2014 Rya
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

import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.mapreduce.InputFormatBase;
import org.apache.accumulo.core.client.security.tokens.AuthenticationToken;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

@SuppressWarnings("rawtypes")
public class AccumuloProps extends InputFormatBase {

    @Override
    public RecordReader createRecordReader(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Accumulo Props just holds properties");
    }

    public static Instance getInstance(JobContext  conf) {
        return InputFormatBase.getInstance(conf);
    }

    public static AuthenticationToken getPassword(JobContext  conf) {
        return InputFormatBase.getAuthenticationToken(conf);
    }

    public static String getUsername(JobContext conf) {
        return InputFormatBase.getPrincipal(conf);
    }

    public static String getTablename(JobContext conf) {
        return InputFormatBase.getInputTableName(conf);
    }
}
