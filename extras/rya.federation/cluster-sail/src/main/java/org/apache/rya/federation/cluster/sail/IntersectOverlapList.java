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

import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;

import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.Iteration;

/**
 * The overlap list that holds the IRIs or literals in the current cluster that
 * also appear as subject or object in another cluster(s).
 * @param <E> the type to iterate over.
 * @param <X> the type of {@code Exception} to throw.
 */
public class IntersectOverlapList<E, X extends Exception> extends FilterIteration<E, X> {
    private final Set<String> includeSet;

    /**
     * Creates a new instance of {@link IntersectOverlapList}.
     * @param iter the {@link Iteration}
     * @param includeSet the include set. (not null)
     * @throws X
     */
    public IntersectOverlapList(final Iteration<? extends E, ? extends X> iter, final Set<String> includeSet) throws X {
        super(iter);
        this.includeSet = requireNonNull(includeSet);
    }

    @Override
    protected boolean accept(final E object) throws X {
        return inIncludeSet(object);
    }

    /**
     * Checks if the object is in the include set.
     * @param object the typed object to check for inclusion in the set.
     * @return {@code true} if the object is in the includeSet.
     * {@false} otherwise.
     */
    private boolean inIncludeSet(final E object) {
        final Resource sub = ((ContextStatementImpl)(object)).getSubject();
        final Value obj = ((ContextStatementImpl)(object)).getObject();

        return includeSet.contains(sub.stringValue()) || includeSet.contains(obj.stringValue());
    }
}