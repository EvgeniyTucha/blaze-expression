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

package com.blazebit.expression.declarative.impl;

import com.blazebit.domain.runtime.model.DomainType;
import com.blazebit.expression.ExpressionInterpreter;
import com.blazebit.expression.spi.TypeAdapter;

import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
public class BooleanTypeAdapter implements TypeAdapter<Boolean, Boolean>, Serializable {

    public static final BooleanTypeAdapter INSTANCE = new BooleanTypeAdapter();

    private BooleanTypeAdapter() {
    }

    @Override
    public Boolean toInternalType(ExpressionInterpreter.Context context, Boolean value, DomainType domainType) {
        return value;
    }

    @Override
    public Boolean toModelType(ExpressionInterpreter.Context context, Boolean value, DomainType domainType) {
        return value;
    }
}
