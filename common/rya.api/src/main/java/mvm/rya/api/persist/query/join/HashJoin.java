package mvm.rya.api.persist.query.join;

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

import info.aduna.iteration.CloseableIteration;
import mvm.rya.api.RdfCloudTripleStoreConfiguration;
import mvm.rya.api.RdfCloudTripleStoreUtils;
import mvm.rya.api.domain.RyaStatement;
import mvm.rya.api.domain.RyaType;
import mvm.rya.api.domain.RyaURI;
import mvm.rya.api.persist.RyaDAOException;
import mvm.rya.api.persist.query.RyaQueryEngine;
import mvm.rya.api.resolver.RyaContext;
import mvm.rya.api.utils.EnumerationWrapper;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use HashTable to do a HashJoin.
 * <p/>
 * TODO: Somehow make a more streaming way of doing this hash join. This will not support large sets.
 * Date: 7/26/12
 * Time: 8:58 AM
 */
public class HashJoin<C extends RdfCloudTripleStoreConfiguration> implements Join<C> {

    private RyaContext ryaContext = RyaContext.getInstance();
    private RyaQueryEngine ryaQueryEngine;

    public HashJoin() {
    }

    public HashJoin(RyaQueryEngine ryaQueryEngine) {
        this.ryaQueryEngine = ryaQueryEngine;
    }

    @Override
    public CloseableIteration<RyaStatement, RyaDAOException> join(C conf, RyaURI... preds) throws RyaDAOException {
        ConcurrentHashMap<Map.Entry<RyaURI, RyaType>, Integer> ht = new ConcurrentHashMap<Map.Entry<RyaURI, RyaType>, Integer>();
        int count = 0;
        boolean first = true;
        for (RyaURI pred : preds) {
            count++;
            //query
            CloseableIteration<RyaStatement, RyaDAOException> results = ryaQueryEngine.query(new RyaStatement(null, pred, null), null);
            //add to hashtable
            while (results.hasNext()) {
                RyaStatement next = results.next();
                RyaURI subject = next.getSubject();
                RyaType object = next.getObject();
                Map.Entry<RyaURI, RyaType> entry = new RdfCloudTripleStoreUtils.CustomEntry<RyaURI, RyaType>(subject, object);
                if (!first) {
                    if (!ht.containsKey(entry)) {
                        continue; //not in join
                    }
                }
                ht.put(entry, count);
            }
            //remove from hashtable values that are under count
            if (first) {
                first = false;
            } else {
                for (Map.Entry<Map.Entry<RyaURI, RyaType>, Integer> entry : ht.entrySet()) {
                    if (entry.getValue() < count) {
                        ht.remove(entry.getKey());
                    }
                }
            }
        }
        final Enumeration<Map.Entry<RyaURI, RyaType>> keys = ht.keys();
        return new CloseableIteration<RyaStatement, RyaDAOException>() {
            @Override
            public void close() throws RyaDAOException {

            }

            @Override
            public boolean hasNext() throws RyaDAOException {
                return keys.hasMoreElements();
            }

            @Override
            public RyaStatement next() throws RyaDAOException {
                Map.Entry<RyaURI, RyaType> subjObj = keys.nextElement();
                return new RyaStatement(subjObj.getKey(), null, subjObj.getValue());
            }

            @Override
            public void remove() throws RyaDAOException {
                keys.nextElement();
            }
        };
    }

    @Override
    public CloseableIteration<RyaURI, RyaDAOException> join(C conf, Map.Entry<RyaURI, RyaType>... predObjs) throws RyaDAOException {
        ConcurrentHashMap<RyaURI, Integer> ht = new ConcurrentHashMap<RyaURI, Integer>();
        int count = 0;
        boolean first = true;
        for (Map.Entry<RyaURI, RyaType> predObj : predObjs) {
            count++;
            RyaURI pred = predObj.getKey();
            RyaType obj = predObj.getValue();
            //query
            CloseableIteration<RyaStatement, RyaDAOException> results = ryaQueryEngine.query(new RyaStatement(null, pred, obj), null);
            //add to hashtable
            while (results.hasNext()) {
                RyaURI subject = results.next().getSubject();
                if (!first) {
                    if (!ht.containsKey(subject)) {
                        continue; //not in join
                    }
                }
                ht.put(subject, count);
            }
            //remove from hashtable values that are under count
            if (first) {
                first = false;
            } else {
                for (Map.Entry<RyaURI, Integer> entry : ht.entrySet()) {
                    if (entry.getValue() < count) {
                        ht.remove(entry.getKey());
                    }
                }
            }
        }
        return new EnumerationWrapper<RyaURI, RyaDAOException>(ht.keys());
    }

    public RyaQueryEngine getRyaQueryEngine() {
        return ryaQueryEngine;
    }

    public void setRyaQueryEngine(RyaQueryEngine ryaQueryEngine) {
        this.ryaQueryEngine = ryaQueryEngine;
    }
}
