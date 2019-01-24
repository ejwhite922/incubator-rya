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

import static java.util.Objects.requireNonNull;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerBase;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Query result handler that returns the count of the number of binding sets
 * produced by a query.
 */
public class CountingResultHandler extends TupleQueryResultHandlerBase {
    private static final Logger log = LoggerFactory.getLogger(CountingResultHandler.class);

    private int count = 0;
    private final Level level;

    /**
     * Creates a new instance of {@link CountingResultHandler}.
     */
    public CountingResultHandler() {
        this(Level.INFO);
    }

    /**
     * Creates a new instance of {@link CountingResultHandler}.
     * @param level the {@link Level} (not null)
     */
    public CountingResultHandler(final Level level) {
        this.level = requireNonNull(level);
    }

    /**
     * @return the total count for the binding sets returned by the query.
     */
    public int getCount() {
        return count;
    }

    /**
     * Resets the count.
     */
    public void resetCount() {
    }

    @Override
    public void handleSolution(final BindingSet bindingSet) throws TupleQueryResultHandlerException {
        count++;
        log(level, bindingSet.toString());
    }

    private static void log(final Level level, final String message) {
        switch (level) {
            case TRACE:
                log.trace(message);
                break;
            case DEBUG:
                log.debug(message);
                break;
            case INFO:
                log.info(message);
                break;
            case WARN:
                log.warn(message);
                break;
            case ERROR:
                log.error(message);
                break;
        }
    }
}