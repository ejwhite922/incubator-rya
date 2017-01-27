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
package org.apache.rya.indexing.entity.query;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.rya.api.domain.RyaType;
import org.apache.rya.api.domain.RyaURI;
import org.apache.rya.indexing.entity.model.Entity;
import org.apache.rya.indexing.entity.model.Property;
import org.apache.rya.indexing.entity.model.Type;
import org.apache.rya.indexing.entity.storage.EntityStorage;
import org.apache.rya.indexing.entity.storage.EntityStorage.EntityStorageException;
import org.apache.rya.rdftriplestore.evaluation.ExternalBatchingIterator;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.impl.ExternalSet;
import org.openrdf.query.algebra.evaluation.iterator.CollectionIteration;
import org.openrdf.query.impl.MapBindingSet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;

/**
 * TODO impl, test, doc
 */
@DefaultAnnotation(NonNull.class)
public class EntityQueryNode extends ExternalSet implements ExternalBatchingIterator {

    /**
     * The RyaURI that when used as the Predicate of a Statement Pattern indicates the Type of the Entities.
     */
    private static final RyaURI TYPE_ID_URI = new RyaURI(RDF.TYPE.toString());

    // Provided at construction time.
    private final Type type;
    private final Collection<StatementPattern> patterns;
    private final EntityStorage entities;

    // Information about the subject of the patterns.
    private final boolean subjectIsConstant;
    private final Optional<String> subjectConstant;
    private final Optional<String> subjectVar;

    //since and EntityQueryNode exists in a single segment, all binding names are garunteed to be assured.
    private final Set<String> bindingNames;

    // Information about the objects of the patterns.

    // XXX what does this map? property name -> binding variable?
    // for any property of the entity that has a variable, have to fill it in?
    private final ImmutableMap<RyaURI, Var> objectVariables;


    /**
     * Constructs an instance of {@link EntityQueryNode}.
     *
     * @param type - The type of {@link Entity} this node matches. (not null)
     * @param patterns - The query StatementPatterns that are solved using an
     *   Entity of the Type. (not null)
     * @param entities - The {@link EntityStorage} that will be searched to match
     *   {@link BindingSet}s when evaluating a query. (not null)
     */
    public EntityQueryNode(final Type type, final Collection<StatementPattern> patterns, final EntityStorage entities) throws IllegalStateException {
        this.type = requireNonNull(type);
        this.patterns = requireNonNull(patterns);
        this.entities = requireNonNull(entities);

        bindingNames = new HashSet<>();

        // Subject based preconditions.
        verifySameSubjects(patterns);

        // Predicate based preconditions.
        verifyAllPredicatesAreConstants(patterns);
        verifyHasCorrectTypePattern(type, patterns);
        verifyAllPredicatesPartOfType(type, patterns);

        // The Subject may either be constant or a variable.
        final Var subject = patterns.iterator().next().getSubjectVar();
        subjectIsConstant = subject.isConstant();
        if(subjectIsConstant) {
            subjectConstant = Optional.of( subject.getValue().toString() );
            subjectVar = Optional.empty();
        } else {
            subjectConstant = Optional.empty();
            subjectVar = Optional.of( subject.getName() );
        }

        // TODO Also, map each variable that is in an Object spot each variable can be mapped to a property name as well
        // Processing note:
        // Any constant that appears in the Object portion of the SP will be used to make sure they match.
        final Builder<RyaURI, Var> builder = ImmutableMap.<RyaURI, Var>builder();
        for(final StatementPattern sp : patterns) {
            final Var object = sp.getObjectVar();
            final Var pred = sp.getPredicateVar();
            bindingNames.addAll(sp.getBindingNames());
            builder.put(new RyaURI(pred.getValue().stringValue()), object);
        }
        objectVariables = builder.build();
    }

    @Override
    public Set<String> getBindingNames() {
        return bindingNames;
    }

    @Override
    public Set<String> getAssuredBindingNames() {
        return bindingNames;
    }

    /**
     * Verify the Subject for all of the patterns is the same.
     *
     * @param patterns - The patterns to check.
     * @throws IllegalStateException If all of the Subjects are not the same.
     */
    private static void verifySameSubjects(final Collection<StatementPattern> patterns) throws IllegalStateException {
        requireNonNull(patterns);

        final Iterator<StatementPattern> it = patterns.iterator();
        final Var subject = it.next().getSubjectVar();

        while(it.hasNext()) {
            final StatementPattern pattern = it.next();
            if(!pattern.getSubjectVar().equals(subject)) {
                throw new IllegalStateException("At least one of the patterns has a different subject from the others. " +
                        "All subjects must be the same.");
            }
        }
    }

    /**
     * Verifies all of the Statement Patterns have Constants for their predicates.
     *
     * @param patterns - The patterns to check. (not null)
     * @throws IllegalStateException A pattern has a variable predicate.
     */
    private static void verifyAllPredicatesAreConstants(final Collection<StatementPattern> patterns) throws IllegalStateException {
        requireNonNull(patterns);

        for(final StatementPattern pattern : patterns) {
            if(!pattern.getPredicateVar().isConstant()) {
                throw new IllegalStateException("The Predicate of a Statement Pattern must be constant. Pattern: " + pattern);
            }
        }
    }

