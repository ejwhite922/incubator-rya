package mvm.rya.accumulo;

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

import static mvm.rya.accumulo.AccumuloRdfConstants.EMPTY_CV;
import static mvm.rya.accumulo.AccumuloRdfConstants.EMPTY_VALUE;
import static mvm.rya.api.RdfCloudTripleStoreConstants.EMPTY_TEXT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mvm.rya.api.RdfCloudTripleStoreConfiguration;
import mvm.rya.api.RdfCloudTripleStoreConstants;
import mvm.rya.api.RdfCloudTripleStoreConstants.TABLE_LAYOUT;
import mvm.rya.api.domain.RyaStatement;
import mvm.rya.api.resolver.RyaTripleContext;
import mvm.rya.api.resolver.triple.TripleRow;
import mvm.rya.api.resolver.triple.TripleRowResolverException;

import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.hadoop.io.Text;

public class RyaTableMutationsFactory {

    RyaTripleContext ryaContext;

    public RyaTableMutationsFactory(RyaTripleContext ryaContext) {
    	this.ryaContext = ryaContext;
    }

    //TODO: Does this still need to be collections
    public Map<RdfCloudTripleStoreConstants.TABLE_LAYOUT, Collection<Mutation>> serialize(
            RyaStatement stmt) throws IOException {

        Collection<Mutation> spo_muts = new ArrayList<Mutation>();
        Collection<Mutation> po_muts = new ArrayList<Mutation>();
        Collection<Mutation> osp_muts = new ArrayList<Mutation>();
        /**
         * TODO: If there are contexts, do we still replicate the information into the default graph as well
         * as the named graphs?
         */
        try {
            Map<TABLE_LAYOUT, TripleRow> rowMap = ryaContext.serializeTriple(stmt);
            TripleRow tripleRow = rowMap.get(TABLE_LAYOUT.SPO);
            spo_muts.add(createMutation(tripleRow));
            tripleRow = rowMap.get(TABLE_LAYOUT.PO);
            po_muts.add(createMutation(tripleRow));
            tripleRow = rowMap.get(TABLE_LAYOUT.OSP);
            osp_muts.add(createMutation(tripleRow));
        } catch (TripleRowResolverException fe) {
            throw new IOException(fe);
        }

        Map<RdfCloudTripleStoreConstants.TABLE_LAYOUT, Collection<Mutation>> mutations =
                new HashMap<RdfCloudTripleStoreConstants.TABLE_LAYOUT, Collection<Mutation>>();
        mutations.put(RdfCloudTripleStoreConstants.TABLE_LAYOUT.SPO, spo_muts);
        mutations.put(RdfCloudTripleStoreConstants.TABLE_LAYOUT.PO, po_muts);
        mutations.put(RdfCloudTripleStoreConstants.TABLE_LAYOUT.OSP, osp_muts);

        return mutations;
    }

    protected Mutation createMutation(TripleRow tripleRow) {
        Mutation mutation = new Mutation(new Text(tripleRow.getRow()));
        byte[] columnVisibility = tripleRow.getColumnVisibility();
        ColumnVisibility cv = columnVisibility == null ? EMPTY_CV : new ColumnVisibility(columnVisibility);
        Long timestamp = tripleRow.getTimestamp();
        byte[] value = tripleRow.getValue();
        Value v = value == null ? EMPTY_VALUE : new Value(value);
        byte[] columnQualifier = tripleRow.getColumnQualifier();
        Text cqText = columnQualifier == null ? EMPTY_TEXT : new Text(columnQualifier);
        byte[] columnFamily = tripleRow.getColumnFamily();
        Text cfText = columnFamily == null ? EMPTY_TEXT : new Text(columnFamily);

        mutation.put(cfText, cqText, cv, timestamp, v);
        return mutation;
    }
}