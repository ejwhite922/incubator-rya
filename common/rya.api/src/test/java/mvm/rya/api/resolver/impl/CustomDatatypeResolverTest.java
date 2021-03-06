package mvm.rya.api.resolver.impl;

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

import junit.framework.TestCase;
import mvm.rya.api.domain.RyaType;
import org.openrdf.model.impl.URIImpl;

/**
 * Date: 7/16/12
 * Time: 2:47 PM
 */
public class CustomDatatypeResolverTest extends TestCase {

    public void testCustomDataTypeSerialization() throws Exception {
        RyaType ryaType = new RyaType(new URIImpl("urn:test#datatype"), "testdata");
        byte[] serialize = new CustomDatatypeResolver().serialize(ryaType);
        RyaType deserialize = new CustomDatatypeResolver().deserialize(serialize);
        assertEquals(ryaType, deserialize);
    }
}