    /**
     * Verifies a single Statement Pattern defines the Type of Entity this query node matches.
     *
     * @param type - The expected Type. (not null)
     * @param patterns - The patterns to check. (not null)
     * @throws IllegalStateException No Type or the wrong Type is specified by the patterns.
     */
    private static void verifyHasCorrectTypePattern(final Type type, final Collection<StatementPattern> patterns) throws IllegalStateException {
        requireNonNull(type);
        requireNonNull(patterns);

        boolean typeFound = false;

        for(final StatementPattern pattern : patterns) {
            final RyaURI predicate = new RyaURI(pattern.getPredicateVar().getValue().toString());

            if(predicate.equals(TYPE_ID_URI)) {
                final RyaURI typeId = new RyaURI( pattern.getObjectVar().getValue().stringValue() );
                if(typeId.equals(type.getId())) {
                    typeFound = true;
                } else {
                    throw new IllegalStateException("Statement Pattern encountred for a Type that does not match the expected Type." +
                            " Expected Type = '" + type.getId().getData() + "' Found Type = '" + typeId.getData() + "'");
                }
            }
        }

        if(!typeFound) {
            throw new IllegalStateException("The collection of Statement Patterns that this node matches must define which Type they match.");
        }
    }

    /**
     * Verify all of the patterns have predicates that match one of the Type's property names.
     *
     * @param type - The Type the patterns match. (not null)
     * @param patterns - The patterns to check.
     * @throws IllegalStateException If any of the non-type defining Statement Patterns
     *   contain a predicate that does not match one of the Type's property names.
     */
    private static void verifyAllPredicatesPartOfType(final Type type, final Collection<StatementPattern> patterns) throws IllegalStateException {
        requireNonNull(type);
        requireNonNull(patterns);

        for(final StatementPattern pattern : patterns) {
            // Skip TYPE patterns.
            final RyaURI predicate = new RyaURI( pattern.getPredicateVar().getValue().toString() );
            if(predicate.equals(TYPE_ID_URI)) {
                continue;
            }

            if(!type.getPropertyNames().contains(predicate)) {
                throw new IllegalStateException("The Predicate of a Statement Pattern must be a property name for the Type. " +
                        "Type ID: '" + type.getId().getData() + "' Pattern: " + pattern);
            }
        }
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(final Collection<BindingSet> bindingSets) throws QueryEvaluationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(final BindingSet bindingSet) throws QueryEvaluationException {
        requireNonNull(bindingSet);
        // ... ok, so if the subject needs to be filled in, then we need to see if the subject variable is in the binding set.
        // if it is, fetch that value and then fetch the entity for the subject.
        final MapBindingSet resultSet = new MapBindingSet();
        try {
            final Optional<Entity> optEntity;
            final String subj;
            if(subjectIsConstant) {
                // if it isn't, fetch the entity for the constant?
                subj = subjectConstant.get();
                optEntity = entities.get(new RyaURI(subjectConstant.get()));
            } else {
                subj = subjectVar.get();
                if(bindingSet.getBindingNames().contains(subj)) {
                    final Binding binding = bindingSet.getBinding(subj);
                    optEntity = entities.get(new RyaURI(binding.getValue().toString()));
                } else {
                    // RETURN AN EMPTY ITERATION IF IT CAN NOT FILL IT IN!
                    return new EmptyIteration<BindingSet, QueryEvaluationException>();
                }
            }

            if(optEntity.isPresent()) {
                // for all variables in the OBJECT portion of the SPs, fill 'em in using the entity that is stored in the index.
                final Entity entity = optEntity.get();
                final ImmutableMap<RyaURI, Property> entityProps = entity.getProperties().get(type.getId());
                //ensure properties match and only add properties that are in the statement patterns to the binding set
                for(final RyaURI key : objectVariables.keySet()) {
                    if(entityProps.containsKey(key)) {
                        //the statement pattern has this property, check to see if the values match(if constant)
                        final Var object = objectVariables.get(key);
                        final RyaType entityType = entityProps.get(key).getValue();
                        final Value entityVal = ValueFactoryImpl.getInstance().createLiteral(entityType.getData(), entityType.getDataType());
                        if(object.isConstant()) {
                            final Value objectVal = object.getValue();
                            if(objectVal.equals(entityVal)) {
                                //add to binding set
                                resultSet.addBinding(object.getName(), entityVal);
                            }
                        } else {
                            resultSet.addBinding(object.getName(), entityVal);
                        }
                    } else {
                        //entity doesn't hold to statement patterns
                    }
                }
            } else {
                return new EmptyIteration<BindingSet, QueryEvaluationException>();
            }
        } catch (final EntityStorageException e) {
            throw new QueryEvaluationException("Failed to evaluate the binding set", e);
        }
        // x = alice's SSN
        // y = blue  <-- how do i know this is for urn:eye property? FROM THE STATEMENT PATTERN. look for the ?y in the SP, and that has the property name in it.
        bindingSet.forEach(new Consumer<Binding>() {
            @Override
            public void accept(final Binding binding) {
                resultSet.addBinding(binding);
            }
        });
        final List<BindingSet> list = new ArrayList<>();
        list.add(resultSet);
        return new CollectionIteration<>(list);
    }

    /**
     * @return - The {@link StatementPattern}s that make up this {@link EntityQueryNode}
     */
    public Collection<StatementPattern> getPatterns() {
        return patterns;
    }

    @Override
    public int hashCode() {
        int hashcode =
            (subjectIsConstant ? 1 : 0) +
            type.hashCode() +
            subjectVar.hashCode() +
            subjectConstant.hashCode();
        for(final StatementPattern sp : getPatterns()) {
            hashcode += sp.hashCode();
        }
        return hashcode;
    }

    @Override
    public boolean equals(final Object other) {
        if(other instanceof EntityQueryNode) {
            return hashCode() == other.hashCode();
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Type: " + type.toString());
        sb.append("Statement Patterns:");
        sb.append("-------------------");
        for(final StatementPattern sp : getPatterns()) {
            sb.append(sp.toString());
        }
        return sb.toString();
    }
}