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
import org.openrdf.model.vocabulary.XMLSchema;

import java.util.Random;

/**
 * Date: 7/20/12
 * Time: 9:43 AM
 */
public class DoubleRyaTypeResolverTest extends TestCase {

    public void testDoubleSerialization() throws Exception {
        Double d = randomDouble();
        RyaType ryaType = new RyaType(XMLSchema.DOUBLE, d.toString());
        byte[] serialize = new DoubleRyaTypeResolver().serialize(ryaType);
        assertEquals(d, Double.parseDouble(new DoubleRyaTypeResolver().deserialize(serialize).getData()));
    }

    private double randomDouble() {
        return new Random(System.currentTimeMillis()).nextDouble();
    }
}
