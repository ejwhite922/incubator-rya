package mvm.rya.api.query.strategy;

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

import mvm.rya.api.RdfCloudTripleStoreConfiguration;
import mvm.rya.api.domain.RyaType;
import mvm.rya.api.domain.RyaURI;
import mvm.rya.api.resolver.triple.TripleRowRegex;

import java.io.IOException;
import java.util.Map;

import static mvm.rya.api.RdfCloudTripleStoreConstants.TABLE_LAYOUT;

/**
 * Date: 7/14/12
 * Time: 7:21 AM
 */
public interface TriplePatternStrategy {

    public Map.Entry<TABLE_LAYOUT, ByteRange> defineRange(RyaURI subject, RyaURI predicate, RyaType object, RyaURI context,
                                                          RdfCloudTripleStoreConfiguration conf) throws IOException;

    public TABLE_LAYOUT getLayout();

    public boolean handles(RyaURI subject, RyaURI predicate, RyaType object, RyaURI context);

    public TripleRowRegex buildRegex(String subject, String predicate, String object, String context, byte[] objectTypeInfo);

}
