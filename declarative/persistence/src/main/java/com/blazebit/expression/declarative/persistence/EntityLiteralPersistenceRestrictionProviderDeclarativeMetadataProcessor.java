/*
 * Copyright 2019 - 2022 Blazebit.
 *
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
 */

package com.blazebit.expression.declarative.persistence;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.domain.boot.model.MetadataDefinition;
import com.blazebit.domain.declarative.spi.DeclarativeMetadataProcessor;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
@ServiceProvider(DeclarativeMetadataProcessor.class)
public class EntityLiteralPersistenceRestrictionProviderDeclarativeMetadataProcessor implements DeclarativeMetadataProcessor<EntityLiteralPersistenceCapable> {

    @Override
    public Class<EntityLiteralPersistenceCapable> getProcessingAnnotation() {
        return EntityLiteralPersistenceCapable.class;
    }

    @Override
    public MetadataDefinition<?> process(Class<?> annotatedClass, EntityLiteralPersistenceCapable annotation, com.blazebit.domain.spi.ServiceProvider serviceProvider) {
        return new ExpressionLiteralPersistenceRestrictionProviderMetadata(annotation.value());
    }

}
