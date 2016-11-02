/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rya.indexing.external.matching;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.rya.indexing.external.matching.QueryNodesToTupleExpr.TupleExprAndNodes;
import org.apache.rya.indexing.pcj.matching.PCJOptimizerUtilities;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ExternalSet;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import com.google.common.base.Optional;

/**
 * Abstract base class meant to be extended by any QueryOptimizer that matches ExternalSets
 * to QueryModelNodes within the parsed query plan.
 *
 * @param <T> - ExternalSet parameter
 */
public abstract class AbstractExternalSetOptimizer<T extends ExternalSet> implements QueryOptimizer {

    protected boolean useOptimal = false;

    @Override
    public void optimize(final TupleExpr tupleExpr, final Dataset dataset, final BindingSet bindings) {
        final QuerySegmentMatchVisitor visitor = new QuerySegmentMatchVisitor();
        tupleExpr.visit(visitor);
    }

    /**
     * This visitor navigates query until it reaches either a Join, Filter, or
     * LeftJoin. Once it reaches this node, it creates the appropriate
     * ExternalSetMatcher and uses this to match each of the {@link ExternalSet}
     * s to the {@link QuerySegment} starting with the Join, Filter, or
     * LeftJoin. Once each ExternalSet has been compared for matching, the
     * portion of the query starting with the Join, Filter, or LeftJoin is
     * replaced by the {@link TupleExpr} returned by
     * {@link ExternalSetMatcher#getQuery()}. This visitor then visits each of
     * the nodes returned by {@link ExternalSetMatcher#getUnmatchedArgs()}.
     *
     */
    protected class QuerySegmentMatchVisitor extends QueryModelVisitorBase<RuntimeException> {

        private final QuerySegmentFactory<T> factory = new QuerySegmentFactory<T>();

        @Override
        public void meetNode(final QueryModelNode node) {

            if (checkNode(node)) {
                //have a set of nodes
                final QuerySegment<T> segment = factory.getQuerySegment(node);

                //create a set of T that are in the set of nodes
                final ExternalSetProvider<T> provider = getProvider(segment);

                //match the provided T nodes to the existing nodes in the segment.
                final ExternalSetMatcher<T> matcher = getMatcher(segment);
                QuerySegment<T> tempSeg = null;
                if(useOptimal) {
                    tempSeg = matcher.match(provider.getExternalSetCombos(segment), getNodeListRater(segment));
                } else {
                    tempSeg = matcher.match(provider.getExternalSets(segment));
                }

                final TupleExprAndNodes tups = tempSeg.getQuery();
                node.replaceWith(tups.getTupleExpr());
                final Set<TupleExpr> unmatched = getUnMatchedArgNodes(tups.getNodes());
                PCJOptimizerUtilities.relocateFilters(tups.getFilters());

                for (final TupleExpr tupleExpr : unmatched) {
                    tupleExpr.visit(this);
                }
            } else {
                super.meetNode(node);
            }
        }
    }

    private Set<TupleExpr> getUnMatchedArgNodes(final List<QueryModelNode> nodes) {
        final Set<TupleExpr> unmatched = new HashSet<>();
        for (final QueryModelNode q : nodes) {
            if (q instanceof UnaryTupleOperator
                    || q instanceof BinaryTupleOperator) {
                unmatched.add((TupleExpr) q);
            }
        }
        return unmatched;
    }


    private static boolean checkNode(final QueryModelNode node) {
        return (node instanceof Join || node instanceof Filter || node instanceof LeftJoin);
    }

    /**
     * Get Matcher used to match ExternalSets to query
     *
     * @param segment
     * @return
     */
    protected abstract ExternalSetMatcher<T> getMatcher(QuerySegment<T> segment);

    /**
     * Get ExternalSetProvider for source of ExternalSets to match to query
     *
     * @param segment
     * @return
     */
    protected abstract ExternalSetProvider<T> getProvider(QuerySegment<T> segment);

    /**
     * Get QueryNodeListRater to find optimal QueryNodeList after matching ExternalSets
     *
     * @return
     */
    protected abstract Optional<QueryNodeListRater> getNodeListRater(QuerySegment<T> segment);

}
