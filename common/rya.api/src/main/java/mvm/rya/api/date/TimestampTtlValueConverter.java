package mvm.rya.api.date;

/*
 * #%L
 * mvm.rya.rya.api
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

import org.openrdf.model.Value;

/**
 * Class TimestampTtlValueConverter
 * @deprecated
 */
public class TimestampTtlValueConverter implements TtlValueConverter {

    private Value start, stop;

    @Override
    public void convert(String ttl, String startTime) {
        long start_l, stop_l;
        long ttl_l = Long.parseLong(ttl);
        stop_l = System.currentTimeMillis();
        if (startTime != null)
            stop_l = Long.parseLong(startTime);
        start_l = stop_l - ttl_l;

        start = vf.createLiteral(start_l);
        stop = vf.createLiteral(stop_l);
    }

    @Override
    public Value getStart() {
        return start;
    }

    @Override
    public Value getStop() {
        return stop;
    }
}
