package mvm.rya.api.resolver;

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

import org.openrdf.model.URI;

/**
 * Date: 7/16/12
 * Time: 12:11 PM
 */
public class RyaTypeResolverMapping {

    protected RyaTypeResolver ryaTypeResolver;

    public RyaTypeResolverMapping() {
    }

    public RyaTypeResolverMapping(RyaTypeResolver ryaTypeResolver) {
        this.ryaTypeResolver = ryaTypeResolver;
    }

    public void setRyaTypeResolver(RyaTypeResolver ryaTypeResolver) {
        this.ryaTypeResolver = ryaTypeResolver;
    }

    public RyaTypeResolver getRyaTypeResolver() {
        return ryaTypeResolver;
    }

    public URI getRyaDataType() {
        return ryaTypeResolver.getRyaDataType();
    }

    byte getMarkerByte() {
        return ryaTypeResolver.getMarkerByte();
    }

}
