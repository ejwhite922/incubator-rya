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
package mvm.rya.indexing.entity.storage.mongo;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bson.Document;

import mvm.rya.api.domain.RyaType;
import mvm.rya.api.domain.RyaURI;
import mvm.rya.indexing.entity.model.Entity;
import mvm.rya.indexing.entity.model.Property;

/**
 * Converts between {@link Entity} and {@link Document}.
 */
@ParametersAreNonnullByDefault
public class EntityDocumentConverter implements DocumentConverter<Entity> {

    public static final String SUBJECT = "_id";
    public static final String EXPLICIT_TYPE_IDS = "explicitTypeIds";
    public static final String PROPERTIES = "properties";
    public static final String VERSION = "version";

    private final RyaTypeDocumentConverter ryaTypeConverter = new RyaTypeDocumentConverter();

    @Override
    public Document toDocument(Entity entity) {
        requireNonNull(entity);

        final Document doc = new Document();
        doc.append(SUBJECT, entity.getSubject().getData());

        doc.append(EXPLICIT_TYPE_IDS, entity.getExplicitTypeIds().stream()
                .map(explicitTypeId -> explicitTypeId.getData())
                .collect(Collectors.toList()));

        final Document propertiesDoc = new Document();
        for(final RyaURI typeId : entity.getProperties().keySet()) {
            final Document typePropertiesDoc = new Document();
            entity.getProperties().get(typeId)
                .forEach((propertyNameUri, property) -> {
                    final String propertyName = property.getName().getData();
                    final RyaType value = property.getValue();
                    typePropertiesDoc.append(propertyName,  ryaTypeConverter.toDocument(value));
                });
            propertiesDoc.append(typeId.getData(), typePropertiesDoc);
        }
        doc.append(PROPERTIES, propertiesDoc);

        doc.append(VERSION, entity.getVersion());

        return doc;
    }

    @Override
    public Entity fromDocument(Document document) throws DocumentConverterException {
        requireNonNull(document);

        // Preconditions.
        if(!document.containsKey(SUBJECT)) {
            throw new DocumentConverterException("Could not convert document '" + document +
                    "' because its '" + SUBJECT + "' field is missing.");
        }

        if(!document.containsKey(EXPLICIT_TYPE_IDS)) {
            throw new DocumentConverterException("Could not convert document '" + document +
                    "' because its '" + EXPLICIT_TYPE_IDS + "' field is missing.");
        }

        if(!document.containsKey(PROPERTIES)) {
            throw new DocumentConverterException("Could not convert document '" + document +
                    "' because its '" + PROPERTIES + "' field is missing.");
        }

        if(!document.containsKey(VERSION)) {
            throw new DocumentConverterException("Could not convert document '" + document +
                    "' because its '" + VERSION + "' field is missing.");
        }

        // Perform the conversion.
        final Entity.Builder builder = Entity.builder()
                .setSubject( new RyaURI(document.getString(SUBJECT)) );

        ((List<String>)document.get(EXPLICIT_TYPE_IDS)).stream()
            .forEach(explicitTypeId -> builder.setExplicitType(new RyaURI(explicitTypeId)));

        final Document propertiesDoc = (Document) document.get(PROPERTIES);
        for(final String typeId : propertiesDoc.keySet()) {
            final Document typePropertiesDoc = (Document) propertiesDoc.get(typeId);
            for(final String propertyName : typePropertiesDoc.keySet()) {
                final Document value = (Document) typePropertiesDoc.get(propertyName);
                final RyaType propertyValue = ryaTypeConverter.fromDocument( value );
                builder.setProperty(new RyaURI(typeId), new Property(new RyaURI(propertyName), propertyValue));
            }
        }

        builder.setVersion( document.getInteger(VERSION) );

        return builder.build();
    }
}