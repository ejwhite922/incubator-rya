package mvm.rya.rdftriplestore.inference;

/*
 * #%L
 * mvm.rya.rya.sail.impl
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

import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;

import java.util.HashMap;
import java.util.Map;

/**
 * Class InferUnion
 * Date: Mar 14, 2012
 * Time: 12:43:49 PM
 */
public class InferUnion extends Union {
    private Map<String, String> properties = new HashMap<String, String>();

    public InferUnion() {
    }

    public InferUnion(TupleExpr leftArg, TupleExpr rightArg) {
        super(leftArg, rightArg);
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
